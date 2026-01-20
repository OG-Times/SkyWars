package fun.ogtimes.skywars.spigot.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EConfiguration {
   @Getter
   protected final Map<String, List<String>> comments = new LinkedHashMap<>();
   @Setter
   protected boolean newLinePerKey = false;

    public List<String> getComments(String var1) {
      return this.comments.containsKey(var1) ? new ArrayList(this.comments.get(var1)) : new ArrayList();
   }

    public boolean shouldAddNewLinePerKey() {
      return this.newLinePerKey;
   }

   public String getPathToComment(List<String> var1, int var2, String var3) {
      if (var1 != null && var2 >= 0 && var2 < var1.size() - 1 && var3 != null) {
         String var4 = this.trimPrefixSpaces(var3);
         if (var4.startsWith("#")) {
            int var5 = var2;

            while(var5 < var1.size() - 1) {
               ++var5;
               String var6 = var1.get(var5);
               String var7 = this.trimPrefixSpaces(var6);
               if (!var7.startsWith("#")) {
                  if (var7.contains(":")) {
                     return this.getPathToKey(var1, var5, var6);
                  }
                  break;
               }
            }
         }
      }

      return null;
   }

   public String getPathToKey(List<String> var1, int var2, String var3) {
      if (var1 != null && var2 >= 0 && var2 < var1.size() && var3 != null && !var3.startsWith("#") && var3.contains(":")) {
         int var4 = this.getPrefixSpaceCount(var3);
         StringBuilder var5 = new StringBuilder(this.trimPrefixSpaces(var3.substring(0, var3.indexOf(58))));
         if (var4 > 0) {
            int var6 = var2;
            int var7 = -1;
            boolean var8 = false;

            while(true) {
               String var9;
               int var10;
               do {
                  do {
                     do {
                        do {
                           if (var6 == 0) {
                              return var5.toString();
                           }

                           --var6;
                           var9 = var1.get(var6);
                           var10 = this.getPrefixSpaceCount(var9);
                           if (this.trim(var9).isEmpty()) {
                              return var5.toString();
                           }
                        } while(this.trim(var9).startsWith("#"));
                     } while(var10 >= var4);
                  } while(!var9.contains(":"));

                  if (var10 <= 0 && var8) {
                     return var5.toString();
                  }

                  if (var10 == 0) {
                     var8 = true;
                  }
               } while(var7 != -1 && var10 >= var7);

               var7 = var10;
               var5.insert(0, this.trimPrefixSpaces(var9.substring(0, var9.indexOf(":"))) + ".");
            }
         } else {
            return var5.toString();
         }
      } else {
         return null;
      }
   }

   public int getPrefixSpaceCount(String var1) {
      int var2 = 0;
      if (var1 != null && var1.contains(" ")) {
         char[] var3 = var1.toCharArray();
         int var4 = var3.length;

          for (char var6 : var3) {
              if (var6 != ' ') {
                  break;
              }

              ++var2;
          }
      }

      return var2;
   }

   public String trim(String var1) {
      return var1 != null ? var1.trim().replace(System.lineSeparator(), "") : "";
   }

   public String trimPrefixSpaces(String var1) {
      if (var1 != null) {
         while(var1.startsWith(" ")) {
            var1 = var1.substring(1);
         }
      }

      return var1;
   }
}
