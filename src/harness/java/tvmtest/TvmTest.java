package tvmtest;

import com.example.tudursvehiclemod.asset.VehicleDefinition;
import com.example.tudursvehiclemod.asset.VehicleRegistry;
import com.example.tudursvehiclemod.asset.WeaponDefinition;
import com.example.tudursvehiclemod.entity.AbstractVehicleEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge test harness for tudursvehiclemod. Server commands (op not required):
 *   /tvmt spawn <def>            spawn a vehicle 10 blocks in front of the player, fuel and ammo filled
 *   /tvmt spawnat <x> <y> <z> <def>   same at a position (works from the console)
 *   /tvmt ride                   the player boards the last spawned vehicle as pilot
 *   /tvmt state                  logs "[tvmt] STATE {...}" for the player's vehicle (or the last spawned one)
 *   /tvmt run <name>             the Fabric harness's full scenario (see TvmRun) -> tvmtest-<name>.json
 *   /tvmt prep                   every loaded vehicle: ammo refilled, fuel set to 37%, weapon 0 fired 3 times (save test)
 *   /tvmt dump <tag>             logs "[tvmt] DUMP <tag> {...}" for every loaded vehicle (save/restart check)
 * The client half (TvmTestClient) scripts a flight when started with -Dtvmt.auto=<def>.
 */
@Mod("tvmtest")
public final class TvmTest {
	public static final Logger LOG = LoggerFactory.getLogger("tvmtest");
	static Entity last;

	static long tickStart, tickSum, tickMax, tickCount;

