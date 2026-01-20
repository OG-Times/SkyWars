package fun.ogtimes.skywars.spigot.abilities;

import fun.ogtimes.skywars.spigot.config.ConfigManager;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Iterator;

@Setter
@Getter
public class Ability {
   private String name;
   private AbilityType type;
   private boolean enabled;
   private HashMap<Integer, AbilityLevel> levels = new HashMap<>();

   public Ability(String var1, AbilityType var2) {
      this.name = var1;
      this.type = var2;
      this.enabled = ConfigManager.abilities.getBoolean("abilities." + var1 + ".enabled");

       for (String var4 : ConfigManager.abilities.getConfigurationSection("abilities." + var1 + ".level").getKeys(false)) {
           int var5 = Integer.parseInt(var4);
           int var6 = ConfigManager.abilities.getInt("abilities." + var1 + ".level." + var4 + ".chance");
           int var7 = ConfigManager.abilities.getInt("abilities." + var1 + ".level." + var4 + ".price");
           boolean var9 = false;
           int var8;
           if (ConfigManager.abilities.isSet("abilities." + var1 + ".level." + var4 + ".value")) {
               var9 = true;
               var8 = ConfigManager.abilities.getInt("abilities." + var1 + ".level." + var4 + ".value");
           } else {
               var8 = 0;
           }

           AbilityLevel var10 = new AbilityLevel(var5, var6, var7, var9, var8);
           this.addLevel(var5, var10);
       }

      AbilityManager.abilities.put(var1, this);
      AbilityManager.abilitiesbyType.put(var2, this);
   }

    public AbilityLevel getLevel(int var1) {
      return this.levels.containsKey(var1) ? this.levels.get(var1) : null;
   }

    public void addLevel(int var1, AbilityLevel var2) {
      this.levels.put(var1, var2);
   }

}
