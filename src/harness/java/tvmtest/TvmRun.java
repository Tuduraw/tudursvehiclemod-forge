package tvmtest;

import com.example.tudursvehiclemod.asset.VehicleDefinition;
import com.example.tudursvehiclemod.asset.VehicleRegistry;
import com.example.tudursvehiclemod.asset.WeaponDefinition;
import com.example.tudursvehiclemod.entity.AbstractVehicleEntity;
import com.example.tudursvehiclemod.entity.DummyPilotEntity;
import com.example.tudursvehiclemod.registry.ModEntityTypes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntConsumer;

/**
 * Forge port of the Fabric harness's "/tvmtest run" scenario (vt/harness TvmTest), step for step, so the two reports can be
 * compared field by field (vt/compare_reports.py). For every vehicle definition, at its own site (x = 2000 + i*320):
 * idle 60 ticks; a dummy pilot at full throttle for 160 ticks; each weapon fired unmanned at a husk 40 blocks ahead for
 * 60 ticks (then 100 ticks to resolve); dummy pilots in every seat against 4 zombies for 200 ticks.
 * Writes tvmtest-&lt;name&gt;.json in the server folder and logs "[tvmtest] DONE &lt;name&gt;".
 */
final class TvmRun {
	static final int GROUND_Y = -60;
	static final int WATER_TOP = -39;

	record Step(String label, Runnable start, int ticks, IntConsumer each) {
	}

	static final TvmRun INSTANCE = new TvmRun();

	final Deque<Step> queue = new ArrayDeque<>();
	Step current;
	int currentTick;
	MinecraftServer server;
	JsonObject report;
	String reportName;
	final Map<String, Integer> spawnCounts = new TreeMap<>();
	boolean counting;

