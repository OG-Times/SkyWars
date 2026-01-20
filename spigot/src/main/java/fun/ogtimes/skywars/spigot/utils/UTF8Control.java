package fun.ogtimes.skywars.spigot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class UTF8Control extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload
    ) throws IOException {

        String bundleName = toBundleName(baseName, locale);

        if ("java.class".equals(format)) {
            try {
                Class<?> bundleClass = loader.loadClass(bundleName);
                if (!ResourceBundle.class.isAssignableFrom(bundleClass)) {
                    throw new ClassCastException(
                            bundleClass.getName() + " cannot be cast to ResourceBundle"
                    );
                }
                return (ResourceBundle) bundleClass
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }

        if (!"java.properties".equals(format)) {
            throw new IllegalArgumentException("Unknown format: " + format);
        }

        String resourceName = toResourceName(bundleName, "properties");

        InputStream stream;
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            stream = connection.getInputStream();
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream == null) {
            return null;
        }

        try (InputStream is = stream;
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }
}