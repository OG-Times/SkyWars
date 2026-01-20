package fun.ogtimes.skywars.spigot.abilities;

import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.arena.chest.RandomItem;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class AbilityManager {
   public static final HashMap<String, Ability> abilities = new LinkedHashMap<>();
   public static final HashMap<AbilityType, Ability> abilitiesbyType = new HashMap<>();
   public static final List<RandomItem> treasureItems = new ArrayList<>();

   public static void initAbilities() {
      abilities.clear();
      abilitiesbyType.clear();
      treasureItems.clear();
      Iterator var0 = ConfigManager.abilities.getConfigurationSection("abilities").getKeys(false).iterator();

      while(true) {
         AbilityType var2;
         do {
            if (!var0.hasNext()) {
               return;
            }

            String var1 = (String)var0.next();
            var2 = AbilityType.valueOf(var1.toUpperCase());
            new Ability(var1, var2);
         } while(var2 != AbilityType.PIRATE);

          for (String var4 : ConfigManager.abilities.getStringList("abilities.pirate.treasures")) {
              String[] var5 = var4.split(" ");
              byte var6 = 100;
              int var7 = Integer.parseInt(var5[0]);
              int var8 = Integer.parseInt(var5[1]);
              String var9 = var5[2];
              ItemBuilder var10 = ChestTypeManager.readItem(var9, "Treasure Items");
              treasureItems.add(new RandomItem(var6, var7, var8, var10));
          }
      }
   }

   public static Ability[] getAbilities() {
      return abilities.values().toArray(new Ability[0]);
   }

   public static Ability[] getEnabledAbilities() {
      LinkedHashMap var0 = new LinkedHashMap();
      Ability[] var1 = getAbilities();
      int var2 = var1.length;

       for (Ability var4 : var1) {
           if (var4.isEnabled()) {
               var0.put(var4.getName(), var4);
           }
       }

      return (Ability[])var0.values().toArray(new Ability[0]);
   }

   public static List<String> getEnabledAbilitiesList() {
      ArrayList var0 = new ArrayList();
      Ability[] var1 = getAbilities();
      int var2 = var1.length;

       for (Ability var4 : var1) {
           if (var4.isEnabled()) {
               var0.add(var4.getName());
           }
       }

      return var0;
   }

   public static Ability getAbility(String var0) {
      return abilities.get(var0);
   }

   public static Ability getAbilityByType(AbilityType var0) {
      return abilitiesbyType.get(var0);
   }
}