	private TvmRun() {
		MinecraftForge.EVENT_BUS.addListener((EntityJoinLevelEvent e) -> {
			if (counting && !e.getLevel().isClientSide()) {
				spawnCounts.merge(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType())), 1, Integer::sum);
			}
		});
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent e) -> {
			if (e.phase == TickEvent.Phase.END) {
				tick();
			}
		});
	}

	void tick() {
		if (current == null) {
			current = queue.poll();
			if (current == null) {
				return;
			}
			currentTick = 0;
			guard(current.label() + "/start", current.start());
		}
		if (currentTick < current.ticks()) {
			final int t = currentTick;
			if (current.each() != null) {
				guard(current.label() + "/tick", () -> current.each().accept(t));
			}
			currentTick++;
		}
		if (currentTick >= current.ticks()) {
			current = null;
		}
	}

	void guard(String where, Runnable r) {
		if (r == null) {
			return;
		}
		try {
			r.run();
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			TvmTest.LOG.error("[tvmtest] exception in {}", where, t);
			JsonObject e = new JsonObject();
			e.addProperty("where", where);
			e.addProperty("exception", sw.toString().lines().limit(6).reduce("", (a, b) -> a + b.trim() + " | "));
			report.getAsJsonArray("exceptions").add(e);
		}
	}

	void then(String label, Runnable start) {
		queue.add(new Step(label, start, 0, null));
	}

	void wait(String label, int ticks, IntConsumer each) {
		queue.add(new Step(label, null, ticks, each));
	}

	void start(MinecraftServer srv, String name, int limit) {
		this.server = srv;
		this.reportName = name;
		this.report = new JsonObject();
		report.addProperty("mode", "run");
		report.add("exceptions", new JsonArray());
		ServerLevel world = srv.overworld();
		List<ResourceLocation> ids = new ArrayList<>(VehicleRegistry.getAll().keySet());
		ids.sort(java.util.Comparator.comparing(ResourceLocation::toString));
		report.addProperty("definitionCount", ids.size());
		TvmTest.LOG.info("[tvmtest] run {} with {} definitions", name, ids.size());
		JsonObject vehicles = new JsonObject();
		report.add("vehicles", vehicles);
		for (int i = 0; i < ids.size() && i < limit; i++) {
			scheduleVehicle(world, ids.get(i), 2000 + i * 320, vehicles);
		}
		then("write", this::write);
	}

	void write() {
		try {
			Path out = Path.of("tvmtest-" + reportName + ".json").toAbsolutePath();
			Files.writeString(out, new GsonBuilder().setPrettyPrinting().create().toJson(report), StandardCharsets.UTF_8);
			TvmTest.LOG.info("[tvmtest] DONE {} -> {}", reportName, out);
		} catch (Exception e) {
			TvmTest.LOG.error("[tvmtest] write failed", e);
		}
	}

	boolean isWater(VehicleDefinition def) {
		String t = def.entityType().getPath();
		return t.equals("ship") || t.equals("submarine");
	}

	void scheduleVehicle(ServerLevel world, ResourceLocation id, int siteX, JsonObject vehicles) {
		VehicleDefinition def = VehicleRegistry.get(id).orElseThrow();
		JsonObject rec = new JsonObject();
		vehicles.add(id.toString(), rec);
		rec.addProperty("entityType", def.entityType().toString());
		rec.addProperty("weaponCount", def.weapons().size());
		final AbstractVehicleEntity[] v = new AbstractVehicleEntity[1];
		Vec3 origin = new Vec3(siteX + 0.5, isWater(def) ? WATER_TOP : GROUND_Y, 0.5);

		then(id + "/prepare", () -> {
			forceChunks(world, siteX, true);
			if (isWater(def)) {
				buildPool(world, siteX);
			}
		});
		then(id + "/spawn", () -> v[0] = spawn(world, id, origin));
		wait(id + "/idle", 60, null);
		then(id + "/idle-record", () -> rec.add("idle", state(v[0])));
		final DummyPilotEntity[] driver = new DummyPilotEntity[1];
		then(id + "/mount", () -> {
			driver[0] = (DummyPilotEntity) ModEntityTypes.DUMMY_PILOT.create(world);
			driver[0].moveTo(v[0].getX(), v[0].getY(), v[0].getZ(), 0f, 0f);
			world.addFreshEntity(driver[0]);
			TvmTest.fillFuel(v[0]);
			rec.addProperty("pilotMounted", driver[0].startRiding(v[0], true));
		});
		wait(id + "/drive", 160, t -> {
			if (v[0] != null && !v[0].isRemoved()) {
				v[0].setSyncedThrottleInput(1.0f);
			}
		});
		then(id + "/drive-record", () -> {
			rec.add("drive", state(v[0]));
			driver[0].discard();
			v[0].discard();
		});
		JsonArray weapons = new JsonArray();
		rec.add("weapons", weapons);
		for (int w = 0; w < def.weapons().size(); w++) {
			final int wi = w;
			WeaponDefinition wd = def.weapons().get(w);
			JsonObject wr = new JsonObject();
			weapons.add(wr);
			final LivingEntity[] target = new LivingEntity[1];
			final int[] fired = new int[1];
			then(id + "/w" + w + "/setup", () -> {
				wr.addProperty("type", String.valueOf(wd.weaponType()));
				v[0] = spawn(world, id, origin);
				TvmTest.fillAmmo(v[0]);
				target[0] = husk(world, new Vec3(origin.x, GROUND_Y, origin.z + 40));
				spawnCounts.clear();
				counting = true;
			});
			wait(id + "/w" + w + "/settle", 20, null);
			wait(id + "/w" + w + "/fire", 60, t -> {
				if (v[0] != null && !v[0].isRemoved()
						&& v[0].tudursvehiclemod$tryFireWeaponAt(wi, target[0].position().add(0, 1, 0))) {
					fired[0]++;
				}
			});
			wait(id + "/w" + w + "/resolve", 100, null);
			then(id + "/w" + w + "/record", () -> {
				counting = false;
				wr.addProperty("fired", fired[0]);
				JsonObject sc = new JsonObject();
				spawnCounts.forEach(sc::addProperty);
				wr.add("spawned", sc);
				wr.addProperty("targetAlive", target[0].isAlive());
				wr.addProperty("targetHealth", round(target[0].getHealth()));
				wr.addProperty("magazine", v[0].tudursvehiclemod$getWeaponMagazineAmmo(wi));
				wr.addProperty("reserve", v[0].tudursvehiclemod$getWeaponReserveAmmo(wi));
				wr.add("vehicle", state(v[0]));
				cleanup(world, siteX);
			});
		}
		JsonObject combat = new JsonObject();
		rec.add("dummyCombat", combat);
		final List<LivingEntity> zombies = new ArrayList<>();
		then(id + "/combat-setup", () -> {
			v[0] = spawn(world, id, origin);
			TvmTest.fillAmmo(v[0]);
			int seats = Math.max(1, v[0].getDefinition().seats().size());
			int mounted = 0;
			for (int s = 0; s < seats; s++) {
				DummyPilotEntity p = (DummyPilotEntity) ModEntityTypes.DUMMY_PILOT.create(world);
				p.moveTo(origin.x, origin.y, origin.z, 0f, 0f);
				world.addFreshEntity(p);
				if (p.startRiding(v[0], true)) {
					mounted++;
				}
			}
			combat.addProperty("pilotsMounted", mounted);
			zombies.clear();
			for (int z = 0; z < 4; z++) {
				zombies.add(zombie(world, new Vec3(origin.x - 6 + z * 4, GROUND_Y, origin.z + 25)));
			}
			spawnCounts.clear();
			counting = true;
		});
		wait(id + "/combat", 200, null);
		then(id + "/combat-record", () -> {
			counting = false;
			JsonObject sc = new JsonObject();
			spawnCounts.forEach(sc::addProperty);
			combat.add("spawned", sc);
			double hp = 0;
			int alive = 0;
			for (LivingEntity z : zombies) {
				if (z.isAlive()) {
					alive++;
					hp += z.getHealth();
				}
			}
			combat.addProperty("zombiesAlive", alive);
			combat.addProperty("zombieHealth", round(hp));
			combat.add("vehicle", state(v[0]));
			cleanup(world, siteX);
			forceChunks(world, siteX, false);
		});
	}

	AbstractVehicleEntity spawn(ServerLevel world, ResourceLocation id, Vec3 pos) {
		VehicleDefinition def = VehicleRegistry.get(id).orElseThrow();
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(def.entityType());
		Entity e = type.create(world);
		if (e instanceof AbstractVehicleEntity av) {
			av.setVehicleDefinitionId(id);
		}
		e.moveTo(pos.x, pos.y, pos.z, 0f, 0f);
		world.addFreshEntity(e);
		return (AbstractVehicleEntity) e;
	}

	LivingEntity husk(ServerLevel world, Vec3 pos) {
		Mob m = EntityType.HUSK.create(world);
		m.setNoAi(true);
		m.setPersistenceRequired();
		m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);
		m.setHealth(1024);
		m.moveTo(pos.x, pos.y, pos.z, 180f, 0f);
		world.addFreshEntity(m);
		return m;
	}

	LivingEntity zombie(ServerLevel world, Vec3 pos) {
		Mob m = EntityType.ZOMBIE.create(world);
		m.setNoAi(true);
		m.setPersistenceRequired();
		m.moveTo(pos.x, pos.y, pos.z, 180f, 0f);
		world.addFreshEntity(m);
		return m;
	}

	void forceChunks(ServerLevel world, int siteX, boolean on) {
		for (int cx = (siteX >> 4) - 5; cx <= (siteX >> 4) + 5; cx++) {
			for (int cz = -5; cz <= 12; cz++) {
				world.setChunkForced(cx, cz, on);
			}
		}
	}

	void buildPool(ServerLevel world, int siteX) {
		BlockState water = Blocks.WATER.defaultBlockState();
		BlockState stone = Blocks.STONE.defaultBlockState();
		for (int x = siteX - 30; x <= siteX + 30; x++) {
			for (int z = -30; z <= 150; z++) {
				boolean wall = x == siteX - 30 || x == siteX + 30 || z == -30 || z == 150;
				for (int y = -63; y <= WATER_TOP - 1; y++) {
					world.setBlock(new BlockPos(x, y, z), wall ? stone : water, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
				}
			}
		}
	}

	void cleanup(ServerLevel world, int siteX) {
		List<Entity> remove = new ArrayList<>();
		for (Entity e : world.getAllEntities()) {
			if (e instanceof net.minecraft.world.entity.player.Player) {
				continue;
			}
			String ns = String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace());
			boolean near = Math.abs(e.getX() - siteX) < 600;
			if (near || ns.equals("tudursvehiclemod")) {
				remove.add(e);
			}
		}
		for (Entity e : remove) {
			e.discard();
		}
	}

	static double round(double d) {
		return Math.round(d * 1000.0) / 1000.0;
	}

	JsonObject state(AbstractVehicleEntity v) {
		JsonObject o = new JsonObject();
		if (v == null) {
			o.addProperty("missing", true);
			return o;
		}
		o.addProperty("removed", v.isRemoved());
		o.addProperty("x", round(v.getX()));
		o.addProperty("y", round(v.getY()));
		o.addProperty("z", round(v.getZ()));
		o.addProperty("yaw", round(v.getYRot()));
		o.addProperty("pitch", round(v.getXRot()));
		Vec3 vel = v.getDeltaMovement();
		o.addProperty("vx", round(vel.x));
		o.addProperty("vy", round(vel.y));
		o.addProperty("vz", round(vel.z));
		o.addProperty("onGround", v.onGround());
		o.addProperty("health", round(v.getHealth()));
		o.addProperty("maxHealth", round(v.getMaxHealth()));
		o.addProperty("fuel", round(v.getFuel()));
		o.addProperty("throttle", round(v.getThrottle()));
		o.addProperty("destroyed", v.tudursvehiclemod$isDestroyed());
		o.addProperty("passengers", v.getPassengers().size());
		return o;
	}
}
