package fun.ogtimes.skywars.spigot.server;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import fun.ogtimes.skywars.spigot.events.SkySignUpdateEvent;
import fun.ogtimes.skywars.spigot.events.enums.SkySignUpdateCause;
import fun.ogtimes.skywars.spigot.utils.Game;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class Server extends Game {
   public Server(String var1) {
      super(var1, "", 0, true, ArenaState.WAITING);
      this.getData(false);
   }

   public void getData(boolean var1) {
      PreparedStatement var2 = null;
      ResultSet var3 = null;

      try {
         Connection var4 = DatabaseHandler.getDS().getConnection();
         Throwable var5 = null;

         try {
            var2 = var4.prepareStatement(String.format("SELECT * FROM %s WHERE bungeeid=?", DatabaseHandler.getDS().TABLE_SERVER));
            var2.setString(1, this.name);
            var2.execute();
            var3 = var2.getResultSet();
            if (var3.next()) {
               ArrayList var6 = new ArrayList();
               String var7 = var3.getString("bungeeid");
               int var8 = var3.getInt("players");
               int var9 = var3.getInt("max_players");
               int var10 = var3.getInt("loading");
               int var11 = this.loading ? 1 : 0;
               String var12 = var3.getString("state");
               String var13 = var3.getString("map");
               if (this.alivePlayers != var8 || this.maxPlayers != var9) {
                  var6.add(SkySignUpdateCause.PLAYERS);
               }

               if (var11 != var10) {
                  var6.add(SkySignUpdateCause.LOADING);
               }

               if (!this.state.toString().equals(var12)) {
                  var6.add(SkySignUpdateCause.STATE);
               }

               if (!this.displayName.equals(var13)) {
                  var6.add(SkySignUpdateCause.MAP);
               }

               SkySignUpdateCause var14 = null;
               if (var6.size() == 1) {
                  var14 = (SkySignUpdateCause)var6.getFirst();
               }

               if (var6.size() >= 2) {
                  var14 = SkySignUpdateCause.ALL;
               }

               this.alivePlayers = var8;
               this.maxPlayers = var9;
               this.loading = var10 == 1;
               this.state = ArenaState.valueOf(var12);
               this.displayName = var13;
               if (!var6.isEmpty() && var1) {
                  SkySignUpdateCause finalVar1 = var14;
                  Bukkit.getScheduler().runTask(SkyWars.getPlugin(), () -> Bukkit.getServer().getPluginManager().callEvent(new SkySignUpdateEvent(var7, finalVar1)));
               }

               var2.close();
            }
         } catch (Throwable var32) {
            var5 = var32;
            throw var32;
         } finally {
            if (var4 != null) {
               if (var5 != null) {
                  try {
                     var4.close();
                  } catch (Throwable var31) {
                     var5.addSuppressed(var31);
                  }
               } else {
                  var4.close();
               }
            }

         }
      } catch (SQLException var34) {
         var34.printStackTrace();
      } finally {
         DatabaseHandler.getDS().close(var3);
         DatabaseHandler.getDS().close(var2);
      }

   }
}
