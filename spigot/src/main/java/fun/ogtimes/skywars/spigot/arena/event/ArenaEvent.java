package fun.ogtimes.skywars.spigot.arena.event;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.chest.ChestType;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.title.Title;
import java.util.Iterator;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

@Setter
@Getter
public class ArenaEvent {
    private EventType event;
    private String argument;
    private int seconds;
    private String title;
    private boolean executed = false;

    public ArenaEvent(EventType var1, String var2, int var3, String var4) {
        this.event = var1;
        this.argument = var2;
        this.seconds = var3;
        this.title = var4;
    }

    public void playEvent(Arena arena) {
        if (this.event == EventType.REFILL) {
            ChestType chestType = ChestTypeManager.getChestType(this.argument == null ? arena.getChest() : (this.argument.equalsIgnoreCase("selected") ? arena.getChest() : this.argument));
            Iterator<?> iterator = arena.getChestFilled().iterator();

            while(iterator.hasNext()) {
                Location location = (Location)iterator.next();
                Block block = location.getBlock();
                if (block.getState() instanceof Chest chest) {
                    chestType.fillChest(chest.getInventory());
                }
            }

            iterator = arena.getAlivePlayer().iterator();

            while(iterator.hasNext()) {
                SkyPlayer skyPlayer = (SkyPlayer)iterator.next();
                skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_EVENT_REFILL));
                Title title = new Title(SkyWars.getMessage(Messages.GAME_EVENT_REFILL), 20, 80, 20);
                title.send(skyPlayer.getPlayer());
            }
        }

        this.executed = true;
    }

    public String getTime() {
        int var1 = this.seconds / 3600;
        int var2 = this.seconds % 3600 / 60;
        int var3 = this.seconds % 60;
        String var4;
        if (var1 >= 1) {
            var4 = String.format("%02d:%02d:%02d", var1, var2, var3);
        } else {
            var4 = String.format("%02d:%02d", var2, var3);
        }

        return var4;
    }
}
