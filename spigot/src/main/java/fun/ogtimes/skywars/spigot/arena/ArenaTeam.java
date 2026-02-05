package fun.ogtimes.skywars.spigot.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Setter
@Getter
public class ArenaTeam {
   private int number;
   private int maximumSize;
   private Location base;
   private List<SkyPlayer> players = new ArrayList<>();
   private LinkedHashMap<Location, Boolean> spawns = new LinkedHashMap<>();
   private List<ArenaBox> cages = new ArrayList<>();

   public ArenaTeam(int number, int maxSize, Location base) {
      this.number = number;
      this.maximumSize = maxSize;
      this.base = base;
      this.loadSpawns();
      this.loadCages();
   }

    public Location getSpawnUsable() {
      Iterator var1 = this.spawns.entrySet().iterator();

      Entry var2;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         var2 = (Entry)var1.next();
      } while(!(Boolean)var2.getValue());

      return (Location)var2.getKey();
   }

   private void loadSpawns() {
      int[] var1 = this.factors(this.maximumSize);
      int var2 = var1[0];
      int var3 = var1[1];
      if (this.maximumSize == 5) {
         var2 = 3;
         var3 = 3;
      }

      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      int var7 = -1;
      int var8 = Math.max(var2, var3);
      int var9 = var8 * var8;

      for(int var10 = 0; var10 < var9; ++var10) {
         if (-var2 / 2 <= var4 && var4 <= var2 / 2 && -var3 / 2 <= var5 && var5 <= var3 / 2) {
            boolean var11 = this.maximumSize != 5 || (var10 != 2 && var10 != 4 && var10 != 6 && var10 != 8);

             if (var11) {
               this.spawns.put(this.base.clone().add(var4 * 5, 0.0D, var5 * 5), true);
            }
         }

         if (var4 == var5 || var4 < 0 && var4 == -var5 || var4 > 0 && var4 == 1 - var5) {
            var8 = var6;
            var6 = -var7;
            var7 = var8;
         }

         var4 += var6;
         var5 += var7;
      }

   }

   private void loadCages() {

       for (Location var2 : this.spawns.keySet()) {
           ArenaBox var3 = new ArenaBox(var2);
           var3.setBox(SkyWars.boxes.getInt("boxes." + SkyWars.boxes.getString("default") + ".item"), SkyWars.boxes.getInt("boxes." + SkyWars.boxes.getString("default") + ".data"));
           this.cages.add(var3);
       }

   }

   private int[] factors(int var1) {
      int[] var2 = new int[2];
      ArrayList var3 = new ArrayList();

      for(int var4 = 1; var4 <= var1; ++var4) {
         if (var1 % var4 == 0) {
            var3.add(var4);
         }
      }

      if (var3.size() == 1) {
         var2[0] = (Integer)var3.getFirst();
         var2[1] = 1;
      } else if (var3.size() == 2) {
         var2[0] = (Integer)var3.get(1);
         var2[1] = (Integer)var3.get(0);
      } else {
         HashMap var10 = new HashMap();

         int var5;
         for(var5 = 0; var5 < var3.size(); ++var5) {
            int var6 = (Integer)var3.get(var5);

             for (Object object : var3) {
                 int var8 = (Integer) object;
                 if (var6 * var8 == var1) {
                     Integer[] var9 = new Integer[]{var6, var8};
                     var10.put(var6 - var8, var9);
                 }
             }
         }

         var5 = Integer.MAX_VALUE;

          for (Object object : var10.entrySet()) {
              Entry var12 = (Entry) object;
              if ((Integer) var12.getKey() >= 0 && (Integer) var12.getKey() < var5) {
                  var5 = (Integer) var12.getKey();
              }
          }

         var2[0] = ((Integer[])var10.get(var5))[0];
         var2[1] = ((Integer[])var10.get(var5))[1];
      }

      return var2;
   }
}
