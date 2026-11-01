package com.example.tudursvehiclemod.compat.fabric.fabric.api.command.v2;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** Fired from compat.ForgeEvents (RegisterCommandsEvent). */
@FunctionalInterface
public interface CommandRegistrationCallback {
	void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment);

	Event<CommandRegistrationCallback> EVENT = new Event<>(l -> (d, r, e) -> l.forEach(x -> x.register(d, r, e)));
}
