# tudursvehiclemod — Forge 1.20.1 版 開発メモ

このプロジェクトは、Mojang マッピング版 (1.21.11 Fabric の開発用コピー, `tudursvehiclemod-mojmap`) から
**ソースを自動生成**して Forge 1.20.1 でビルドする構成です。本体のコードは手で書き写さず、
「共通コア (自動生成) + 版ごとの層 (Forge 層)」に分けています。

```
src/generated/   tools/port/port.py が生成する本体コード・リソース (1.20.1 名に変換済み)。直接編集しない。
                 ※ 編集した場合は必ず `port.py makepatches` で tools/port/patches に保存する
src/forge/       Forge 専用コード: エントリポイント・イベント橋渡し・1.21.11 API の互換レイヤー・mixin 設定
src/harness/     テスト用 mod "tvmtest" (別 jar。通常プレイには不要)
tools/port/      生成ツール一式 (port.py / build_cross.py / rules.json / patches / exclude.txt / autofix.py)
```

## 必要なもの

- JDK 17 (ForgeGradle 6 のビルド用) と JDK 21 (生成時に Mojang 版プロジェクトの Loom を動かす用)
- Mojang 版プロジェクト (`tudursvehiclemod-mojmap`) を一度ビルド済みであること
  (Loom キャッシュに 1.21.11 の Mojang マッピングが必要)

## 生成とビルド

```sh
# 1. Mojang 版から src/generated を作り直す (Mojang 版を更新したときだけ)
LOOM_JAVA_HOME=/path/to/jdk21 python3 tools/port/port.py generate --mojmap ../tudursvehiclemod-mojmap

# 2. Forge 版をビルド
JAVA_HOME=/path/to/jdk17 ./gradlew build          # -> build/libs/tudursvehiclemod-1.1.1-forge-1.20.1.jar
JAVA_HOME=/path/to/jdk17 ./gradlew harnessJar     # -> build/libs/tvmtest-forge-0.1.jar (テスト用)
```

jar 名は `tudursvehiclemod-<mod_version>-<mod_loader>-<minecraft_version>.jar` です (`gradle.properties` の
`mod_version`・`mod_loader`・`minecraft_version`)。`mods.toml` のバージョンは `mod_version` (`1.1.1`) のままです。
Fabric 版 (`tudursvehiclemod-1.1.1-fabric-1.21.11.jar`) と同じ規則です。

`port.py refresh` は Loom を動かさずに、前回の変換結果へ rules と patches だけを掛け直します
(rules.json を変えたとき用)。作業フォルダは `.port/` (`gradlew clean` では消えません)。

### 生成の流れ

1. **名前の変換**: 1.21.11 の intermediary 名をキーに、1.20.1 の Mojang 名を値にした
   マッピングを作り (`build_cross.py`)、Loom の `migrateClientMappings` で main/client の全ソースを
   型解決付きで書き換えます。intermediary 名は版をまたいで変わらないため、1.20.1 に残っている
   クラス・メソッド・フィールドはすべて 1.20.1 の名前になります (1.20.1 に無いものは 1.21.11 名のまま残る)。
2. **rules.json**: 1.20.1 に無い API の import を互換レイヤー (`com.example.tudursvehiclemod.compat`) へ
   付け替え、呼び出し形の違いを正規表現で書き換えます。特定ファイルだけに効かせたい規則は
   `"files": "**/Foo.java"` を付けます。
3. **patches**: 規則にしにくい個別の修正 (`tools/port/patches/**.patch`)。適用は fuzz 3 で行うので、
   規則の変更で前後の行が少し変わっても大抵は当たります。当たらない場合は `.rej` が残るので手で直し、
   `makepatches` で作り直します。
4. **リソース**: 1.21 のデータフォルダ名 (recipe, loot_table, tags/item …) を 1.20.1 の名前に直し、
   レシピの JSON 形式 (材料の書き方、結果の `id`→`item`、染色 → `nbt.display.color`) を変換します。

