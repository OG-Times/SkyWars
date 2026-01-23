package fun.ogtimes.skywars.spigot.commands;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.commands.acf.SkyWarsCommand;

import java.util.List;

/**
 * This code was made by jsexp, in case of any unauthorized
 * use, at least please leave credits.
 * Find more about me @ my <a href="https://github.com/hardcorefactions">GitHub</a> :D
 * © 2025 - jsexp
 */
public class CommandManager {
    private final SkyWars instance;
    private BukkitCommandManager manager;

    public CommandManager(SkyWars instance) {
        this.instance = instance;

        this.manager = new BukkitCommandManager(instance);
        this.manager.enableUnstableAPI("help");

        this.registerContexts();
        this.registerCompletions();
        this.registerCommands();
    }

    private void registerCompletions() {
        manager.getCommandCompletions().registerCompletion("arenas", context ->
                ArenaManager.getGames().stream().map(Arena::getName).toList());
    }

    private void registerContexts() {
        manager.getCommandContexts().registerContext(Arena.class, context -> {
            String name = context.popFirstArg();
            if (name == null) {
                return null;
            }

            Arena arena = ArenaManager.getGame(name);
            if (arena == null) {
                throw new InvalidCommandArgument("Arena '" + name + "' not found!", false);
            }

            return arena;
        });
    }

    private void registerCommands() {
        List.of(
                new SkyWarsCommand()
        ).forEach(command -> manager.registerCommand(command));
    }

}
