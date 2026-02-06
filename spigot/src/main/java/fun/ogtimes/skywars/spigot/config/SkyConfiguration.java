package fun.ogtimes.skywars.spigot.config;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
public class SkyConfiguration extends YamlConfiguration implements IConfiguration {
    private final EConfiguration econfig = new EConfiguration();
    private File file;

    public SkyConfiguration(File file) {
        this.file = file;

        try {
            this.load(file);
        } catch (FileNotFoundException ignored) {
        } catch (InvalidConfigurationException | IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }

    }

    public EConfiguration getEConfig() {
        return this.econfig;
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        this.file = file;
        super.load(file);
        super.options().header("");

        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            Bukkit.getLogger().log(Level.SEVERE, file.getName() + " doesn't have nothing to load");
            return;
        }

        boolean firstLineNotEmpty = !this.econfig.trim(lines.getFirst()).isEmpty();

        Map<String, List<String>> commentsMap = new LinkedHashMap<>();

        for (int index = 0; index < lines.size(); ++index) {
            String rawLine = lines.get(index);
            String trimmed = this.econfig.trimPrefixSpaces(rawLine);
            if (trimmed.startsWith("#") && (index > 0 || !firstLineNotEmpty)) {
                String path = this.econfig.getPathToComment(lines, index, rawLine);
                if (path != null) {
                    List<String> list = commentsMap.get(path);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(trimmed.substring(trimmed.startsWith("# ") ? 2 : 1));
                    commentsMap.put(path, list);
                }
            }
        }

        if (!commentsMap.isEmpty()) {
            this.econfig.getComments().putAll(commentsMap);
        }
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save " + this.file, ex);
        }

    }

    @Override
    public void save(File file) throws IOException {
        super.save(file);
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("");

            for (int i = 0; i < lines.size(); ++i) {
                String currentLine = lines.get(i);
                String path = null;
                if (!currentLine.startsWith("#") && currentLine.contains(":")) {
                    path = this.econfig.getPathToKey(lines, i, currentLine);
                }

                StringBuilder builder;
                if (path != null && this.econfig.getComments().containsKey(path)) {
                    int prefixSpaces = this.econfig.getPrefixSpaceCount(currentLine);
                    builder = new StringBuilder();
                    builder.append(" ".repeat(Math.max(0, prefixSpaces)));

                    List<String> comments = this.econfig.getComments().get(path);
                    if (comments != null) {
                        for (String comment : comments) {
                            writer.append(builder.toString()).append("# ").append(comment);
                            writer.newLine();
                        }
                    }
                }

                boolean isComment = currentLine.startsWith("#");
                if (!currentLine.startsWith("-") && !currentLine.startsWith("  -") && !currentLine.startsWith("    -") && !currentLine.startsWith("      -")) {
                    writer.append(currentLine);
                } else {
                    writer.append("  ").append(currentLine);
                }

                writer.newLine();
                if (this.econfig.shouldAddNewLinePerKey() && i < lines.size() - 1 && !isComment) {
                    String nextLine = lines.get(i + 1);
                    if (!nextLine.startsWith(" ") && !nextLine.startsWith("'") && !nextLine.startsWith("-")) {
                        writer.newLine();
                    }
                }
            }
        }

    }

    @Override
    public void set(String key, Object value) {
        if (value != null) {
            List<String> comments = this.econfig.getComments(key);
            if (!comments.isEmpty()) {
                this.econfig.getComments().put(key, comments);
            } else {
                this.econfig.getComments().remove(key);
            }
        } else {
            this.econfig.getComments().remove(key);
        }

        super.set(key, value);
    }

    public void addDefault(String key, Object value, String... comments) {
        if (value != null && comments != null && comments.length > 0 && !this.econfig.getComments().containsKey(key)) {
            List<String> list = new ArrayList<>();
            for (String c : comments) {
                list.add(Objects.requireNonNullElse(c, ""));
            }

            this.econfig.getComments().put(key, list);
        }

        super.addDefault(key, value);
    }

    public void createSection(String key, String... values) {
        if (key != null && values != null && values.length > 0) {
            List<String> list = new ArrayList<>();
            for (String v : values) {
                list.add(Objects.requireNonNullElse(v, ""));
            }

            this.econfig.getComments().put(key, list);
        }

        super.createSection(key);
    }

    public void setHeader(String... header) {
        StringBuilder sb = new StringBuilder();
        for (String h : header) {
            sb.append(h).append("\n");
        }

        super.options().header(sb.toString());
    }

    public void set(String key, Object value, String... comments) {
        if (value != null) {
            if (comments != null) {
                if (comments.length > 0) {
                    List<String> list = new ArrayList<>();
                    for (String c : comments) {
                        list.add(Objects.requireNonNullElse(c, ""));
                    }

                    this.econfig.getComments().put(key, list);
                } else {
                    this.econfig.getComments().remove(key);
                }
            }
        } else {
            this.econfig.getComments().remove(key);
        }

        super.set(key, value);
    }
}
