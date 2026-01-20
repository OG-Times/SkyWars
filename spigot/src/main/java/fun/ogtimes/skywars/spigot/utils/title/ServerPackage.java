package fun.ogtimes.skywars.spigot.utils.title;

import org.bukkit.Bukkit;

enum ServerPackage {
   MINECRAFT("net.minecraft.server." + getServerVersion()),
   CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion());

   private final String path;

   ServerPackage(String var3) {
      this.path = var3;
   }

   public static String getServerVersion() {
      return Bukkit.getServer().getClass().getPackage().getName().substring(23);
   }

   public String toString() {
      return this.path;
   }

   public Class<?> getClass(String var1) throws ClassNotFoundException {
      return Class.forName(this.toString() + "." + var1);
   }
}
