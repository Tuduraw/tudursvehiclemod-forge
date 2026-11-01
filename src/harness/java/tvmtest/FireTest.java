package tvmtest;

import com.example.tudursvehiclemod.entity.AbstractVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/** /tvmt firetest <weapon> <def>: spawns the vehicle at x=50000 on the ground, then for 60 ticks tries to fire that weapon at a
 * point 40 blocks ahead, logging "[tvmt] FT tick result cooldown magazine reserve pitch y" every tick. */
final class FireTest {
	private static AbstractVehicleEntity v;
	private static int weapon;
	private static int tick = -1;
	private static boolean registered;

	private FireTest() {
	}

	static void start(ServerLevel level, ResourceLocation def, int w) {
		if (!registered) {
			registered = true;
			MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent e) -> {
				if (e.phase == TickEvent.Phase.END) {
					tick();
				}
			});
		}
		for (int cx = 3123; cx <= 3127; cx++) {
			for (int cz = -2; cz <= 4; cz++) {
				level.setChunkForced(cx, cz, true);
			}
		}
		v = (AbstractVehicleEntity) TvmTest.spawn(level, def, new Vec3(50000.5, -60, 0.5), 0f);
		TvmTest.fillAmmo(v);
		weapon = w;
		tick = -20;
	}

	static void tick() {
		if (v == null || tick >= 60) {
			return;
		}
		tick++;
		if (tick < 0) {
			return;
		}
		boolean ok = v.tudursvehiclemod$tryFireWeaponAt(weapon, v.position().add(0, 1, 40));
		int cd = -1;
		try {
			java.lang.reflect.Field f = AbstractVehicleEntity.class.getDeclaredField("weaponCooldowns");
			f.setAccessible(true);
			cd = ((int[]) f.get(v))[weapon];
		} catch (ReflectiveOperationException e) {
			cd = -2;
		}
		TvmTest.LOG.info("[tvmt] FT {} {} age={} cd={} mag={} res={} pitch={} y={} hp={}", tick, ok, v.tickCount, cd,
				v.tudursvehiclemod$getWeaponMagazineAmmo(weapon), v.tudursvehiclemod$getWeaponReserveAmmo(weapon),
				TvmTest.round(v.getXRot()), TvmTest.round(v.getY()), TvmTest.round(v.getHealth()));
		if (tick == 60) {
			v.discard();
			v = null;
		}
	}
}
