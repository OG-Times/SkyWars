package fun.ogtimes.skywars.spigot.listener;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class TrailListener implements Listener {
    @EventHandler
    public void trail(EntityShootBowEvent var1) {
        LivingEntity var2 = var1.getEntity();
        Entity var3 = var1.getProjectile();
        if (var3 instanceof Projectile) {
            Projectile var4 = (Projectile)var3;
            if (var4 instanceof Arrow && var2 instanceof Player) {
                Player var5 = (Player)var2;
                SkyPlayer var6 = SkyWars.getSkyPlayer(var5);
                if (var6.isInArena()) {
                    Arena var7 = var6.getArena();
                    if (var7.getState() == ArenaState.INGAME && var6.hasTrail()) {
                        this.trail(var6.getTrail(), var4);
                    }
                }
            }
        }

    }

    private void trail(String trail, Projectile projectile) {
        if (!projectile.isOnGround() && !projectile.isDead()) {
            Location location = projectile.getLocation();
            Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
                Effect effect = Effect.getByName(trail);
                if (effect == null) {
                    String trailHashCoded = trail.toUpperCase();
                    byte var6 = -1;
                    switch(trailHashCoded.hashCode()) {
                        case 2402290:
                            if (trailHashCoded.equals("NOTE")) {
                                var6 = 5;
                            }
                            break;
                        case 15786612:
                            if (trailHashCoded.equals("REDSTONE")) {
                                var6 = 4;
                            }
                            break;
                        case 62462639:
                            if (trailHashCoded.equals("WATER_SPLASH")) {
                                var6 = 2;
                            }
                            break;
                        case 66975507:
                            if (trailHashCoded.equals("FLAME")) {
                                var6 = 1;
                            }
                            break;
                        case 78988968:
                            if (trailHashCoded.equals("SLIME")) {
                                var6 = 0;
                            }
                            break;
                        case 1272362666:
                            if (trailHashCoded.equals("DRIP_LAVA")) {
                                var6 = 3;
                            }
                    }

                    effect = switch (var6) {
                        case 0 -> Effect.SLIME;
                        case 1 -> Effect.FLAME;
                        case 2 -> Effect.WATERDRIP;
                        case 3 -> Effect.LAVADRIP;
                        case 4 -> Effect.COLOURED_DUST;
                        case 5 -> Effect.NOTE;
                        default -> effect;
                    };
                }

                if (effect == null) {
                    return;
                }

                location.getWorld().spigot().playEffect(location, effect, 0, 0, 0.1F, 0.1F, 0.1F, 0.3F, 5, 100);

                this.trail(trail, projectile);
            }, 1L);
        }
    }
}
