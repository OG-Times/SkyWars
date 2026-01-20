package ak.CookLoco.SkyWars.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

public final class ReflectionUtils {
   private ReflectionUtils() {
   }

   public static Constructor<?> getConstructor(Class<?> var0, Class<?>... var1) {
      Class[] var2 = DataType.getPrimitive(var1);
      Constructor[] var3 = var0.getConstructors();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Constructor var6 = var3[var5];
         if (DataType.compare(DataType.getPrimitive(var6.getParameterTypes()), var2)) {
            return var6;
         }
      }

      throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
   }

   public static Constructor<?> getConstructor(String var0, PackageType var1, Class<?>... var2) {
      return getConstructor(var1.getClass(var0), var2);
   }

   public static Object instantiateObject(Class<?> var0, Object... var1) {
      return getConstructor(var0, DataType.getPrimitive(var1)).newInstance(var1);
   }

   public static Object instantiateObject(String var0, PackageType var1, Object... var2) {
      return instantiateObject(var1.getClass(var0), var2);
   }

   public static Method getMethod(Class<?> var0, String var1, Class<?>... var2) {
      Class[] var3 = DataType.getPrimitive(var2);
      Method[] var4 = var0.getMethods();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Method var7 = var4[var6];
         if (var7.getName().equals(var1) && DataType.compare(DataType.getPrimitive(var7.getParameterTypes()), var3)) {
            return var7;
         }
      }

      throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
   }

   public static Method getMethod(String var0, PackageType var1, String var2, Class<?>... var3) {
      return getMethod(var1.getClass(var0), var2, var3);
   }

   public static Object invokeMethod(Object var0, String var1, Object... var2) {
      return getMethod(var0.getClass(), var1, DataType.getPrimitive(var2)).invoke(var0, var2);
   }

   public static Object invokeMethod(Object var0, Class<?> var1, String var2, Object... var3) {
      return getMethod(var1, var2, DataType.getPrimitive(var3)).invoke(var0, var3);
   }

   public static Object invokeMethod(Object var0, String var1, PackageType var2, String var3, Object... var4) {
      return invokeMethod(var0, var2.getClass(var1), var3, var4);
   }

   public static Field getField(Class<?> var0, boolean var1, String var2) {
      Field var3 = var1 ? var0.getDeclaredField(var2) : var0.getField(var2);
      var3.setAccessible(true);
      return var3;
   }

   public static Field getField(String var0, PackageType var1, boolean var2, String var3) {
      return getField(var1.getClass(var0), var2, var3);
   }

   public static Object getValue(Object var0, Class<?> var1, boolean var2, String var3) {
      return getField(var1, var2, var3).get(var0);
   }

   public static Object getValue(Object var0, String var1, PackageType var2, boolean var3, String var4) {
      return getValue(var0, var2.getClass(var1), var3, var4);
   }

   public static Object getValue(Object var0, boolean var1, String var2) {
      return getValue(var0, var0.getClass(), var1, var2);
   }

   public static void setValue(Object var0, Class<?> var1, boolean var2, String var3, Object var4) {
      getField(var1, var2, var3).set(var0, var4);
   }

   public static void setValue(Object var0, String var1, PackageType var2, boolean var3, String var4, Object var5) {
      setValue(var0, var2.getClass(var1), var3, var4, var5);
   }

   public static void setValue(Object var0, boolean var1, String var2, Object var3) {
      setValue(var0, var0.getClass(), var1, var2, var3);
   }

   public static enum DataType {
      BYTE(Byte.TYPE, Byte.class),
      SHORT(Short.TYPE, Short.class),
      INTEGER(Integer.TYPE, Integer.class),
      LONG(Long.TYPE, Long.class),
      CHARACTER(Character.TYPE, Character.class),
      FLOAT(Float.TYPE, Float.class),
      DOUBLE(Double.TYPE, Double.class),
      BOOLEAN(Boolean.TYPE, Boolean.class);

      private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap();
      private final Class<?> primitive;
      private final Class<?> reference;

      private DataType(Class<?> var3, Class<?> var4) {
         this.primitive = var3;
         this.reference = var4;
      }

      public Class<?> getPrimitive() {
         return this.primitive;
      }

      public Class<?> getReference() {
         return this.reference;
      }

      public static DataType fromClass(Class<?> var0) {
         return (DataType)CLASS_MAP.get(var0);
      }

      public static Class<?> getPrimitive(Class<?> var0) {
         DataType var1 = fromClass(var0);
         return var1 == null ? var0 : var1.getPrimitive();
      }

      public static Class<?> getReference(Class<?> var0) {
         DataType var1 = fromClass(var0);
         return var1 == null ? var0 : var1.getReference();
      }

      public static Class<?>[] getPrimitive(Class<?>[] var0) {
         int var1 = var0 == null ? 0 : var0.length;
         Class[] var2 = new Class[var1];

         for(int var3 = 0; var3 < var1; ++var3) {
            var2[var3] = getPrimitive(var0[var3]);
         }

         return var2;
      }

      public static Class<?>[] getReference(Class<?>[] var0) {
         int var1 = var0 == null ? 0 : var0.length;
         Class[] var2 = new Class[var1];

         for(int var3 = 0; var3 < var1; ++var3) {
            var2[var3] = getReference(var0[var3]);
         }

         return var2;
      }

      public static Class<?>[] getPrimitive(Object[] var0) {
         int var1 = var0 == null ? 0 : var0.length;
         Class[] var2 = new Class[var1];

         for(int var3 = 0; var3 < var1; ++var3) {
            var2[var3] = getPrimitive(var0[var3].getClass());
         }

         return var2;
      }

      public static Class<?>[] getReference(Object[] var0) {
         int var1 = var0 == null ? 0 : var0.length;
         Class[] var2 = new Class[var1];

         for(int var3 = 0; var3 < var1; ++var3) {
            var2[var3] = getReference(var0[var3].getClass());
         }

         return var2;
      }

      public static boolean compare(Class<?>[] var0, Class<?>[] var1) {
         if (var0 != null && var1 != null && var0.length == var1.length) {
            for(int var2 = 0; var2 < var0.length; ++var2) {
               Class var3 = var0[var2];
               Class var4 = var1[var2];
               if (!var3.equals(var4) && !var3.isAssignableFrom(var4)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }

      static {
         DataType[] var0 = values();
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            DataType var3 = var0[var2];
            CLASS_MAP.put(var3.primitive, var3);
            CLASS_MAP.put(var3.reference, var3);
         }

      }
   }

   public static enum PackageType {
      MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()),
      CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()),
      CRAFTBUKKIT_BLOCK(CRAFTBUKKIT, "block"),
      CRAFTBUKKIT_CHUNKIO(CRAFTBUKKIT, "chunkio"),
      CRAFTBUKKIT_COMMAND(CRAFTBUKKIT, "command"),
      CRAFTBUKKIT_CONVERSATIONS(CRAFTBUKKIT, "conversations"),
      CRAFTBUKKIT_ENCHANTMENS(CRAFTBUKKIT, "enchantments"),
      CRAFTBUKKIT_ENTITY(CRAFTBUKKIT, "entity"),
      CRAFTBUKKIT_EVENT(CRAFTBUKKIT, "event"),
      CRAFTBUKKIT_GENERATOR(CRAFTBUKKIT, "generator"),
      CRAFTBUKKIT_HELP(CRAFTBUKKIT, "help"),
      CRAFTBUKKIT_INVENTORY(CRAFTBUKKIT, "inventory"),
      CRAFTBUKKIT_MAP(CRAFTBUKKIT, "map"),
      CRAFTBUKKIT_METADATA(CRAFTBUKKIT, "metadata"),
      CRAFTBUKKIT_POTION(CRAFTBUKKIT, "potion"),
      CRAFTBUKKIT_PROJECTILES(CRAFTBUKKIT, "projectiles"),
      CRAFTBUKKIT_SCHEDULER(CRAFTBUKKIT, "scheduler"),
      CRAFTBUKKIT_SCOREBOARD(CRAFTBUKKIT, "scoreboard"),
      CRAFTBUKKIT_UPDATER(CRAFTBUKKIT, "updater"),
      CRAFTBUKKIT_UTIL(CRAFTBUKKIT, "util");

      private final String path;

      private PackageType(String var3) {
         this.path = var3;
      }

      private PackageType(PackageType var3, String var4) {
         this(var3 + "." + var4);
      }

      public String getPath() {
         return this.path;
      }

      public Class<?> getClass(String var1) {
         return Class.forName(this + "." + var1);
      }

      public String toString() {
         return this.path;
      }

      public static String getServerVersion() {
         return Bukkit.getServer().getClass().getPackage().getName().substring(23);
      }
   }
}
