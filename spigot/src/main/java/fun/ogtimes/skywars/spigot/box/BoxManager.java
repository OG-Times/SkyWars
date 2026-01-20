package fun.ogtimes.skywars.spigot.box;

import fun.ogtimes.skywars.spigot.SkyWars;
import java.util.HashMap;
import java.util.Iterator;

public class BoxManager {
   public static final HashMap<String, Box> boxes = new HashMap<>();

   public static void initBoxes() {
      boxes.clear();

       for (String var1 : SkyWars.boxes.getConfigurationSection("boxes").getKeys(false)) {
           Box var2 = new Box(SkyWars.boxes.getString("boxes." + var1 + ".name"), SkyWars.boxes.getString("boxes." + var1 + ".desc"));
           var2.setItem(SkyWars.boxes.getInt("boxes." + var1 + ".item"));
           var2.setData(SkyWars.boxes.getInt("boxes." + var1 + ".data"));
           var2.setSlot(SkyWars.boxes.getInt("boxes." + var1 + ".slot"));
           var2.setSection(var1);
           boxes.put(var1, var2);
       }

   }

   public static Box getDefaultBox() {
      return boxes.get(SkyWars.boxes.getString("default"));
   }

   public static Box[] getBoxes() {
      return boxes.values().toArray(new Box[0]);
   }

   public static Box getBox(String var0) {
      return boxes.get(var0);
   }
}