### 編集の手順 (重要)

```sh
# src/generated を直したら必ず
python3 tools/port/port.py makepatches
# Java 17 で通らない instanceof (式の型がすでにパターン型のもの) は自動修正できる
JAVA_HOME=/path/to/jdk17 ./gradlew compileJava 2>&1 | tee cc.log; python3 tools/port/autofix.py cc.log
```

`makepatches` をせずに `generate`/`refresh` すると、src/generated の手修正は消えます。

## Forge 層 (src/forge) の中身

| ファイル | 役割 |
|---|---|
| `forge/TudursVehicleModForge` | `@Mod` エントリ。最初の `RegisterEvent` で共通の初期化 (`VehicleMod.onInitialize`、クライアントでは `VehicleModClient.onInitializeClient`) を実行 |
| `compat/Registration` | `Registry.register(...)` の代わり。オブジェクトはその場で返し、実登録は各レジストリの `RegisterEvent` で行う |
| `forge/ForgeEvents` / `ForgeClientEvents` | Fabric 形式のイベント (互換レイヤー側) を Forge のイベントから発火 |
| `compat/fabric/...` | Fabric API の代替 (イベント、ネットワーク、キー登録、レンダラ登録、リロードリスナー等) |
| `compat/net/Channel` | `CustomPacketPayload` + `StreamCodec` を Forge の SimpleChannel 1 本で送受信 |
| `compat/mc/...` | 1.20.1 に無い 1.21 の型: `StreamCodec`/`ByteBufCodecs`、`ValueInput`/`ValueOutput` (NBT の上に実装)、`DataComponentType` (アイテム NBT の上に実装)、`EntityRenderState` 系と状態ベースの `EntityRenderer`、`SubmitNodeCollector`、`Gizmos` |
| `compat/Compat` | 呼び出し形が違うものの小さな変換 (DustParticle、染色、NativeImage の色順、NBT の既定値付き取得 等) |
| `forge/client/CameraRoll` | 1.20.1 は視点の回転を yaw/pitch + `ComputeCameraAngles` の roll で作るため、機体のロールをここ経由で渡す |
| `forge/client/DitherRenderTypes` | 半透明表現 `dither_shader` 用の RenderType。シェーダーは `assets/tudursvehiclemod/shaders/core/rendertype_entity_dither_cutout.*` |
| `forge/mixin/...` | 1.20.1 用に書いた mixin (`tudursvehiclemod.forge.mixins.json`)。下記 |

### Mixin

mixin は他のソースと同様に生成・変換し、1.20.1 向けにパッチを当てて使っています
(refmap は MixinGradle が生成)。1.20.1 に対象が無いため外しているもの (`tools/port/exclude.txt`):

- `ExplosionKnockbackMixin` (1.20.1 の `ExplosionDamageCalculator` にノックバック倍率が無い)
  → `forge/mixin/ExplosionKnockbackForgeMixin` (`Explosion.explode` で乗り物への押し出しだけを止める)
- `LivingEntityRenderStateMixin` / `LivingEntityRendererMixin` (1.21 の描画ステート方式に依存)
  → `forge/mixin/client/LivingEntityRendererForgeMixin` (搭乗者を機体と一体で描画: 向き・ピッチ・ロールへの追従、
  頭の正面固定、HideEntity・遠隔操縦時の非表示、EntityWidth/EntityHeight)
- `BipedEntityModelMixin` (パラシュート降下中は座り姿勢にしない) → `ParachuteEntity.shouldRiderSit()` (Forge の機能) をパッチで追加
- `RenderLayerFactoryInvoker` (独自 RenderPipeline 用) → `forge/client/DitherRenderTypes`

