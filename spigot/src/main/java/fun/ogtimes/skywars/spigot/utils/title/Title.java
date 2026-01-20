package fun.ogtimes.skywars.spigot.utils.title;

import com.google.common.base.Preconditions;
import java.util.Iterator;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

@Getter
public class Title {
   /** @deprecated */
   @Deprecated
   public static boolean DEBUG;
   private JSONObject title;
   private JSONObject subtitle;
   @Setter
   private int fadeIn;
   @Setter
   private int fadeOut;
   @Setter
   private int stay;

   public Title(String var1, int var2, int var3, int var4) {
      String[] var5 = var1.split("\n");
      this.title = convert(var5[0]);
      this.subtitle = convert(var5.length > 1 ? var5[1] : "");
      this.fadeIn = var2;
      this.fadeOut = var4;
      this.stay = var3;
   }

   public Title(String var1, String var2, int var3, int var4, int var5) {
      this.title = convert(var1);
      this.subtitle = convert(var2);
      this.fadeIn = var3;
      this.fadeOut = var5;
      this.stay = var4;
   }

   public Title(JSONObject var1, JSONObject var2, int var3, int var4, int var5) {
      this.title = var1;
      this.subtitle = var2;
      this.fadeIn = var3;
      this.fadeOut = var4;
      this.stay = var5;
   }

   static JSONObject convert(String var0) {
      JSONObject var1 = new JSONObject();
      var1.put("text", var0);
      return var1;
   }

   public void send(Player var1) {
      Preconditions.checkNotNull(var1);

      try {
         Object var2 = var1.getClass().getMethod("getHandle").invoke(var1);
         Object var3 = var2.getClass().getField("playerConnection").get(var2);
         Class var4 = ServerPackage.MINECRAFT.getClass("PacketPlayOutTitle");
         Class var5 = ServerPackage.MINECRAFT.getClass("Packet");
         Class var6 = ServerPackage.MINECRAFT.getClass("IChatBaseComponent");
         Class var7 = ServerPackage.MINECRAFT.getClass("IChatBaseComponent$ChatSerializer");
         Class var8 = ServerPackage.MINECRAFT.getClass("PacketPlayOutTitle$EnumTitleAction");
         Object var9 = var4.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(this.fadeIn, this.stay, this.fadeOut);
         var3.getClass().getMethod("sendPacket", var5).invoke(var3, var9);
         Object var10;
         Object var11;
         if (this.title != null && !this.title.isEmpty()) {
            var10 = var7.getMethod("a", String.class).invoke(null, this.title.toString());
            var11 = var4.getConstructor(var8, var6).newInstance(var8.getField("TITLE").get(null), var10);
            var3.getClass().getMethod("sendPacket", var5).invoke(var3, var11);
         }

         if (this.subtitle != null && !this.subtitle.isEmpty()) {
            var10 = var7.getMethod("a", String.class).invoke(null, this.subtitle.toString());
            var11 = var4.getConstructor(var8, var6).newInstance(var8.getField("SUBTITLE").get(null), var10);
            var3.getClass().getMethod("sendPacket", var5).invoke(var3, var11);
         }

      } catch (Throwable var12) {
         throw new RuntimeException(var12);
      }
   }

   public void sendToAll() {

       for (Player var2 : Bukkit.getOnlinePlayers()) {
           this.send(var2);
       }

   }

    public void setTitle(String var1) {
      this.title = convert(var1);
   }

   public void setTitle(JSONObject var1) {
      this.title = var1;
   }

    public void setSubtitle(String var1) {
      this.subtitle = convert(var1);
   }

   public void setSubtitle(JSONObject var1) {
      this.subtitle = var1;
   }

}
