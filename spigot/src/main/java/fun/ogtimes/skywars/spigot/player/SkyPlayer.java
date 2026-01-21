package fun.ogtimes.skywars.spigot.player;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.abilities.Ability;
import fun.ogtimes.skywars.spigot.abilities.AbilityLevel;
import fun.ogtimes.skywars.spigot.abilities.AbilityManager;
import fun.ogtimes.skywars.spigot.abilities.AbilityType;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaBox;
import fun.ogtimes.skywars.spigot.arena.chest.ChestType;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import fun.ogtimes.skywars.spigot.events.SkyPlayerSpectatorEvent;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.events.enums.SpectatorReason;
import fun.ogtimes.skywars.spigot.kit.Kit;
import fun.ogtimes.skywars.spigot.listener.DamageListener;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import fun.ogtimes.skywars.spigot.utils.Utils;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import fun.ogtimes.skywars.spigot.utils.economy.skyeconomy.CustomEconomy;
import fun.ogtimes.skywars.spigot.utils.sky.SkyData;

import java.util.*;
import java.util.Map.Entry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkyPlayer extends SkyData {
    private final UUID uniqueId;

    private String name;
    private String url;
    private Arena arena;
    private Kit kit;
    private ArenaBox box;
    private String trail;
    private String boxsection;
    private final Set<String> ownedKits = new HashSet<>();
    private final Map<AbilityType, AbilityLevel> ownedAbilityByType = new HashMap<>();
    private final Set<AbilityType> disabledAbilities = new HashSet<>();
    private Location arenaSpawn;
    private boolean spectating;
    private int wins = 0;
    private int kills = 0;
    private int deaths = 0;
    private int played = 0;
    private int arrowShot = 0;
    private int arrowHit = 0;
    private int blocksBroken = 0;
    private int blocksPlaced = 0;
    private int distanceWalked = 0;
    private int timePlayed = 0;
    private double coins = 0.0D;
    private Date localTimePlayed = null;
    private ItemStack[] armourContents = null;
    private ItemStack[] inventoryContents = null;
    private Integer xplevel = 0;
    private float exp = 0.0F;

    public SkyPlayer(String name, UUID uuid) {
        this.name = name;
        this.uniqueId = uuid;

        addData("upload_data", false);
        load();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uniqueId) != null ? Bukkit.getPlayer(this.uniqueId) : Bukkit.getPlayer(this.name);
    }

    public Location getArenaSpawn() {
        return this.getArena() != null ? this.arenaSpawn : null;
    }

    public void addWins(int amount) {
        this.setWins(this.getWins() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }
    }

    public void addKills(int amount) {
        this.setKills(this.getKills() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addDeaths(int amount) {
        this.setDeaths(this.getDeaths() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addPlayed(int amount) {
        this.setPlayed(this.getPlayed() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addArrowShot(int amount) {
        this.setArrowShot(this.getArrowShot() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addArrowHit(int amount) {
        this.setArrowHit(this.getArrowHit() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addBlocksBroken(int amount) {
        this.setBlocksBroken(this.getBlocksBroken() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addBlocksPlaced(int amount) {
        this.setBlocksPlaced(this.getBlocksPlaced() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addDistanceWalked(int amount) {
        this.setDistanceWalked(this.getDistanceWalked() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void addTimePlayed(int amount) {
        this.setTimePlayed(this.getTimePlayed() + amount);
        if (amount > 0) {
            this.addData("upload_data", true);
        }

    }

    public void setSpectating(boolean spectating, SpectatorReason reason) {
        this.spectating = spectating;
        if (reason != null) {
            SkyPlayerSpectatorEvent var3 = new SkyPlayerSpectatorEvent(this, this.getArena(), spectating, reason);
            Bukkit.getServer().getPluginManager().callEvent(var3);
        }

    }

    public boolean isInArena() {
        return this.arena != null;
    }

    public boolean hasKit() {
        return this.kit != null;
    }

    public boolean hasBox() {
        return this.box != null;
    }

    public boolean hasTrail() {
        return this.trail != null && !this.trail.isEmpty() && !this.trail.equals("none");
    }

    public void teleport(Location location) {
        Player player = this.getPlayer();
        if (player != null) {
            this.getPlayer().teleport(location);
        }

    }

    public boolean hasPermission(String permission) {
        return this.getPlayer().hasPermission(permission);
    }

    public void sendMessage(String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(this.getPlayer(), message);
        }

        if (message != null && !message.isEmpty()) {
            Player player = this.getPlayer();
            if (player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Utils.color(message)));
            }
        }

    }

    public void sendMessage(String var1, Object... var2) {
        this.sendMessage(String.format(var1, var2));
    }

    public void clearInventory(boolean var1) {
        Player player = this.getPlayer();
        if (player != null) {
            if (SkyWars.getPlugin().getConfig().getBoolean("options.saveInventory") && var1) {
                this.armourContents = player.getInventory().getArmorContents();
                this.inventoryContents = player.getInventory().getContents();
                this.xplevel = player.getLevel();
                this.exp = player.getExp();
            }

            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
            player.getInventory().setContents(new ItemStack[0]);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            player.setHealth(player.getMaxHealth());

            player.setFoodLevel(20);
            player.setExp(0.0F);
            player.setLevel(0);
            player.setFlying(false);
            player.setAllowFlight(false);
            this.updateInventory();
        }
    }

    public void resetInventory() {
        this.getPlayer().getInventory().setArmorContents(null);
        this.getPlayer().getInventory().clear();
        if (SkyWars.getPlugin().getConfig().getBoolean("options.saveInventory")) {
            if (this.inventoryContents != null && this.inventoryContents.length > 0 && this.inventoryContents.length <= this.getPlayer().getInventory().getSize() + 1) {
                this.getPlayer().getInventory().setContents(this.inventoryContents);
            }

            if (this.armourContents != null) {
                this.getPlayer().getInventory().setArmorContents(this.armourContents);
            }

            this.getPlayer().setLevel(this.xplevel);
            this.getPlayer().setExp(this.exp);
            this.updateInventory();
            this.armourContents = null;
            this.inventoryContents = null;
            this.xplevel = 0;
            this.exp = 0.0F;
        }

    }

    public void resetVotes() {
        if (this.hasData("voted_chest")) {
            ChestType[] chestTypes = ChestTypeManager.getChestTypes();

            for (ChestType chestType : chestTypes) {
                if (this.hasData("voted_chest_" + chestType.getName())) {
                    this.getArena().addData("vote_chest_" + chestType.getName(), this.getArena().getInt("vote_chest_" + chestType.getName()) - 1);
                    this.removeData("voted_chest_" + chestType.getName());
                }
            }

            this.removeData("voted_chest");
        }

        if (this.hasData("voted_time")) {
            if (this.hasData("voted_time_day")) {
                this.getArena().addData("vote_time_day", this.getArena().getInt("vote_time_day") - 1);
                this.removeData("voted_time_day");
            }

            if (this.hasData("voted_time_night")) {
                this.getArena().addData("vote_time_night", this.getArena().getInt("vote_time_night") - 1);
                this.removeData("voted_time_night");
            }

            if (this.hasData("voted_time_sunset")) {
                this.getArena().addData("vote_time_sunset", this.getArena().getInt("vote_time_sunset") - 1);
                this.removeData("voted_time_sunset");
            }

            this.removeData("voted_time");
        }

    }

    public void updateInventory() {
        this.getPlayer().updateInventory();
    }

    public double getCoins() {
        return SkyEconomyManager.getCoins(this.getPlayer());
    }

    public double getCoins2() {
        return this.coins;
    }

    public void load() {
        this.loadData();
    }

    public void loadData() {
        DatabaseHandler.getDS().loadPlayerData(this);
        if (CustomEconomy.isCustom()) {
            this.coins = DatabaseHandler.getDS().getCoins(this);
        }

    }

    public void upload(boolean sync) {
        if (sync) {
            this.uploadData();
        } else {
            this.uploadAsyncData();
        }

    }

    public void uploadAsyncData() {
        if (this.hasData("upload_data") && this.getBoolean("upload_data")) {
            (new BukkitRunnable() {
                public void run() {
                    DatabaseHandler.getDS().uploadPlayerData(SkyPlayer.this);
                }
            }).runTaskAsynchronously(SkyWars.getPlugin());
        }

    }

    public void uploadData() {
        if (this.hasData("upload_data") && this.getBoolean("upload_data")) {
            DatabaseHandler.getDS().uploadPlayerData(this);
        }

    }

    public String convertKitsToString() {
        String var1 = "";

        String var3;
        for(Iterator<String> var2 = this.ownedKits.iterator(); var2.hasNext(); var1 = var1 + var3 + ",") {
            var3 = var2.next();
        }

        if (var1.endsWith(",")) {
            var1.substring(0, var1.length() - 1);
        }

        return var1;
    }

    public void setBoxSection(String var1, boolean var2) {
        this.boxsection = var1;
    }

    public String getBoxSection() {
        return this.boxsection;
    }

    public int getBoxItem(String var1) {
        return SkyWars.boxes.getInt("boxes." + var1 + ".item");
    }

    public int getBoxData(String var1) {
        return SkyWars.boxes.getInt("boxes." + var1 + ".data");
    }

    public boolean hasKit(Kit kit) {
        return kit != null && this.ownedKits.contains(kit.getName());
    }

    public void addKit(Kit var1) {
        if (!this.hasKit(var1)) {
            this.ownedKits.add(var1.getName());
        }

    }

    public void distanceWalkedConvert() {
        double localDistance = this.getDouble("local_distance");
        int roundedDistance = (int)Math.round(localDistance);
        this.addDistanceWalked(roundedDistance);
        this.removeData("local_distance");
    }

    public void playedTimeStart() {
        this.localTimePlayed = new Date();
    }

    public void playedTimeEnd() {
        if (this.localTimePlayed != null) {
            Date date = new Date();
            long played = date.getTime() - this.localTimePlayed.getTime();
            int playedSeconds = (int)(played / 1000L);
            this.addTimePlayed(playedSeconds);
            this.localTimePlayed = null;
        }

    }

    public boolean isAbilityDisabled(AbilityType var1) {
        return this.disabledAbilities.contains(var1);
    }

    public void addAbilityDisabled(AbilityType var1) {
        this.disabledAbilities.add(var1);

        if (!this.hasAbility(var1)) {
            this.ownedAbilityByType.put(var1, null);
        }

        this.addData("upload_data", true);
    }

    public void removeAbilityDisabled(AbilityType var1) {
        this.disabledAbilities.remove(var1);

        this.addData("upload_data", true);
    }

    public boolean hasAbility(AbilityType var1) {
        return this.ownedAbilityByType.get(var1) != null && this.ownedAbilityByType.containsKey(var1);
    }

    public AbilityLevel getAbilityLevel(AbilityType var1) {
        return this.ownedAbilityByType.getOrDefault(var1, null);
    }

    public void setAbility(AbilityType var1, AbilityLevel var2) {
        this.ownedAbilityByType.put(var1, var2);
    }

    public void addAbilityLevel(AbilityType var1) {
        if (this.hasAbility(var1)) {
            if (this.ownedAbilityByType.get(var1).getLevel() == 8) {
                return;
            }

            this.ownedAbilityByType.put(var1, AbilityManager.getAbilityByType(var1).getLevel(this.ownedAbilityByType.get(var1).getLevel() + 1));
        } else {
            this.ownedAbilityByType.put(var1, AbilityManager.getAbilityByType(var1).getLevel(1));
        }

    }

    public void serializeAbilities(String var1) {
        if (var1 != null && !var1.isEmpty()) {
            String[] var2 = var1.split(";");

            for (String var6 : var2) {
                String[] var7 = var6.split(",");
                Ability var8 = AbilityManager.getAbility(var7[0]);
                AbilityLevel var9 = null;
                if (!Objects.equals(var7[1], "0")) {
                    var9 = var8.getLevel(Integer.parseInt(var7[1]));
                }

                if (var7[2].equals("1")) {
                    this.addAbilityDisabled(var8.getType());
                }

                this.setAbility(var8.getType(), var9);
            }
        }

    }

    public String deserializeAbilities() {
        StringBuilder var1 = new StringBuilder();

        for (Entry<AbilityType, AbilityLevel> entry : this.ownedAbilityByType.entrySet()) {
            if (entry.getValue() == null) {
                var1.append(AbilityManager.getAbilityByType(entry.getKey()).getName()).append(",0,").append(this.isAbilityDisabled(entry.getKey()) ? "1" : "0").append(";");
            } else {
                var1.append(AbilityManager.getAbilityByType(entry.getKey()).getName()).append(",").append(entry.getValue().getLevel()).append(",").append(this.isAbilityDisabled(entry.getKey()) ? "1" : "0").append(";");
            }
        }

        if (var1.toString().endsWith(";")) {
            var1.substring(0, var1.length() - 1);
        }

        return var1.toString();
    }

    public void leave() {
        if (isInArena()) {
            if (DamageListener.lastDamage.containsKey(getUniqueId())) {
                Player lastDamager = Bukkit.getPlayer(DamageListener.lastDamage.get(getUniqueId()));
                getPlayer().damage(1000.0D, lastDamager);
                addDeaths(1);
            }

            arena.removePlayer(this, ArenaLeaveCause.COMMAND);
            SkyWars.log("CmdOther.onCommand - " + getName() + " removed using command");
        }

        if (SkyWars.isProxyMode()) {
            ProxyUtils.teleToServer(getPlayer(), SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
        }
    }
}
