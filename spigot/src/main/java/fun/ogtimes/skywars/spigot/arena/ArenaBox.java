package fun.ogtimes.skywars.spigot.arena;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

@Data
public class ArenaBox {
    private final Location base;
    private final World world;
    private int x;
    private int y;
    private int z;

    public ArenaBox(Location location) {
        this.base = location;
        this.world = location.getWorld();
    }

    public void setBox(int var1, int var2) {
        this.x = this.base.getBlockX();
        this.y = this.base.getBlockY();
        this.z = this.base.getBlockZ();
        Material material = Material.getMaterial(var1);
        this.world.getBlockAt(this.x, this.y - 1, this.z).setType(material);
        this.world.getBlockAt(this.x, this.y - 1, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y, this.z - 1).setType(material);
        this.world.getBlockAt(this.x, this.y, this.z - 1).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y + 1, this.z - 1).setType(material);
        this.world.getBlockAt(this.x, this.y + 1, this.z - 1).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y + 2, this.z - 1).setType(material);
        this.world.getBlockAt(this.x, this.y + 2, this.z - 1).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y, this.z + 1).setType(material);
        this.world.getBlockAt(this.x, this.y, this.z + 1).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y + 1, this.z + 1).setType(material);
        this.world.getBlockAt(this.x, this.y + 1, this.z + 1).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y + 2, this.z + 1).setType(material);
        this.world.getBlockAt(this.x, this.y + 2, this.z + 1).setData((byte)var2);
        this.world.getBlockAt(this.x + 1, this.y, this.z).setType(material);
        this.world.getBlockAt(this.x + 1, this.y, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x + 1, this.y + 1, this.z).setType(material);
        this.world.getBlockAt(this.x + 1, this.y + 1, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x + 1, this.y + 2, this.z).setType(material);
        this.world.getBlockAt(this.x + 1, this.y + 2, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x - 1, this.y, this.z).setType(material);
        this.world.getBlockAt(this.x - 1, this.y, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x - 1, this.y + 1, this.z).setType(material);
        this.world.getBlockAt(this.x - 1, this.y + 1, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x - 1, this.y + 2, this.z).setType(material);
        this.world.getBlockAt(this.x - 1, this.y + 2, this.z).setData((byte)var2);
        this.world.getBlockAt(this.x, this.y + 3, this.z).setType(Material.AIR);
    }

    public Location getLocation() {
        return this.base;
    }

    public void removeBase() {
        this.world.getBlockAt(this.x, this.y - 1, this.z).setType(Material.AIR);
    }

    public void removeAll() {
        Material var1 = Material.AIR;
        this.world.getBlockAt(this.x, this.y - 1, this.z).setType(var1);
        this.world.getBlockAt(this.x, this.y, this.z - 1).setType(var1);
        this.world.getBlockAt(this.x, this.y + 1, this.z - 1).setType(var1);
        this.world.getBlockAt(this.x, this.y + 2, this.z - 1).setType(var1);
        this.world.getBlockAt(this.x, this.y, this.z + 1).setType(var1);
        this.world.getBlockAt(this.x, this.y + 1, this.z + 1).setType(var1);
        this.world.getBlockAt(this.x, this.y + 2, this.z + 1).setType(var1);
        this.world.getBlockAt(this.x + 1, this.y, this.z).setType(var1);
        this.world.getBlockAt(this.x + 1, this.y + 1, this.z).setType(var1);
        this.world.getBlockAt(this.x + 1, this.y + 2, this.z).setType(var1);
        this.world.getBlockAt(this.x - 1, this.y, this.z).setType(var1);
        this.world.getBlockAt(this.x - 1, this.y + 1, this.z).setType(var1);
        this.world.getBlockAt(this.x - 1, this.y + 2, this.z).setType(var1);
        this.world.getBlockAt(this.x, this.y + 3, this.z).setType(var1);
    }
}
