package fun.ogtimes.skywars.spigot.kit;

import fun.ogtimes.skywars.spigot.SkyWars;
import java.io.File;
import java.util.HashMap;

public class KitManager {
   public static final HashMap<String, Kit> kits = new HashMap<>();

   public static void initKits() {
      kits.clear();
      File var0 = new File(SkyWars.getPlugin().getDataFolder(), SkyWars.kits + File.separator);
      if (var0.exists() && var0.isDirectory()) {
         File[] var1 = var0.listFiles();
         int var2 = var1.length;

          for (File var4 : var1) {
              if (var4.getName().contains(".yml")) {
                  String var5 = var4.getName().replace(".yml", "");
                  new Kit(var5);
              }
          }
      }

   }

   public static Kit[] getKits() {
      return kits.values().toArray(new Kit[0]);
   }

   public static Kit getKit(String var0) {
      return kits.get(var0);
   }
}
