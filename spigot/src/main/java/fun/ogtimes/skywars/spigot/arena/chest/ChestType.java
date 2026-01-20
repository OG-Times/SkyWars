package fun.ogtimes.skywars.spigot.arena.chest;

import fun.ogtimes.skywars.spigot.SkyWars;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestType {
   @Getter
   private final String name;
   @Getter
   private String title;
   @Setter
   @Getter
   private int slot;
   @Getter
   private Material item;
   private short item_data;
    @Getter
    private FileConfiguration config = null;
   @Setter
   @Getter
   private List<String> description = new ArrayList<>();
   @Setter
   @Getter
   private List<RandomItem> items = new ArrayList<>();

   public ChestType(String var1) {
      this.name = var1;
       File config_file = new File(SkyWars.getPlugin().getDataFolder(), SkyWars.chests + File.separator + var1 + ".yml");
      this.config = YamlConfiguration.loadConfiguration(config_file);
      ChestTypeManager.chesttypes.put(this.getName(), this);
   }

   public void setTitle(String var1) {
      this.title = ChatColor.translateAlternateColorCodes('&', var1);
   }

    public void setItem(String var1, short var2) {
      if (ChestTypeManager.isNumeric(var1)) {
         this.item = Material.getMaterial(Integer.parseInt(var1));
      } else {
         this.item = Material.getMaterial(var1.toUpperCase());
      }

      this.item_data = var2;
   }

    public void addDescription(String var1) {
      this.description.add(var1);
   }

   public void addItem(RandomItem var1) {
      this.items.add(var1);
   }

    public short getItemData() {
      return this.item_data;
   }

    public String getShortName() {
      return this.config.getString("name") == null ? this.name : this.config.getString("name");
   }

    private int countItems(Inventory var1) {
      int var2 = 0;
      ItemStack[] var3 = var1.getContents();
      int var4 = var3.length;

       for (ItemStack var6 : var3) {
           if (var6 != null && var6.getType() != Material.AIR) {
               ++var2;
           }
       }

      return var2;
   }

   public void fillChest(Inventory var1) {
       this.fillChest(var1, var1.getHolder() instanceof DoubleChest);

   }

   private void fillChest(Inventory var1, boolean var2) {
      var1.clear();
      if (!this.getItems().isEmpty()) {
         int var3 = SkyWars.getPlugin().getConfig().getInt("max_items_types_chest");

         while(this.countItems(var1) < var3) {
            Collections.shuffle(this.getItems(), new Random());

             for (RandomItem var5 : this.getItems()) {
                 if (this.countItems(var1) < var3 && var5.hasChance()) {
                     var1.setItem((new Random()).nextInt(var1.getSize()), var5.getItem().build());
                 }
             }
         }
      }

   }
}
