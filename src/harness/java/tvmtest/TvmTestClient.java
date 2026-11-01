package tvmtest;

import com.example.tudursvehiclemod.entity.AbstractVehicleEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/**
 * Scripted flight, active only with -Dtvmt.auto=<vehicle definition id>: once in a world it spawns that
 * vehicle, boards it, holds throttle, pulls the nose up with mouse-style turn() input, fires the selected
 * weapon, takes screenshots (screenshots/tvmt_*.png) and asks the server to log state, then quits.
 */
final class TvmTestClient {
	private static int ticks = -1;

	private TvmTestClient() {
	}

	static void init() {
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
			if (e.phase == TickEvent.Phase.END) {
				tick(Minecraft.getInstance());
			}
		});
	}

	static KeyMapping key(Minecraft mc, String name) {
		for (KeyMapping k : mc.options.keyMappings) {
			if (k.getName().equals(name)) {
				return k;
			}
		}
		return null;
	}

	static void cmd(Minecraft mc, String c) {
		TvmTest.LOG.info("[tvmt] client> /{}", c);
		mc.player.connection.sendCommand(c);
	}

	static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, "tvmt_" + name + ".png", mc.getMainRenderTarget(), msg -> TvmTest.LOG.info("[tvmt] screenshot {}", name));
	}

	static void clientState(Minecraft mc, String tag) {
		if (mc.player.getVehicle() instanceof AbstractVehicleEntity v) {
			TvmTest.LOG.info("[tvmt] CLIENT {} pos=({}, {}, {}) yaw={} pitch={} roll={} cam=({}, {}) camPos={}", tag,
					TvmTest.round(v.getX()), TvmTest.round(v.getY()), TvmTest.round(v.getZ()),
					TvmTest.round(v.getYRot()), TvmTest.round(v.getXRot()), TvmTest.round(v.getRoll(1.0f)),
					TvmTest.round(mc.gameRenderer.getMainCamera().getYRot()), TvmTest.round(mc.gameRenderer.getMainCamera().getXRot()),
					mc.gameRenderer.getMainCamera().getPosition());
		} else {
			TvmTest.LOG.info("[tvmt] CLIENT {} not riding (vehicle={})", tag, mc.player.getVehicle());
		}
	}

	static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			return;
		}
		ticks++;
		String def = System.getProperty("tvmt.auto");
		KeyMapping fire = key(mc, "key.tudursvehiclemod.fire_weapon");
		// The first ~5 s after joining are mostly the terrain loading screen.
		int t = ticks - 100;
		if (def != null && def.startsWith("wreck:")) {
			// wreck:<height>:<vehicle id> - see TvmTest's /tvmt wreck
			// client-side view of every vehicle in the tank: "[tvmt] WC <x> <t> y sinking" (compare with the server's WR lines)
			for (net.minecraft.world.entity.Entity ce : mc.level.entitiesForRendering()) {
				if (ce instanceof AbstractVehicleEntity cv && t >= 20 && t <= 260) {
					TvmTest.LOG.info("[tvmt] WC x={} {} y={} sinking={}", TvmTest.round(cv.getX()), t, TvmTest.round(cv.getY()), cv.tudursvehiclemod$isSinking());
				}
			}
			// wreck test: six vehicle types dropped 10 blocks above the water and destroyed (the ship floating first);
			// "wreck:intact" drops the same six without destroying them, for comparison
			String delayOverride = def.endsWith("intact") ? " 1000 " : null;
			if (t == 10) {
				cmd(mc, "tvmt clearvehicles");
			}
			if (t == 20 && delayOverride != null) {
				for (String cm : new String[] {"-20.5 10 X ww2fict:a_fighter", "-5.5 10 X tudur_sample:sample_helicopter", "10.5 10 X tudur_sample:sample_vtol",
						"25.5 10 X tudur_sample:sample_halftrack", "40.5 10 X ww2fict:a_floatfighter", "55.5 0 X tudur_sample:sample_ship"}) {
					cmd(mc, "tvmt wreck " + cm.replace(" X ", delayOverride));
				}
			} else if (t == 20) {
				cmd(mc, "tvmt wreck -20.5 10 0 ww2fict:a_fighter");
				cmd(mc, "tvmt wreck -5.5 10 0 tudur_sample:sample_helicopter");
				cmd(mc, "tvmt wreck 10.5 10 0 tudur_sample:sample_vtol");
				cmd(mc, "tvmt wreck 25.5 10 0 tudur_sample:sample_halftrack");
				cmd(mc, "tvmt wreck 40.5 10 0 ww2fict:a_floatfighter");
				cmd(mc, "tvmt wreck 55.5 0 80 tudur_sample:sample_ship");
			} else if (t == 300) {
				TvmTest.LOG.info("[tvmt] CLIENT DONE");
				mc.stop();
			}
			return;
		}
		if (def != null && def.contains("submarine")) {
			subTick(mc, def, t);
			return;
		}
		if (t == 20) {
			cmd(mc, "tvmt home");
		} else if (t == 60) {
			cmd(mc, "tvmt spawn " + def);
		} else if (t == 75) {
			shot(mc, "00_parked");
		} else if (t == 80) {
			cmd(mc, "tvmt ride");
		} else if (t == 130) {
			clientState(mc, "seated");
			shot(mc, "01_seated_fp");
			cmd(mc, "tvmt state");
		} else if (t == 140) {
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		} else if (t == 150) {
			shot(mc, "02_seated_tp");
			mc.options.setCameraType(CameraType.FIRST_PERSON);
		}
		// Full throttle from 160 on.
		if (t == 160) {
			mc.options.keyUp.setDown(true);
		}
		// Nose up: the same Entity.turn() the mouse handler calls (PlayerLookRateMixin turns it into aircraft input).
		if (t >= 400 && t < 440) {
			mc.player.turn(0.0, -25.0);
		}
		// Turn: mouse X, then the strafe key (A).
		if (t >= 690 && t < 702) {
			mc.player.turn(25.0, 0.0);
		} else if (t == 702) {
			clientState(mc, "mouseX");
		} else if (t == 705) {
			mc.options.keyLeft.setDown(true);
		} else if (t == 725) {
			clientState(mc, "keyA");
			shot(mc, "07_turn_fp");
			cmd(mc, "tvmt state");
		} else if (t == 728) {
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		} else if (t == 738) {
			shot(mc, "08_turn_tp");
			mc.options.keyLeft.setDown(false);
			clientState(mc, "keyA2");
		}
		if (t > 160 && t % 40 == 0 && t <= 760) {
			clientState(mc, "t" + t);
			cmd(mc, "tvmt state");
		}
		if (t == 520) {
			shot(mc, "03_climb_fp");
		} else if (t == 530) {
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		} else if (t == 545) {
			shot(mc, "04_climb_tp");
		} else if (t == 560) {
			if (fire != null) {
				fire.setDown(true);
			}
		} else if (t == 575) {
			shot(mc, "05_firing_tp");
		} else if (t == 600) {
			if (fire != null) {
				fire.setDown(false);
			}
			cmd(mc, "tvmt state");
		} else if (t == 620) {
			mc.options.setCameraType(CameraType.FIRST_PERSON);
		} else if (t == 640) {
			if (fire != null) {
				fire.setDown(true);
			}
		} else if (t == 650) {
			shot(mc, "06_firing_fp");
		} else if (t == 680) {
			if (fire != null) {
				fire.setDown(false);
			}
		} else if (t == 780) {
			cmd(mc, "tvmt dump flight");
		} else if (t == 800) {
			TvmTest.LOG.info("[tvmt] CLIENT DONE");
			mc.stop();
		}
	}

	static void hold(Minecraft mc, String name, boolean down) {
		KeyMapping k = key(mc, name);
		if (k == null) {
			TvmTest.LOG.info("[tvmt] no key {}", name);
			return;
		}
		k.setDown(down);
	}

	static void tap(Minecraft mc, String name) {
		KeyMapping k = key(mc, name);
		if (k != null) {
			// consumeClick() sees a click, isDown() a held key: give both
			KeyMapping.click(k.getKey());
		}
	}

	/** Submarine: surface run, dive, descend, turn, pitch, ascend; client vs server state every 20 ticks, screenshots in both views. */
	static net.minecraft.world.entity.Entity lastVehicle;

	static void subTick(Minecraft mc, String def, int t) {
		// Timeline shared with the Fabric harness (vt/harness TvmTestClient.subTick).
		if (System.getProperty("tvmt.trace") != null && mc.player.getVehicle() instanceof AbstractVehicleEntity v) {
			TvmTest.LOG.info("[tvmt] TR CL {} t={} y={} z={} yaw={} pitch={}", mc.level.getGameTime(), t, TvmTest.round(v.getY()), TvmTest.round(v.getZ()), TvmTest.round(v.getYRot()), TvmTest.round(v.getXRot()));
		}
		if (t == 20) {
			cmd(mc, "tvmt subsetup " + def);
		} else if (t == 120) {
			mc.options.keyUp.setDown(true);
		} else if (t == 160) {
			hold(mc, "key.tudursvehiclemod.hatch_toggle", true);
			tap(mc, "key.tudursvehiclemod.hatch_toggle");
		} else if (t == 162) {
			hold(mc, "key.tudursvehiclemod.hatch_toggle", false);
		} else if (t == 200) {
			hold(mc, "key.tudursvehiclemod.descend", true);
		} else if (t == 240) {
			hold(mc, "key.tudursvehiclemod.descend", false);
		} else if (t == 330) {
			mc.options.keyJump.setDown(true);
		} else if (t == 370) {
			mc.options.keyJump.setDown(false);
		} else if (t == 380) {
			mc.options.keyLeft.setDown(true);
		} else if (t == 420) {
			mc.options.keyLeft.setDown(false);
		} else if (t == 440) {
			hold(mc, "key.tudursvehiclemod.level_ascend", true);
		} else if (t == 540) {
			hold(mc, "key.tudursvehiclemod.level_ascend", false);
		}
		// Fire while submerged (300-330) and after surfacing (545-560); dismount at 580 and watch the hull settle.
		if (t == 300 || t == 545) {
			hold(mc, "key.tudursvehiclemod.fire_weapon", true);
		} else if (t == 330 || t == 560) {
			hold(mc, "key.tudursvehiclemod.fire_weapon", false);
		}
		if (t == 298 || t == 332 || t == 543 || t == 562) {
			clientState(mc, "fire" + t);
			cmd(mc, "tvmt state");
		}
		if (t == 578) {
			lastVehicle = mc.player.getVehicle();
			mc.options.keyUp.setDown(false);
		} else if (t == 580) {
			mc.options.keyShift.setDown(true);
		} else if (t == 584) {
			mc.options.keyShift.setDown(false);
		}
		if (t >= 576 && t <= 640 && lastVehicle != null) {
			TvmTest.LOG.info("[tvmt] DISMOUNT t={} riding={} hull=({}, {}, {}) yaw={}", t, mc.player.getVehicle() != null,
					TvmTest.round(lastVehicle.getX()), TvmTest.round(lastVehicle.getY()), TvmTest.round(lastVehicle.getZ()), TvmTest.round(lastVehicle.getYRot()));
		}
		if (t == 600 || t == 640) {
			cmd(mc, "tvmt dump d" + t);
		}
		if (t > 20 && t % 20 == 0 && t <= 560) {
			clientState(mc, "s" + t);
			cmd(mc, "tvmt state");
		}
		if (t == 280 || t == 350 || t == 400) {
			shot(mc, "sub" + t + "_fp");
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		} else if (t == 285 || t == 355 || t == 405) {
			shot(mc, "sub" + t + "_tp");
			mc.options.setCameraType(CameraType.FIRST_PERSON);
		} else if (t == 660) {
			TvmTest.LOG.info("[tvmt] CLIENT DONE");
			mc.stop();
		}
	}
}