ほかに `forge/mixin/BlockEntityLoadForgeMixin` で、ブロックエンティティがサーバーのチャンクに追加されたとき
(チャンク読み込み・設置の両方) に `BLOCK_ENTITY_LOAD` を発火しています (Fabric API と同じタイミング)。
`forge/mixin/client/MoveEntityCodecForgeMixin` は、自分が操縦している乗り物についても、サーバーから届く差分の位置の
基準 (`VecDeltaCodec`) を更新します (1.21.2 以降のバニラと同じ動き。下記「修正 (2026-11-01、サーバー負荷の調査)」)。
Forge では `@ModifyArgs` が使えない (生成クラスが読み込めない) ため、引数の書き換えは `@ModifyArg` で行っています。

## 試作で確認したこと (2026-10-31)

環境: Forge 1.20.1-47.4.28、アドオンパック FictionalWW2Pack と sample_pack。

- 製品 jar (SRG 名・refmap 付き) で専用サーバーが起動し、機体定義 29 件・武器 33 件を読み込む
- `generate` で作り直したソースが手元の `src/generated` と完全に一致する (再現性)
- 戦闘機 `ww2fict:a_fighter` の操縦 (開発環境クライアント・シングルプレイ):
  表示・テクスチャ・プロペラ、搭乗、スロットルで加速して離陸、マウス入力で機首上げ (ピッチ -60°)、
  旋回時のカメラのロール、一人称/三人称、HUD (スロットル・燃料・速度)、機関銃 (弾数の減少と弾の生成)
- 保存: 9 機種 (戦闘機、サンプルの航空機/固定砲台/ハーフトラック/ヘリ/船/潜水艦/UAV/VTOL) で燃料・弾薬・体力を
  変えてから再起動し、状態が一致 (差はダンプから保存までの間の物理による微小変化のみ)

## 確認したこと (2026-11-01 追加)

- 搭乗者の描画: 操縦席に収まり、機体のピッチ・ロールに合わせて傾く (三人称のスクリーンショットで確認)
- `dither_shader`: シェーダーが読み込まれ、キャノピーがディザ (網目) で透けて描かれる
- 画面: 燃料精製機・ステーション・ドローンセンター・変換機・火力支援センター・機体選択 (スポーンアイテム)・
  機体メニュー (キー) の 7 画面が、通常の操作 (ブロックの右クリック・アイテム使用・キー) で開いて表示される
- 全機種・全武装の自動テスト (`/tvmt run`、Fabric 版ハーネスと同じ手順): 29 機種で例外 0 件。全武装が発射され弾が生成される。
  ダミーパイロットの搭乗数も Fabric 版と一致。発射回数・命中ダメージの一部は Fabric 版と違うが、原因はテスト環境
  (プレイヤーのいないサーバーで、出したての機体が約 20 tick 後に tick されなくなる) で、両版で同じように起きることを
  1 tick ごとのログで確認した (`/tvmt firetest`)

## 修正 (2026-11-01、forge.0.3 / 0.4)

- 三人称視点の位置: 1.20.1 の `Camera.rotation()` は 1.21.11 と前後が逆 (+Z が視線方向) のため、
  カメラを後ろへ引く処理 (`CameraMixin.pullBackAlongCurrentRotation`) が前へ引いていた。`getLookVector()` の逆向きに修正
  (車・船・ヘリ・潜水艦と、航空機の自由視点の三人称が対象)
- 潜水艦の位置・姿勢の同期 (`VehicleMoveIgnoreMixin`、1.21.11 版と共通。Yarn 版・Mojang 版にも同じ修正)
  - 受信処理 `handleMoveVehicle` は通信スレッドで一度呼ばれてからサーバースレッドで呼び直される。サーバーの位置を控えて戻す処理が
    通信スレッド側でも動いて競合し、サーバーがクライアントの数 tick 前の位置・姿勢を取り込むことがあった
  - 潜水艦だけサーバーの計算を正としていたため、操縦者のクライアントの予測は常に「通信の遅れ × 速度」だけ先行し、
    バニラの引き戻し (moved wrongly) とその返信が互いに引き金になって毎秒数百回の引き戻しが続いていた
  - サーバーを正とした理由 (昇降キーをクライアントが予測しない) は、クライアントが入力を自分の機体にも反映するようになって
    解消済みだったため、潜水艦も他の乗り物と同じく操縦者のクライアントの位置を採用する方式に戻した。
    滑走路で運ばれている間とアドオンの外部制御中の扱いは従来どおり。どの処理もサーバースレッドでだけ行う
  - 確認 (`/tvmt subsetup`、潜航・旋回・浮上、Forge 版と Fabric 版): 引き戻しの警告 0 件、
    クライアントとサーバーの位置・姿勢は一致 (差は通信 1 tick 分のみ)

