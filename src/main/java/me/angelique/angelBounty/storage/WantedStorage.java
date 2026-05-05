package me.angelique.angelBounty.storage;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.model.WantedData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WantedStorage {

    private final AngelBounty plugin;
    private final File file;

    public WantedStorage(AngelBounty plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "wanted-data.yml");
    }

    public Map<UUID, WantedData> load() {
        Map<UUID, WantedData> result = new HashMap<>();
        if (!file.exists()) {
            return result;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("players");
        if (section == null) {
            return result;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int kills = section.getInt(key + ".kills", 0);
                int stars = section.getInt(key + ".stars", 0);
                result.put(uuid, new WantedData(kills, stars));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    public void save(Map<UUID, WantedData> dataMap) {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection section = yaml.createSection("players");
        for (Map.Entry<UUID, WantedData> entry : dataMap.entrySet()) {
            String key = entry.getKey().toString();
            WantedData data = entry.getValue();
            section.set(key + ".kills", data.getPlayerKills());
            section.set(key + ".stars", data.getStars());
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder.");
                return;
            }
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save wanted-data.yml: " + exception.getMessage());
        }
    }
}
