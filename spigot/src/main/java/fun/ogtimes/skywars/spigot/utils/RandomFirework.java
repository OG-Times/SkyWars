package fun.ogtimes.skywars.spigot.utils;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class RandomFirework {
   private static final ArrayList<Color> colors = new ArrayList<>();
   private static final ArrayList<Type> types = new ArrayList<>();
   private static Random random = null;

   private static void loadColors() {
      colors.add(Color.WHITE);
      colors.add(Color.PURPLE);
      colors.add(Color.RED);
      colors.add(Color.GREEN);
      colors.add(Color.AQUA);
      colors.add(Color.BLUE);
      colors.add(Color.FUCHSIA);
      colors.add(Color.GRAY);
      colors.add(Color.LIME);
      colors.add(Color.MAROON);
      colors.add(Color.YELLOW);
      colors.add(Color.SILVER);
      colors.add(Color.TEAL);
      colors.add(Color.ORANGE);
      colors.add(Color.OLIVE);
      colors.add(Color.NAVY);
      colors.add(Color.BLACK);
   }

   private static void loadTypes() {
      types.add(Type.BURST);
      types.add(Type.BALL);
      types.add(Type.BALL_LARGE);
      types.add(Type.CREEPER);
      types.add(Type.STAR);
   }

   public static void loadFireworks() {
      random = new Random();
      loadColors();
      loadTypes();
   }

   private static Type getRandomType() {
      int var0 = types.size();
       return (Type)types.get(random.nextInt(var0));
   }

   private static Color getRandomColor() {
      int var0 = colors.size();
       return (Color)colors.get(random.nextInt(var0));
   }

   public static void launchRandomFirework(Location var0) {
      Firework var1 = var0.getWorld().spawn(var0, Firework.class);
      FireworkMeta var2 = var1.getFireworkMeta();
      var2.setPower(1);
      var2.addEffects(FireworkEffect.builder().flicker(true).with(getRandomType()).withColor(getRandomColor(), getRandomColor()).withFade(getRandomColor()).build());
      var1.setFireworkMeta(var2);
   }
}
