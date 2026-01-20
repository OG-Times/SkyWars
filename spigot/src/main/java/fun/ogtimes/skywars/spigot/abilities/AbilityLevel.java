package fun.ogtimes.skywars.spigot.abilities;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AbilityLevel {
   private int level;
   private int chance;
   private int price;
   private int value;
   private boolean hasvalue;

   public AbilityLevel(int var1, int var2, int var3, boolean var4, int var5) {
      this.setLevel(var1);
      this.setChance(var2);
      this.setPrice(var3);
      this.setHasvalue(var4);
      this.setValue(var5);
   }

}
