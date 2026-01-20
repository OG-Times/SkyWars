package ak.CookLoco.SkyWars.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class UTF8Control extends Control {
   public ResourceBundle newBundle(String var1, Locale var2, String var3, ClassLoader var4, boolean var5) {
      String var6 = this.toBundleName(var1, var2);
      Object var7 = null;
      if (var3.equals("java.class")) {
         try {
            Class var8 = var4.loadClass(var6);
            if (!ResourceBundle.class.isAssignableFrom(var8)) {
               throw new ClassCastException(var8.getName() + " cannot be cast to ResourceBundle");
            }

            var7 = (ResourceBundle)var8.newInstance();
         } catch (ClassNotFoundException var19) {
         }
      } else {
         if (!var3.equals("java.properties")) {
            throw new IllegalArgumentException("unknown format: " + var3);
         }

         final String var20 = this.toResourceName(var6, "properties");
         final ClassLoader var9 = var4;
         final boolean var10 = var5;
         InputStream var11 = null;

         try {
            var11 = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
               public InputStream run() {
                  InputStream var1 = null;
                  if (var10) {
                     URL var2 = var9.getResource(var20);
                     if (var2 != null) {
                        URLConnection var3 = var2.openConnection();
                        if (var3 != null) {
                           var3.setUseCaches(false);
                           var1 = var3.getInputStream();
                        }
                     }
                  } else {
                     var1 = var9.getResourceAsStream(var20);
                  }

                  return var1;
               }
            });
         } catch (PrivilegedActionException var18) {
            throw (IOException)var18.getException();
         }

         if (var11 != null) {
            try {
               var7 = new PropertyResourceBundle(new InputStreamReader(var11, "UTF-8"));
            } finally {
               var11.close();
            }
         }
      }

      return (ResourceBundle)var7;
   }
}