## 修正 (2026-11-01、サーバー負荷の調査)

報告: 専用サーバーで潜水艦を操縦中に武装が使えなくなる、降車直後に機体が手前へ戻ってから正しい位置へ飛ぶ。

- 降車直後の巻き戻り (Forge 版のみ): 1.20.1 の `ClientPacketListener.handleMoveEntity` は、自分が操縦している乗り物への
  移動パケットを丸ごと捨てるため、差分の位置の基準が搭乗時のまま止まる。降車して差分を受け取り始めた瞬間、古い基準から
  計算した位置へ戻り、次の絶対位置の同期で正しい位置へ飛んでいた。1.21.11 は基準だけ更新するため起きない。
  → `MoveEntityCodecForgeMixin` で同じ動きにした。確認: 降車の前後で位置が連続し、サーバーの位置と一致 (修正前は約 50 ブロック離れた
  位置へ戻っていた)
- 1.21.11 版と共通の修正 (Mojang 版から再生成で反映): 機体の向きの補間用の値 (`yRotO`) を毎 tick 上書きしていたため、
  向きが補間されず旋回中に 1 tick ごとの段になっていた。旋回中に乗員の視点を回す処理も補間に含まれるようにした
- 武装: テストで潜航中に魚雷が撃てなかったのは、魚雷の姿勢制限 (ピッチ・ロールが 15° を超えると発射不可、MCヘリと同じ仕様) による。
  浮上して水平に戻ると発射できる。サーバーの遅れによる発射の取りこぼしは見られなかった
- サーバー負荷 (専用サーバー + 同じマシンのクライアント、CPU 2 コア、JFR で計測):
  - 乗っている間の 1 tick の平均は 4.6〜10.8 ms (上限 50 ms)。時々ある 60〜600 ms のスパイクは GC の停止で、
    サーバーとソフトウェア描画のクライアントが 2 コアを取り合うこの環境によるもの
  - サーバースレッドの時間の多くはバニラのチャンク処理 (周囲チャンクの tick・自然スポーン判定)。mod の乗り物の tick は約 10%、
    mod のコードによるメモリ割り当ては全体の約 8%
  - テスト用ワールドに、どの機能も使っていない強制ロードのチャンクが 47 個残っていた (サーバーを強制終了したときの残り)。
    `/forceload query` で確認、`/tvm unloadchunks` で解除できる。解除後は待機時 1.7 ms/tick
- テスト: `/tvmt subsetup` は水槽があれば作り直さない (作り直すと水の更新で負荷が出る)。乗っている間はサーバーの tick 時間を
  100 tick ごとに記録 (`[tvmt] MSPT avg max`)。潜水艦の手順に、潜航中・浮上後の発射と降車 (降車前後の位置を記録) を追加。
  専用サーバーで試すとき、テスト用プレイヤーはオペレーターにする (サーバーが遅れるとコマンドの連投判定でキックされるため)

## 1.21.11 版との差

- アイテムの見た目: 1.20.1 は `models/item/*.json` を使用 (同梱済み)。パラシュートは `Equipable` (右クリックで胸に装備) と
  `DyeableLeatherItem` (革防具と同じく作業台で染色、色はアイテムに表示) で再現。
- アドオン: 1.21.11 Fabric 用のアドオン mod (jar) はそのままでは動きません。アドオンパック
  (`tudursvehiclemod-addons/` のデータ・アセット) はそのまま読み込めます。

