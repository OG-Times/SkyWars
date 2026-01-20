package fun.ogtimes.skywars.spigot.config;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class SkyConfiguration extends YamlConfiguration implements IConfiguration {
    private EConfiguration econfig = new EConfiguration();
    @Getter
    private File file;

    public SkyConfiguration(File var1) {
        this.file = var1;

        try {
            this.load(var1);
        } catch (FileNotFoundException ignored) {
        } catch (InvalidConfigurationException | IOException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + var1, var4);
        }

    }

    public EConfiguration getEConfig() {
        return this.econfig;
    }

    public void load(File var1) throws IOException, InvalidConfigurationException {
        this.file = var1;
        super.load(var1);
        super.options().header("");
        BufferedReader var2 = null;
        ArrayList var3 = new ArrayList();

        try {
            var2 = new BufferedReader(new FileReader(var1));

            String var4;
            while((var4 = var2.readLine()) != null) {
                var3.add(var4);
            }
        } finally {
            if (var2 != null) {
                var2.close();
            }

        }

        if (var3.isEmpty()) {
            Bukkit.getLogger().log(Level.SEVERE, var1.getName() + " doesn't have nothing to load");
        } else {
            boolean var13 = !this.econfig.trim((String)var3.get(0)).isEmpty();
            LinkedHashMap var5 = new LinkedHashMap();

            for(int var6 = 0; var6 < var3.size(); ++var6) {
                String var7 = (String)var3.get(var6);
                String var8 = this.econfig.trimPrefixSpaces(var7);
                if (var8.startsWith("#") && (var6 > 0 || !var13)) {
                    String var9 = this.econfig.getPathToComment(var3, var6, var7);
                    if (var9 != null) {
                        Object var10 = (List)var5.get(var9);
                        if (var10 == null) {
                            var10 = new ArrayList();
                        }

                        ((List)var10).add(var8.substring(var8.startsWith("# ") ? 2 : 1));
                        var5.put(var9, var10);
                    }
                }
            }

        }
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void save(File var1) throws IOException {
        super.save(var1);
        ArrayList var2 = new ArrayList();
        BufferedReader var3 = null;

        try {
            var3 = new BufferedReader(new FileReader(var1));

            String var4;
            while((var4 = var3.readLine()) != null) {
                var2.add(var4);
            }
        } finally {
            if (var3 != null) {
                var3.close();
            }

        }

        BufferedWriter var20 = null;

        try {
            var20 = new BufferedWriter(new FileWriter(var1));
            var20.write("");

            for(int var5 = 0; var5 < var2.size(); ++var5) {
                String var6 = (String)var2.get(var5);
                String var7 = null;
                if (!var6.startsWith("#") && var6.contains(":")) {
                    var7 = this.econfig.getPathToKey(var2, var5, var6);
                }

                String var9;
                if (var7 != null && this.econfig.getComments().containsKey(var7)) {
                    int var8 = this.econfig.getPrefixSpaceCount(var6);
                    var9 = "";

                    for(int var10 = 0; var10 < var8; ++var10) {
                        var9 = var9 + " ";
                    }

                    List var22 = (List)this.econfig.getComments().get(var7);
                    if (var22 != null) {
                        Iterator var11 = var22.iterator();

                        while(var11.hasNext()) {
                            String var12 = (String)var11.next();
                            var20.append(var9).append("# ").append(var12);
                            var20.newLine();
                        }
                    }
                }

                boolean var21 = var6.startsWith("#");
                if (!var6.startsWith("-") && !var6.startsWith("  -") && !var6.startsWith("    -") && !var6.startsWith("      -")) {
                    var20.append(var6);
                } else {
                    var20.append("  " + var6);
                }

                var20.newLine();
                if (this.econfig.shouldAddNewLinePerKey() && var5 < var2.size() - 1 && !var21) {
                    var9 = (String)var2.get(var5 + 1);
                    if (var9 != null && !var9.startsWith(" ") && !var9.startsWith("'") && !var9.startsWith("-")) {
                        var20.newLine();
                    }
                }
            }
        } finally {
            if (var20 != null) {
                var20.close();
            }

        }

    }

    public void set(String var1, Object var2) {
        if (var2 != null) {
            if (this.econfig.getComments(var1).size() > 0) {
                this.econfig.getComments().put(var1, this.econfig.getComments(var1));
            } else {
                this.econfig.getComments().remove(var1);
            }
        } else {
            this.econfig.getComments().remove(var1);
        }

        super.set(var1, var2);
    }

    public void addDefault(String var1, Object var2, String... var3) {
        if (var2 != null && var3 != null && var3.length > 0 && !this.econfig.getComments().containsKey(var1)) {
            ArrayList var4 = new ArrayList();
            String[] var5 = var3;
            int var6 = var3.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String var8 = var5[var7];
                if (var8 != null) {
                    var4.add(var8);
                } else {
                    var4.add("");
                }
            }

            this.econfig.getComments().put(var1, var4);
        }

        super.addDefault(var1, var2);
    }

    public void createSection(String var1, String... var2) {
        if (var1 != null && var2 != null && var2.length > 0) {
            ArrayList var3 = new ArrayList();
            String[] var4 = var2;
            int var5 = var2.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String var7 = var4[var6];
                if (var7 != null) {
                    var3.add(var7);
                } else {
                    var3.add("");
                }
            }

            this.econfig.getComments().put(var1, var3);
        }

        super.createSection(var1);
    }

    public void setHeader(String... var1) {
        String var2 = "";
        String[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];
            var2 = var2 + var6 + "\n";
        }

        super.options().header(var2);
    }

    public void set(String var1, Object var2, String... var3) {
        if (var2 != null) {
            if (var3 != null) {
                if (var3.length > 0) {
                    ArrayList var4 = new ArrayList();
                    String[] var5 = var3;
                    int var6 = var3.length;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        String var8 = var5[var7];
                        if (var8 != null) {
                            var4.add(var8);
                        } else {
                            var4.add("");
                        }
                    }

                    this.econfig.getComments().put(var1, var4);
                } else {
                    this.econfig.getComments().remove(var1);
                }
            }
        } else {
            this.econfig.getComments().remove(var1);
        }

        super.set(var1, var2);
    }
}
