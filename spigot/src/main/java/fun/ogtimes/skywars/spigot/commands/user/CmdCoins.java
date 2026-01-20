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
    public void onCommand(CommandSender var1, String[] var2) {
        Player var3 = null;
        SkyPlayer var4 = null;
        boolean var5 = false;
        if (var1 instanceof Player) {
            var3 = (Player)var1;
            var4 = SkyWars.getSkyPlayer(var3);
            if (var4 == null) {
                return;
            }

            var5 = true;
        }

        if (!var1.hasPermission(this.getPermission())) {
            var1.sendMessage("§cYou don't have permission!");
        } else if (var2.length == 0) {
            if (var5) {
                double var12 = var4.getCoins();
                var4.sendMessage("&aCoins: &e" + var12);
            } else {
                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', this.help(var1)));
            }

        } else {
            if (var2.length >= 1 && var1.hasPermission("skywars.admin.coins")) {
                String var6 = var2[0].toLowerCase();
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
                        if (var2.length == 1) {
                            var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins add <amount> [PlayerName]"));
                            return;
                        }

                        if (var2.length == 2) {
                            if (var5) {
                                var13 = Integer.parseInt(var2[1]);
                                SkyEconomyManager.addCoins(var4.getPlayer(), var13, false);
                            } else {
                                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins add <amount> [PlayerName]"));
                            }

                            return;
                        }

                        if (var2.length == 3) {
                            var8 = var2[2];
                            var9 = Bukkit.getPlayer(var8);
                            var10 = Integer.parseInt(var2[1]);
                            if (var9 == null) {
                                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            var11 = SkyWars.getSkyPlayer(var9);
                            if (var11 == null) {
                                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            SkyEconomyManager.addCoins(var11.getPlayer(), var10, false);
                            return;
                        }
                        break;
                    case 1:
                        if (var2.length == 1) {
                            var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins remove <amount> [PlayerName]"));
                            return;
                        }

                        if (var2.length == 2) {
                            if (var5) {
                                var13 = Integer.parseInt(var2[1]);
                                SkyEconomyManager.removeCoins(var4.getPlayer(), var13);
                            } else {
                                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cType: /sw coins remove <amount> [PlayerName]"));
                            }

                            return;
                        }

                        if (var2.length == 3) {
                            var8 = var2[2];
                            var9 = Bukkit.getPlayer(var8);
                            var10 = Integer.parseInt(var2[1]);
                            if (var9 == null) {
                                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            var11 = SkyWars.getSkyPlayer(var9);
                            if (var11 == null) {
                                var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + var8 + " isn't online or not exists"));
                                return;
                            }

                            SkyEconomyManager.removeCoins(var11.getPlayer(), var10);
                        }
                }
            }

        }
    }

    public String help(CommandSender var1) {
        String var2 = "&a/sw coins &a- &bget coins \n";
        if (var1.hasPermission("skywars.admin.coins")) {
            var2 = var2 + "&a/sw coins &eadd <amount> [name] &a- &badd coins yourself/other\n&a/sw coins &eremove <amount> [name] &a- &bremove coins yourself/other";
        }

        return var1.hasPermission(this.getPermission()) ? var2 : "";
    }

    public String getPermission() {
        return "skywars.user";
    }

    public boolean console() {
        return true;
    }

    public List<String> onTabComplete(CommandSender var1, String[] var2) {
        return null;
    }
}
