package ak.CookLoco.SkyWars.utils.economy;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.config.ConfigManager;
import ak.CookLoco.SkyWars.utils.MSG;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.CoinsEconomy;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.CraftconomyUtils;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.CustomEconomy;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.PlayerPointsUtils;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.VaultUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SkyEconomyManager {
   private static SkyEconomy economy;

   private SkyEconomyManager() {
   }

   public static void load() {
      if (ConfigManager.main.getString("economy.mode").equalsIgnoreCase("Vault")) {
         economy = (new VaultUtils()).setupEconomy();
      } else if (ConfigManager.main.getString("economy.mode").equalsIgnoreCase("PlayerPoints")) {
         economy = (new PlayerPointsUtils()).hookPlayerPoints();
      } else if (ConfigManager.main.getString("economy.mode").equalsIgnoreCase("Craftconomy3")) {
         economy = (new CraftconomyUtils()).loadCraftconomy();
      } else if (ConfigManager.main.getString("economy.mode").equalsIgnoreCase("Coins")) {
         economy = (new CoinsEconomy()).loadEconomy();
      }

      if (economy == null) {
         economy = new CustomEconomy();
      }

   }

   public static double getCoins(Player var0) {
      return economy == null ? 0.0D : economy.getCoins(var0);
   }

   public static void addCoins(Player var0, double var1, boolean var3) {
      if (economy == null) {
         SkyWars.logError("Seems that your economy is null, please check your config.");
      } else {
         economy.addCoins(var0, var1, var3);
         var0.sendMessage(String.format(SkyWars.getMessage(MSG.PLAYER_COINS_ADD), var1));
         if (!SkyWars.is19orHigher()) {
            var0.playSound(var0.getLocation(), Sound.valueOf("ORB_PICKUP"), 1.0F, 1.0F);
         } else {
            var0.playSound(var0.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
         }

      }
   }

   public static void removeCoins(Player var0, int var1) {
      if (economy == null) {
         SkyWars.logError("Seems that your economy is null, please check your config.");
      } else {
         economy.removeCoins(var0, (double)var1);
         var0.sendMessage(String.format(SkyWars.getMessage(MSG.PLAYER_COINS_LESS), var1));
         if (!SkyWars.is19orHigher()) {
            var0.playSound(var0.getLocation(), Sound.valueOf("ORB_PICKUP"), 1.0F, 1.0F);
         } else {
            var0.playSound(var0.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
         }

      }
   }

   public static SkyEconomy getEconomy() {
      return economy;
   }

   public static void setEconomy(SkyEconomy var0) {
      Preconditions.checkNotNull(var0, "You can't set a null economy");
      economy = var0;
   }
}
