package ak.CookLoco.SkyWars.utils.economy;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.config.ConfigManager;
import ak.CookLoco.SkyWars.utils.Messages;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.CustomEconomy;
import ak.CookLoco.SkyWars.utils.economy.skyeconomy.VaultUtils;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
public final class SkyEconomyManager {
    private static SkyEconomy economy;

    private SkyEconomyManager() {
    }

    public static void load() {
        if (ConfigManager.main.getString("economy.mode").equalsIgnoreCase("Vault")) {
            economy = (new VaultUtils()).setupEconomy();
        }

        if (economy == null) {
            economy = new CustomEconomy();
        }

    }

    public static double getCoins(Player var0) {
        return economy == null ? 0.0D : economy.getCoins(var0);
    }

    public static void addCoins(Player player, double var1, boolean var3) {
        if (economy == null) {
            SkyWars.logError("Seems that your economy is null, please check your config.");
        } else {
            economy.addCoins(player, var1, var3);
            player.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_COINS_ADD), var1));
            player.playSound(player.getLocation(), Sound.valueOf("ORB_PICKUP"), 1.0F, 1.0F);

        }
    }

    public static void removeCoins(Player var0, int var1) {
        if (economy == null) {
            SkyWars.logError("Seems that your economy is null, please check your config.");
        } else {
            economy.removeCoins(var0, (double)var1);
            var0.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_COINS_LESS), var1));
            var0.playSound(var0.getLocation(), Sound.valueOf("ORB_PICKUP"), 1.0F, 1.0F);

        }
    }

    public static void setEconomy(SkyEconomy var0) {
        Preconditions.checkNotNull(var0, "You can't set a null economy");
        economy = var0;
    }
}
