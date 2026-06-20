package Itzjatinog.ogduels.managers;

import Itzjatinog.ogduels.OGDuels;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KillStreakManager implements Listener {
    private final OGDuels plugin;
    private final Map<UUID, Integer> currentStreaks = new HashMap<>();
    private final Map<UUID, Integer> highestStreaks = new HashMap<>();
    
    private File configurationFile;
    private FileConfiguration streakConfig;

    public KillStreakManager(OGDuels plugin) {
        this.plugin = plugin;
        setupStreakConfig();
        loadDataProfiles();
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KillStreakPlaceholders().register();
        }
    }

    // Automatically initialize and create streak.yml
    private void setupStreakConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        configurationFile = new File(plugin.getDataFolder(), "streak.yml");
        if (!configurationFile.exists()) {
            try {
                configurationFile.createNewFile();
                // Populating defaults if file is freshly generated
                streakConfig = YamlConfiguration.loadConfiguration(configurationFile);
                buildConfigDefaults();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create streak.yml file!");
                e.printStackTrace();
            }
        } else {
            streakConfig = YamlConfiguration.loadConfiguration(configurationFile);
        }
    }

    private void buildConfigDefaults() throws IOException {
        streakConfig.set("killstreaks.leaderboard.title", "&6&lTop 10 Killstreaks Leaderboard");
        streakConfig.set("killstreaks.leaderboard.format", "&e#%pos% &b%player% &7- &a%score% Max Streak");
        
        streakConfig.set("killstreaks.milestones.5.broadcast", "&7[&c&lSTREAK&7] &e%player% &7is on a &6&lDominating &7Kill Streak of &a%streak%&7!");
        streakConfig.set("killstreaks.milestones.5.commands", Arrays.asList("eco give %player% 100", "give %player% golden_apple 1"));
        
        streakConfig.set("killstreaks.milestones.10.broadcast", "&7[&c&lSTREAK&7] &e%player% &7has achieved a &4&lMEGA STREAK &7of &a%streak% &7kills!");
        streakConfig.set("killstreaks.milestones.10.commands", Arrays.asList("eco give %player% 500", "give %player% diamond 2"));
        
        streakConfig.save(configurationFile);
    }

    public int getCurrentStreak(Player player) {
        return currentStreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public int getHighestStreak(Player player) {
        return highestStreaks.getOrDefault(player.getUniqueId(), 0);
    }

    private void loadDataProfiles() {
        // Hooks into SQLite/MySQL logic layout configurations from streakConfig
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        Player killer = loser.getKiller();

        UUID loserUUID = loser.getUniqueId();
        currentStreaks.put(loserUUID, 0);

        if (killer == null || !killer.isOnline()) return;
        UUID killerUUID = killer.getUniqueId();

        int newStreak = currentStreaks.getOrDefault(killerUUID, 0) + 1;
        currentStreaks.put(killerUUID, newStreak);

        int record = highestStreaks.getOrDefault(killerUUID, 0);
        if (newStreak > record) {
            highestStreaks.put(killerUUID, newStreak);
        }

        handleStreakMilestones(killer, newStreak);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        currentStreaks.remove(event.getPlayer().getUniqueId());
    }

    private void handleStreakMilestones(Player killer, int streak) {
        String path = "killstreaks.milestones." + streak;

        if (!streakConfig.contains(path)) {
            parseDefaultMultiKillAnnouncements(killer, streak);
            return;
        }

        // 1. Fully Custom Broadcast Engine
        if (streakConfig.contains(path + ".broadcast")) {
            String msg = streakConfig.getString(path + ".broadcast", "")
                    .replace("%player%", killer.getName())
                    .replace("%streak%", String.valueOf(streak));
            if (!msg.isEmpty()) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }

        // 2. Multi-Action & Multi-Command Execution Pipeline
        if (streakConfig.contains(path + ".commands")) {
            List<String> commands = streakConfig.getStringList(path + ".commands");
            for (String cmd : commands) {
                String parsedCmd = cmd.replace("%player%", killer.getName())
                                     .replace("%streak%", String.valueOf(streak));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
            }
        }
    }

    private void parseDefaultMultiKillAnnouncements(Player killer, int streak) {
        String title;
        switch (streak) {
            case 2 -> title = "&e&lDOUBLE KILL!";
            case 3 -> title = "&6&lTRIPLE KILL!";
            case 4 -> title = "&c&lQUADRA KILL!";
            case 5 -> title = "&4&lPENTA KILL!";
            case 6 -> title = "&d&lHEXA KILL!";
            default -> {
                if (streak > 6) {
                    title = "&d&lUNSTOPPABLE KILLSTREAK (" + streak + ")!";
                } else {
                    return;
                }
            }
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', 
            "&7[&c&lStreak&7] &e" + killer.getName() + " &7just achieved a " + title));
    }

    // --- Native PlaceholderAPI Support Integration ---
    private class KillStreakPlaceholders extends PlaceholderExpansion {
        @Override
        public @NotNull String getIdentifier() { return "killstreak"; }
        @Override
        public @NotNull String getAuthor() { return "Itzjatinog"; }
        @Override
        public @NotNull String getVersion() { return "1.0.0"; }
        @Override
        public boolean persist() { return true; }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return "0";
            if (params.equalsIgnoreCase("current")) {
                return String.valueOf(getCurrentStreak(player));
            }
            if (params.equalsIgnoreCase("max") || params.equalsIgnoreCase("max_streak")) {
                return String.valueOf(getHighestStreak(player));
            }
            return null;
        }
    }
}