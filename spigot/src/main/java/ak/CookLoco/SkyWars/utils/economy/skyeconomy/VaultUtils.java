package ak.CookLoco.SkyWars.utils.economy.skyeconomy;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.utils.economy.SkyEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtils implements SkyEconomy {
    private Economy economy = null;

    public double getCoins(Player var1) {
        return this.economy == null && this.setupEconomy() != null ? 0.0D : (double)((int)this.economy.getBalance(var1));
    }

    public void addCoins(Player var1, double var2, boolean var4) {
        if (this.economy != null) {
            this.economy.depositPlayer(var1, var4 ? this.multiply(var1, var2) : var2);
        }
    }

    public void removeCoins(Player var1, double var2) {
        if (this.economy != null) {
            this.economy.withdrawPlayer(var1, var2);
        }
    }

    public SkyEconomy setupEconomy() {
        if (SkyWars.getPlugin().getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        } else {
            RegisteredServiceProvider<Economy> service = SkyWars.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
            if (service == null) {
                return null;
            } else {
                this.economy = service.getProvider();
                return this.economy != null ? this : null;
            }
        }
    }
}
