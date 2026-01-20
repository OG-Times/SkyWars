package fun.ogtimes.skywars.spigot.commands.user;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdCoins implements BaseCommand {
    public void onCommand(CommandSender sender, String[] args) {
        Player var3 = null;
        SkyPlayer var4 = null;
        boolean var5 = false;
        if (sender instanceof Player) {
            var3 = (Player) sender;
            var4 = SkyWars.getSkyPlayer(var3);
            if (var4 == null) {
                return;
            }

            var5 = true;
        }

        if (!sender.hasPermission(this.getPermission())) {
            sender.sendMessage("§cYou don't have permission!");
        } else if (args.length == 0) {
            if (var5) {
                double var12 = var4.getCoins();
                var4.sendMessage("&aCoins: &e" + var12);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.help(sender)));
            }

        } else {
            if (args.length >= 1 && sender.hasPermission("skywars.admin.coins")) {
                String var6 = args[0].toLowerCase();
                byte var7 = -1;
                switch(var6.hashCode()) {
                    case -934610812:
                        if (var6.equals("remove")) {
                            var7 = 1;
                        }
                        break;
                    case 96417:
                        if (var6.equals("add")) {
                            var7 = 0;
                        }
                }

                String var8;
                Player var9;
                int var10;
                SkyPlayer var11;
                int var13;
                switch(var7) {
                    case 0:
                        if (args.length == 1) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins add <amount> [PlayerName]"));
                            return;
                        }

                        if (args.length == 2) {
                            if (var5) {
                                var13 = Integer.parseInt(args[1]);
                                SkyEconomyManager.addCoins(var4.getPlayer(), var13, false);
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins add <amount> [PlayerName]"));
                            }

                            return;
                        }

                        if (args.length == 3) {
                            var8 = args[2];
                            var9 = Bukkit.getPlayer(var8);
                            var10 = Integer.parseInt(args[1]);
                            if (var9 == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            var11 = SkyWars.getSkyPlayer(var9);
                            if (var11 == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            SkyEconomyManager.addCoins(var11.getPlayer(), var10, false);
                            return;
                        }
                        break;
                    case 1:
                        if (args.length == 1) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins remove <amount> [PlayerName]"));
                            return;
                        }

                        if (args.length == 2) {
                            if (var5) {
                                var13 = Integer.parseInt(args[1]);
                                SkyEconomyManager.removeCoins(var4.getPlayer(), var13);
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins remove <amount> [PlayerName]"));
                            }

                            return;
                        }

                        if (args.length == 3) {
                            var8 = args[2];
                            var9 = Bukkit.getPlayer(var8);
                            var10 = Integer.parseInt(args[1]);
                            if (var9 == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            var11 = SkyWars.getSkyPlayer(var9);
                            if (var11 == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            SkyEconomyManager.removeCoins(var11.getPlayer(), var10);
                        }
                }
            }

        }
    }

    public String help(CommandSender sender) {
        String var2 = "&a/sw coins &a- &bget coins \n";
        if (sender.hasPermission("skywars.admin.coins")) {
            var2 = var2 + "&a/sw coins &eadd <amount> [name] &a- &badd coins yourself/other\n&a/sw coins &eremove <amount> [name] &a- &bremove coins yourself/other";
        }

        return sender.hasPermission(this.getPermission()) ? var2 : "";
    }

    public String getPermission() {
        return "skywars.user";
    }

    public boolean console() {
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
