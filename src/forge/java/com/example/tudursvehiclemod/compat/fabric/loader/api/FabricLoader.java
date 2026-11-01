package com.example.tudursvehiclemod.compat.fabric.loader.api;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Forge stand-in for the parts of FabricLoader this mod uses: game/config dirs, loaded mods and their files. */
public final class FabricLoader {
	private static final FabricLoader INSTANCE = new FabricLoader();

	public static FabricLoader getInstance() {
		return INSTANCE;
	}

	public Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}

	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	/** A loaded mod's files, as Fabric's ModContainer exposes them. */
	public interface ModContainer {
		List<Path> getRootPaths();

		Optional<Path> findPath(String file);
	}

	public Collection<ModContainer> getAllMods() {
		List<ModContainer> out = new ArrayList<>();
		for (var info : ModList.get().getModFiles()) {
			var file = info.getFile();
			out.add(new ModContainer() {
				@Override
				public List<Path> getRootPaths() {
					return List.of(file.findResource("."));
				}

				@Override
				public Optional<Path> findPath(String path) {
					Path p = file.findResource(path.split("/"));
					return java.nio.file.Files.exists(p) ? Optional.of(p) : Optional.empty();
				}
			});
		}
		return out;
	}
}
