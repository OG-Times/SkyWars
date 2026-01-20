package fun.ogtimes.skywars.spigot.box;

import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

public class Box {
   @Getter
   private final String name;
   private String desc;
   @Setter
   @Getter
   private String section;
   @Setter
   @Getter
   private int item;
   @Setter
   @Getter
   private int data;
   @Setter
   @Getter
   private int slot;

   public Box(String var1, String var2) {
      this.name = var1;
      this.desc = var2;
   }

    public String getDescription() {
      return this.desc;
   }

    public ItemBuilder getItemBuilder() {
      return (new ItemBuilder(Material.getMaterial(this.getItem()), (short)this.getData())).setTitle(this.getName()).addLore(Arrays.asList(this.getDescription().split("\n")));
   }

    public void setDescription(String var1) {
      this.desc = var1;
   }

}
