package tvmtest;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/**
 * Opens every screen of the mod through the normal client paths, active with -Dtvmt.gui=1: right-clicks each of the five
 * blocks (placed by /tvmt place), uses a tiered spawner item (vehicle select screen), and opens the vehicle menu with its key
 * while riding. Takes screenshots/tvmt_gui_*.png and logs "[tvmt] GUI <name> -> <screen class>", then quits.
 */
final class TvmGuiClient {
	private static final String[] BLOCKS = {"fuel_refiner", "station", "drone_center", "vehicle_converter", "fire_support_center"};
	private static int ticks = -1;
	private static BlockPos target;

	private TvmGuiClient() {
	}

	static void init() {
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
			if (e.phase == TickEvent.Phase.END) {
				tick(Minecraft.getInstance());
			}
		});
	}

	static void logScreen(Minecraft mc, String name) {
		TvmTest.LOG.info("[tvmt] GUI {} -> {}", name, mc.screen == null ? "none" : mc.screen.getClass().getName());
		TvmTestClient.shot(mc, "gui_" + name);
	}

	static void close(Minecraft mc) {
		if (mc.screen != null) {
			if (mc.player.containerMenu != mc.player.inventoryMenu) {
				mc.player.closeContainer();
			} else {
				mc.setScreen(null);
			}
		}
	}

	static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			return;
		}
		ticks++;
		int t = ticks - 100;
		if (t == -40) {
			TvmTestClient.cmd(mc, "tvmt home");
		}
		if (t < 0) {
			return;
		}
		if (t < 80 * BLOCKS.length) {
			int k = t / 80;
			int s = t % 80;
			if (s == 0) {
				target = mc.player.blockPosition().relative(mc.player.getDirection(), 2);
				TvmTestClient.cmd(mc, "tvmt place " + target.getX() + " " + target.getY() + " " + target.getZ() + " tudursvehiclemod:" + BLOCKS[k]);
			} else if (s == 15) {
				BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target).add(0, 0.5, 0), Direction.UP, target, false);
				TvmTest.LOG.info("[tvmt] use {} -> {}", BLOCKS[k], mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit));
			} else if (s == 50) {
				logScreen(mc, BLOCKS[k]);
			} else if (s == 60) {
				close(mc);
				TvmTestClient.cmd(mc, "tvmt place " + target.getX() + " " + target.getY() + " " + target.getZ() + " minecraft:air");
			}
			return;
		}
		t -= 80 * BLOCKS.length;
		if (t == 0) {
			TvmTestClient.cmd(mc, "tvmt give tudursvehiclemod:aircraft_spawner_t1");
		} else if (t == 15) {
			TvmTest.LOG.info("[tvmt] use spawner -> {}", mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND));
		} else if (t == 45) {
			logScreen(mc, "vehicle_select");
		} else if (t == 55) {
			close(mc);
			TvmTestClient.cmd(mc, "tvmt give minecraft:air");
		} else if (t == 70) {
			TvmTestClient.cmd(mc, "tvmt spawn ww2fict:a_fighter");
		} else if (t == 90) {
			TvmTestClient.cmd(mc, "tvmt ride");
		} else if (t == 130) {
			KeyMapping menu = TvmTestClient.key(mc, "key.tudursvehiclemod.open_vehicle_menu");
			if (menu != null) {
				KeyMapping.click(menu.getKey());
			}
		} else if (t == 160) {
			logScreen(mc, "vehicle_menu");
		} else if (t == 170) {
			close(mc);
		} else if (t == 200) {
			TvmTest.LOG.info("[tvmt] CLIENT DONE");
			mc.stop();
		}
	}
}
