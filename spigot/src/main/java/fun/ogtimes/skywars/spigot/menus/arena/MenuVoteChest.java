package fun.ogtimes.skywars.spigot.menus.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.chest.ChestType;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.menus.Menu;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MenuVoteChest extends Menu {
    public MenuVoteChest(Player player) {
       super(player, "voteChest", SkyWars.getMessage(Messages.VOTE_CHESTS_TITLE), 3);
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.update();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {}

    @Override
    public void onClick(InventoryClickEvent event) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(this.getPlayer());
        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            ChestType[] chestTypes = ChestTypeManager.getChestTypes();

            if (event.getCurrentItem() == null) {
                return;
            }

            for (ChestType chestType : chestTypes) {
                if (event.getCurrentItem().getType() == chestType.getItem() && event.getSlot() == chestType.getSlot()) {
                    if (!skyPlayer.hasPermission("skywars.vote.chest." + chestType.getName())) {
                        skyPlayer.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_VOTE_CHEST));
                        this.getPlayer().closeInventory();
                        return;
                    }

                    String previousVote = skyPlayer.getString("voted_chest_type");
                    if (previousVote != null && !previousVote.isEmpty()) {
                        if (previousVote.equalsIgnoreCase(chestType.getName())) {
                            this.getPlayer().closeInventory();
                            return;
                        }

                        int previousCount = arena.getInt("vote_chest_" + previousVote);
                        if (previousCount > 0) {
                            arena.addData("vote_chest_" + previousVote, previousCount - 1);
                        }
                    }

                    skyPlayer.addData("voted_chest", true);
                    skyPlayer.addData("voted_chest_type", chestType.getName());
                    skyPlayer.addData("voted_chest_" + chestType.getName(), true);

                    arena.addData("vote_chest_" + chestType.getName(), arena.getInt("vote_chest_" + chestType.getName()) + 1);
                    this.getPlayer().sendMessage(String.format(SkyWars.getMessage(Messages.VOTE_CHESTS_SUCCESSFUL), ChatColor.stripColor(chestType.getTitle())));
                    arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_VOTE_CHESTS), this.getPlayer().getName(), chestType.getShortName(), arena.getInt("vote_chest_" + chestType.getName())));
                    this.getPlayer().closeInventory();
                }
            }
        }

    }

    @Override
    public void update() {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(this.getPlayer());
        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            ChestType[] chestTypes = ChestTypeManager.getChestTypes();

            for (ChestType chestType : chestTypes) {
                if (arena.getConfig().getStringList("chests.selectable").contains(chestType.getName())) {
                    ItemBuilder builder = new ItemBuilder(chestType.getItem(), chestType.getItemData());
                    builder.setTitle(chestType.getTitle()).setLore(chestType.getDescription());
                    builder.addLore(String.format(SkyWars.getMessage(Messages.VOTE_VOTES), arena.getInt("vote_chest_" + chestType.getName())));
                    this.setItem(chestType.getSlot(), builder);
                }
            }
        }

    }
}