## テスト (src/harness)

テスト用 mod `tvmtest` のコマンド (OP 不要):

```
/tvmt spawn <機体ID>              プレイヤーの 10 ブロック前に出す (燃料・弾薬は満タン)
/tvmt spawnat <x> <y> <z> <機体ID> 座標指定 (コンソールから可)
/tvmt ride                        直前に出した機体にパイロットとして乗る
/tvmt state                       搭乗中 (または直前) の機体の状態をログへ  "[tvmt] STATE {...}"
/tvmt dump <tag>                  読み込まれている全機体の状態をログへ    "[tvmt] DUMP <tag> [...]"
/tvmt run <name>                  全機種・全武装の自動テスト (Fabric 版ハーネスと同じ手順) → tvmtest-<name>.json
/tvmt firetest <武器番号> <機体ID> 1 機を出して 60 tick 撃ち続け、1 tick ごとに冷却・弾数をログへ "[tvmt] FT ..."
/tvmt home                        プレイヤーをワールドのスポーン地点の地面へ
/tvmt subsetup <機体ID>           ガラスの水槽 (x -30..70, z -50..50, 水面 y -26) を作り、潜水艦に乗せる
/tvmt place <x> <y> <z> <ブロック> / /tvmt give <アイテム>   画面テスト用
```

```
/tvmt prep                        読み込まれている全機体: 弾薬補充、燃料 37%、武器0 を 3 発 (保存テスト用)
```

自動飛行テスト (開発環境のクライアントで、離陸・上昇・バンク・機銃射撃・スクリーンショット):

```sh
# シングルプレイ (run/saves/<名前> に既存のワールドが必要)
JAVA_HOME=/path/to/jdk17 ./gradlew runClient -PtvmtAuto=ww2fict:a_fighter -PtvmtWorld=<名前>
# マルチプレイ
JAVA_HOME=/path/to/jdk17 ./gradlew runClient -PtvmtAuto=ww2fict:a_fighter -PtvmtServer=localhost:25565
# 画面テスト (7 画面を順に開いてスクリーンショット)
JAVA_HOME=/path/to/jdk17 ./gradlew runClient -PtvmtAuto=ww2fict:a_fighter -PtvmtWorld=<名前> -PtvmtGui=1
# 潜水艦 (機体 ID に submarine を含むと潜航・旋回・浮上の手順になる)。-PtvmtTrace=1 でサーバーと
# クライアントの位置・姿勢を毎 tick 記録 ("[tvmt] TR SV/CL <ゲーム時刻>")
JAVA_HOME=/path/to/jdk17 ./gradlew runClient -PtvmtAuto=tudur_sample:sample_submarine -PtvmtWorld=<名前> -PtvmtTrace=1
# スクリーンショット: run/screenshots/tvmt_*.png、ログ: run/logs/latest.log の "[tvmt]" 行
```

アドオンパックは実行フォルダの `tudursvehiclemod-addons/` に置きます (開発環境では `run/tudursvehiclemod-addons/`)。

サーバーに接続したクライアントは、機体定義と武器設定を自分の環境のパックから読み込み、サーバーが送るハッシュと
照合します (一致しないとチャットに警告)。サーバーと同じアドオンパックを入れてください。

## 残り作業

| 項目 | 内容 | 規模 |
|---|---|---|
| 操縦の確認 | 車・船・潜水艦・ヘリ・VTOL・UAV・空母甲板を実際に操縦して確認 (自動テストでは移動・発射まで) | 中 |
| 特殊な武装の確認 | TV 誘導ミサイルの操縦、ロックオン、フレア、CAS・艦載機の編隊 (発射・弾の生成までは確認済み) | 中 |
| その他の確認 | HUD スクリプトの各要素、音、サーチライトの照明、ドローンの自律飛行、遠隔操縦 | 中 |
| アドオン mod | 1.21.11 Fabric 用 jar は動かないため、必要なものから個別に移植 | アドオンごと |