	public TvmTest() {
		MinecraftForge.EVENT_BUS.addListener(this::commands);
		// Server tick time while anyone rides a vehicle: "[tvmt] MSPT avg max" every 100 ticks (load investigation).
		MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.ServerTickEvent e) -> {
			if (e.phase == net.minecraftforge.event.TickEvent.Phase.START) {
				tickStart = System.nanoTime();
				return;
			}
			boolean riding = false;
			for (ServerPlayer p : e.getServer().getPlayerList().getPlayers()) {
				riding |= p.getVehicle() instanceof AbstractVehicleEntity;
			}
			if (!riding) {
				return;
			}
			long ns = System.nanoTime() - tickStart;
			tickSum += ns;
			tickMax = Math.max(tickMax, ns);
			if (++tickCount == 100) {
				LOG.info("[tvmt] MSPT avg={} max={}", round(tickSum / 100 / 1.0e6), round(tickMax / 1.0e6));
				tickSum = tickMax = tickCount = 0;
			}
		});
		if (System.getProperty("tvmt.trace") != null) {
			// per-tick server-side attitude of every player's vehicle (compare with the client's "TR CL" lines by game time)
			MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.ServerTickEvent e) -> {
				if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) {
					return;
				}
				for (ServerPlayer p : e.getServer().getPlayerList().getPlayers()) {
					if (p.getVehicle() instanceof AbstractVehicleEntity v) {
						LOG.info("[tvmt] TR SV {} y={} z={} yaw={} pitch={}", p.level().getGameTime(), round(v.getY()), round(v.getZ()), round(v.getYRot()), round(v.getXRot()));
					}
				}
			});
		}
		if (FMLEnvironment.dist == Dist.CLIENT && System.getProperty("tvmt.gui") != null) {
			TvmGuiClient.init();
		} else if (FMLEnvironment.dist == Dist.CLIENT && System.getProperty("tvmt.auto") != null) {
			TvmTestClient.init();
		}
	}

	/** Logs a failure with its stack trace (command errors otherwise only reach the player's chat). */
	static com.mojang.brigadier.Command<CommandSourceStack> safe(com.mojang.brigadier.Command<CommandSourceStack> body) {
		return c -> {
			try {
				return body.run(c);
			} catch (Exception ex) {
				LOG.error("[tvmt] command failed: " + c.getInput(), ex);
				reply(c, "FAILED " + ex);
				return 0;
			}
		};
	}

	void commands(RegisterCommandsEvent e) {
		e.getDispatcher().register(Commands.literal("tvmt").requires(s -> true)
				.then(Commands.literal("spawn").then(Commands.argument("def", StringArgumentType.greedyString()).executes(safe(c -> {
					ServerPlayer p = c.getSource().getPlayerOrException();
					Vec3 look = Vec3.directionFromRotation(0, p.getYRot());
					Vec3 pos = p.position().add(look.scale(10));
					last = spawn(p.serverLevel(), new ResourceLocation(StringArgumentType.getString(c, "def")), pos, p.getYRot());
					return reply(c, "spawned " + last);
				}))))
				.then(Commands.literal("spawnat").then(Commands.argument("x", DoubleArgumentType.doubleArg())
						.then(Commands.argument("y", DoubleArgumentType.doubleArg()).then(Commands.argument("z", DoubleArgumentType.doubleArg())
								.then(Commands.argument("def", StringArgumentType.greedyString()).executes(safe(c -> {
									Vec3 pos = new Vec3(DoubleArgumentType.getDouble(c, "x"), DoubleArgumentType.getDouble(c, "y"), DoubleArgumentType.getDouble(c, "z"));
									last = spawn(c.getSource().getLevel(), new ResourceLocation(StringArgumentType.getString(c, "def")), pos, 0f);
									return reply(c, "spawned " + last);
								})))))))
				.then(Commands.literal("run").then(Commands.argument("name", StringArgumentType.word())
						.executes(safe(c -> {
							TvmRun.INSTANCE.start(c.getSource().getServer(), StringArgumentType.getString(c, "name"), Integer.MAX_VALUE);
							return reply(c, "run started");
						}))))
				.then(Commands.literal("place").then(Commands.argument("x", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
						.then(Commands.argument("y", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
								.then(Commands.argument("z", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
										.then(Commands.argument("block", StringArgumentType.greedyString()).executes(safe(c -> {
											net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(
													com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "x"),
													com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "y"),
													com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "z"));
											var block = BuiltInRegistries.BLOCK.get(new ResourceLocation(StringArgumentType.getString(c, "block")));
											c.getSource().getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
											LOG.info("[tvmt] PLACE {} at {}", BuiltInRegistries.BLOCK.getKey(block), pos);
											return reply(c, "placed");
										})))))))
				.then(Commands.literal("home").executes(safe(c -> {
					// Back on the ground at the world spawn (a saved world may have the player mid-air or falling).
					ServerPlayer p = c.getSource().getPlayerOrException();
					p.stopRiding();
					p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
					p.fallDistance = 0;
					p.teleportTo(c.getSource().getLevel(), 10.5, -60, 1.5, 0f, 0f);
					return reply(c, "home");
				})))
				.then(Commands.literal("subsetup").then(Commands.argument("def", StringArgumentType.greedyString()).executes(safe(c -> {
					// Submarine test (same as the Fabric harness's /tvmtest subsetup): glass tank x -30..70, z -50..50, floor y -61,
					// water to y -26; the submarine at (10.5, -30, -40.5) yaw 0 with the player as pilot.
					ServerLevel lv = c.getSource().getLevel();
					ServerPlayer p = c.getSource().getPlayerOrException();
					p.stopRiding();
					var glass = net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState();
					var water = net.minecraft.world.level.block.Blocks.WATER.defaultBlockState();
					// The tank is kept between runs (building it is a load spike of its own); rebuilt only when missing.
					boolean built = lv.getBlockState(new net.minecraft.core.BlockPos(-30, -40, 0)).is(net.minecraft.world.level.block.Blocks.GLASS)
							&& lv.getBlockState(new net.minecraft.core.BlockPos(10, -40, 0)).is(net.minecraft.world.level.block.Blocks.WATER);
					for (int x = -30; x <= 70 && !built; x++) for (int z = -50; z <= 50; z++) for (int y = -61; y <= -26; y++) {
						boolean wall = x == -30 || x == 70 || z == -50 || z == 50 || y == -61;
						lv.setBlock(new net.minecraft.core.BlockPos(x, y, z), wall ? glass : water, 2);
					}
					for (Entity old : lv.getEntitiesOfClass(AbstractVehicleEntity.class, new net.minecraft.world.phys.AABB(-40, -70, -60, 80, 0, 60))) {
						if (old.getPassengers().isEmpty()) {
							old.discard();
						}
					}
					last = spawn(lv, new ResourceLocation(StringArgumentType.getString(c, "def")), new net.minecraft.world.phys.Vec3(10.5, -30, -40.5), 0f);
					p.teleportTo(lv, 10.5, -29, -40.5, 0f, 0f);
					LOG.info("[tvmt] SUBSETUP ride={}", p.startRiding(last, true));
					return reply(c, "subsetup");
				}))))
				.then(Commands.literal("give").then(Commands.argument("item", StringArgumentType.greedyString()).executes(safe(c -> {
					ServerPlayer p = c.getSource().getPlayerOrException();
					var item = BuiltInRegistries.ITEM.get(new ResourceLocation(StringArgumentType.getString(c, "item")));
					p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(item));
					LOG.info("[tvmt] GIVE {}", BuiltInRegistries.ITEM.getKey(item));
					return reply(c, "given");
				}))))
				.then(Commands.literal("firetest").then(Commands.argument("w", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
						.then(Commands.argument("def", StringArgumentType.greedyString()).executes(safe(c -> {
							FireTest.start(c.getSource().getLevel(), new ResourceLocation(StringArgumentType.getString(c, "def")),
									com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "w"));
							return reply(c, "firetest started");
						})))))
				.then(Commands.literal("prep").executes(safe(c -> {
					// For the save test: every loaded vehicle gets a distinctive fuel level and fires weapon 0 a few times.
					int n = 0;
					for (ServerLevel level : c.getSource().getServer().getAllLevels()) {
						for (Entity e2 : level.getAllEntities()) {
							if (e2 instanceof AbstractVehicleEntity av) {
								fillAmmo(av);
								setFuel(av, av.getMaxFuel() * 0.37f);
								for (int k = 0; k < 3 && !av.getDefinition().weapons().isEmpty(); k++) {
									av.tryFireWeapon(0, null);
								}
								n++;
							}
						}
					}
					return reply(c, "prepared " + n);
				})))
				.then(Commands.literal("ride").executes(safe(c -> {
					ServerPlayer p = c.getSource().getPlayerOrException();
					boolean ok = last != null && p.startRiding(last, true);
					LOG.info("[tvmt] RIDE {} vehicle={}", ok, p.getVehicle());
					return reply(c, "ride " + ok);
				})))
				.then(Commands.literal("state").executes(safe(c -> {
					Entity v = c.getSource().getEntity() != null && c.getSource().getEntity().getVehicle() != null ? c.getSource().getEntity().getVehicle() : last;
					JsonObject s = v instanceof AbstractVehicleEntity av ? state(av) : new JsonObject();
					LOG.info("[tvmt] STATE {}", s);
					return reply(c, s.toString());
				})))
				.then(Commands.literal("dump").then(Commands.argument("tag", StringArgumentType.word()).executes(safe(c -> {
					JsonArray all = new JsonArray();
					for (ServerLevel level : c.getSource().getServer().getAllLevels()) {
						for (Entity e2 : level.getAllEntities()) {
							if (e2 instanceof AbstractVehicleEntity av) {
								JsonObject s = state(av);
								CompoundTag tag = new CompoundTag();
								av.saveWithoutId(tag);
								s.addProperty("nbtKeys", String.join(",", new java.util.TreeSet<>(tag.getAllKeys())));
								s.addProperty("nbtSize", tag.toString().length());
								all.add(s);
							}
						}
					}
					LOG.info("[tvmt] DUMP {} {}", StringArgumentType.getString(c, "tag"), all);
					return reply(c, "dumped " + all.size());
				})))));
	}

	/** Replies go to the log (and to an RCON/console caller); the scripted client keeps its screen free of chat. */
	static int reply(CommandContext<CommandSourceStack> c, String msg) {
		if (!(c.getSource().getEntity() instanceof ServerPlayer)) {
			c.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(msg), false);
		}
		return 1;
	}

	static Entity spawn(ServerLevel level, ResourceLocation id, Vec3 pos, float yaw) {
		VehicleDefinition def = VehicleRegistry.get(id).orElseThrow(() -> new IllegalArgumentException("no vehicle " + id));
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(def.entityType());
		Entity e = type.create(level);
		if (e instanceof AbstractVehicleEntity av) {
			av.setVehicleDefinitionId(id);
		}
		e.moveTo(pos.x, pos.y, pos.z, yaw, 0f);
		level.addFreshEntity(e);
		if (e instanceof AbstractVehicleEntity av) {
			fillFuel(av);
			fillAmmo(av);
		}
		LOG.info("[tvmt] SPAWN {} type={} at {}", id, BuiltInRegistries.ENTITY_TYPE.getKey(type), pos);
		return e;
	}

	static JsonObject state(AbstractVehicleEntity v) {
		JsonObject o = new JsonObject();
		o.addProperty("uuid", v.getUUID().toString());
		o.addProperty("def", String.valueOf(v.getVehicleDefinitionId()));
		o.addProperty("type", String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(v.getType())));
		o.addProperty("x", round(v.getX()));
		o.addProperty("y", round(v.getY()));
		o.addProperty("z", round(v.getZ()));
		Vec3 m = v.getDeltaMovement();
		o.addProperty("speed", round(m.length() * 20.0));
		o.addProperty("vy", round(m.y * 20.0));
		o.addProperty("yaw", round(v.getYRot()));
		o.addProperty("pitch", round(v.getXRot()));
		o.addProperty("roll", round(v.getRoll(1.0f)));
		o.addProperty("throttle", round(v.getThrottle()));
		o.addProperty("fuel", round(v.getFuel()));
		o.addProperty("onGround", v.onGround());
		o.addProperty("passengers", v.getPassengers().size());
		o.addProperty("health", round(v.getHealth()));
		JsonArray ammo = new JsonArray();
		for (int i = 0; i < v.getDefinition().weapons().size(); i++) {
			ammo.add(v.tudursvehiclemod$getWeaponMagazineAmmo(i) + "/" + v.tudursvehiclemod$getWeaponReserveAmmo(i));
		}
		o.add("ammo", ammo);
		List<String> nearby = new ArrayList<>();
		for (Entity p : v.level().getEntities(v, v.getBoundingBox().inflate(200), e -> e instanceof net.minecraft.world.entity.projectile.Projectile)) {
			nearby.add(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(p.getType())));
		}
		o.addProperty("projectilesNearby", nearby.size());
		return o;
	}

	static double round(double d) {
		return Math.round(d * 100.0) / 100.0;
	}

	static void fillFuel(AbstractVehicleEntity v) {
		setFuel(v, v.getMaxFuel());
	}

	@SuppressWarnings("unchecked")
	static void setFuel(AbstractVehicleEntity v, float fuel) {
		try {
			java.lang.reflect.Field f = AbstractVehicleEntity.class.getDeclaredField("FUEL");
			f.setAccessible(true);
			v.getEntityData().set((EntityDataAccessor<Float>) f.get(null), fuel);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/** Vehicles spawn with empty magazines and resupply needs a seated player; fill them directly. */
	static void fillAmmo(AbstractVehicleEntity v) {
		try {
			VehicleDefinition def = v.getDefinition();
			java.lang.reflect.Method size = AbstractVehicleEntity.class.getDeclaredMethod("tudursvehiclemod$ensureWeaponAmmoArraysSized", VehicleDefinition.class);
			size.setAccessible(true);
			size.invoke(v, def);
			java.lang.reflect.Field ammo = AbstractVehicleEntity.class.getDeclaredField("weaponAmmo");
			java.lang.reflect.Field reserve = AbstractVehicleEntity.class.getDeclaredField("weaponReserveAmmo");
			ammo.setAccessible(true);
			reserve.setAccessible(true);
			int[] a = (int[]) ammo.get(v);
			int[] r = (int[]) reserve.get(v);
			for (int i = 0; i < def.weapons().size() && i < a.length; i++) {
				WeaponDefinition w = def.weapons().get(i);
				a[i] = Math.max(1, w.magazineSize());
				if (r[i] >= 0) {
					r[i] = Math.max(0, w.maxAmmo() - a[i]);
				}
			}
			v.tudursvehiclemod$syncWeaponAmmo();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
