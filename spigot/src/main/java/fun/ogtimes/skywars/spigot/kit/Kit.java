package fun.ogtimes.skywars.spigot.kit;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.config.SkyConfiguration;
import fun.ogtimes.skywars.spigot.utils.Console;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;

public class Kit {
   private String name;
   private int price;
   private List<ItemBuilder> items = new ArrayList();
   private int slot;
   private List<String> item_lore = new ArrayList();
   private List<String> contents = new ArrayList();
   private boolean free = false;
   private ItemBuilder item;
   private SkyConfiguration config;

   public Kit(String var1) {
      this.name = var1;
      File var2 = new File(SkyWars.getPlugin().getDataFolder(), SkyWars.kits + File.separator + var1 + ".yml");
      this.config = new SkyConfiguration(var2);
      this.loadConfig(var2);
      this.price = this.config.getInt("price");
      if (this.price <= 0) {
         this.free = true;
      }

      this.slot = this.config.getInt("icon.slot");
      this.items.clear();
      SkyWars.log("Kit.load - Loading: " + var1);
      Iterator var3 = this.config.getStringList("items").iterator();

      String var4;
      while(var3.hasNext()) {
         var4 = (String)var3.next();

         try {
            this.items.add(Utils.readItem(var4));
         } catch (NullPointerException var6) {
            Console.severe("The kit '" + var1 + "' has skipped the item \"" + var4.toString() + "\" due to a syntax error");
         }
      }

      SkyWars.log("Kit.load - Loaded: " + var1);
      var3 = this.config.getStringList("contents").iterator();

      while(var3.hasNext()) {
         var4 = (String)var3.next();
         this.contents.add(var4.toString());
      }

      this.item_lore.add(SkyWars.getMessage(Messages.KIT_CONTENTS));
      var3 = this.contents.iterator();

      while(var3.hasNext()) {
         var4 = (String)var3.next();
         this.item_lore.add(String.format(ChatColor.translateAlternateColorCodes('&', SkyWars.getMessage(Messages.KIT_CONTENTS_FORMAT)), var4));
      }

      this.item = Utils.readItem(this.config.getString("icon.item"));
      this.updateConfig(var2);
      this.item.setTitle(this.isFree() ? String.format(SkyWars.getMessage(Messages.KIT_NAME_FREE), var1) : String.format(SkyWars.getMessage(Messages.KIT_NAME_NOTPURCHASED), var1));
      if (!this.isFree()) {
         this.item.addLore(String.format(SkyWars.getMessage(Messages.KIT_COST), this.price));
      }

      this.item.addLore(this.item_lore);
      KitManager.kits.put(var1, this);
   }

   public String getName() {
      return this.name;
   }

   public int getPrice() {
      return this.price;
   }

   public List<ItemBuilder> getItems() {
      return this.items;
   }

   public int getSlot() {
      return this.slot;
   }

   public ItemBuilder getIcon() {
      return this.item;
   }

   public boolean isFree() {
      return this.free;
   }

   public List<String> getContents() {
      return this.item_lore;
   }

   public SkyConfiguration getConfig() {
      return this.config;
   }

   private void loadConfig(File var1) {
      this.config.addDefault("price", 0, "Kit price (if price is 0, the kit will be free)");
      this.config.addDefault("icon.slot", 0, "Slot in the Kit Selector (Waiting)");
      this.config.addDefault("icon.item", "1:0", "Item in Kit Selector (ID:Data)");
      this.config.addDefault("contents", new ArrayList(), "Description of the kit");
      this.config.addDefault("items", new ArrayList(), "Items to be given in the kit", "Format: ID:Data,Amount (If Data is equals to 0 or Amount equals to 1 is not necessary to write it)", "Format: ITEM_NAME:Data,Amount", "Format: ID:Data,Amount,Value,Value,Value,...", "Available values:", "    name:&3Item Name", "    lore:&7Item description line", "    ENCHANTMENT:LEVEL  (Enchantment list here: https://goo.gl/KKBDiH)", "    potion:POTION_NAME:Upgraded:Extended  (Potions list here: https://goo.gl/aBGNSw) (Example: potion:JUMP:true:false for Jump II)", "    leather_color:R-G-B  (R,G,B values from 0 to 255)", "    glowing - Add Glowing/Enchantment effect to the item");
      this.config.options().copyDefaults(true);
      this.config.getEConfig().setNewLinePerKey(true);
      this.config.save();
   }

   private void updateConfig(File var1) {
      ArrayList var2 = new ArrayList();
      Iterator var3 = this.items.iterator();

      while(var3.hasNext()) {
         ItemBuilder var4 = (ItemBuilder)var3.next();
         var2.add(var4.toString());
      }

      this.config.set("icon.item", this.item.toString());
      this.config.set("items", var2);
      this.config.save();
   }
}
