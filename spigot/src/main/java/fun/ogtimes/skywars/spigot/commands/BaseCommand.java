package fun.ogtimes.skywars.spigot.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface BaseCommand {
   void onCommand(CommandSender sender, String[] args);

   List<String> onTabComplete(CommandSender sender, String[] args);

   String help(CommandSender sender);

   String getPermission();

   boolean console();
}
