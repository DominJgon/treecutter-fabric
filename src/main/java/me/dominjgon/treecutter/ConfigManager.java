package me.dominjgon.treecutter;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigManager {
    private final Map<String, ConfigValue> cache = new HashMap<>();
    private final Path configPath;
    private final Properties defaults;

    public ConfigManager(Path configPath, Properties defaults) {
        this.configPath = configPath;
        this.defaults = defaults;
    }

    public void load() {
        Properties props = new Properties();
        boolean needsSave = false;

        if(Files.exists(configPath)) {
            try(Reader reader = Files.newBufferedReader(configPath)) {
                props.load(reader);
            } catch(Exception e) {
                // log warning
            }
        }

        // Check if any default keys are missing
        for(String key : defaults.stringPropertyNames()) {
            if(!props.containsKey(key)) {
                props.setProperty(key, defaults.getProperty(key));
                needsSave = true;
            }
        }

        // Only write file if something was missing
        if(needsSave || !Files.exists(configPath)) {
            save(props);
        }

        // Cache all entries
        cache.clear();
        for(String key : props.stringPropertyNames()) {
            cache.put(key, new ConfigValue(props.getProperty(key)));
        }
    }

    public ConfigValue get(String key) {
        if (!cache.containsKey(key))
            throw new RuntimeException("Unknown config key: " + key);
        return cache.get(key);
    }

    private void save(Properties props) {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            props.store(writer, "Treecutter Configuration");
        } catch (Exception e) {
            // log error
            Treecutter.LogInfo("Error saving properties, {}", e);
        }
    }
}