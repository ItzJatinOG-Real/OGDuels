package Itzjatinog.ogduels.config;

import Itzjatinog.ogduels.OGDuels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final OGDuels plugin;
    private String prefix;

    public ConfigManager(OGDuels plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.prefix = plugin.getConfig().getString("prefix", "<gray>[<blue>OGDuels</blue>] ");
    }

    public Component getMessage(String path) {
        return MiniMessage.miniMessage().deserialize(prefix + plugin.getConfig().getString("messages." + path, ""));
    }

    public Component getMessageRaw(String path, String target, String replacement) {
        String msg = plugin.getConfig().getString("messages." + path, "").replace(target, replacement);
        return MiniMessage.miniMessage().deserialize(prefix + msg);
    }

    public Location getLobbyLocation() {
        FileConfiguration c = plugin.getConfig();
        if (!c.contains("lobby-location.world")) return Bukkit.getWorlds().get(0).getSpawnLocation();
        return new Location(Bukkit.getWorld(c.getString("lobby-location.world")),
                c.getDouble("lobby-location.x"), c.getDouble("lobby-location.y"), c.getDouble("lobby-location.z"),
                (float) c.getDouble("lobby-location.yaw"), (float) c.getDouble("lobby-location.pitch"));
    }
}
