package Itzjatinog.ogduels;

import Itzjatinog.ogduels.commands.DuelPlayerCommand;
import Itzjatinog.ogduels.commands.OGDuelsCommand;
import Itzjatinog.ogduels.managers.PartyManager;
import Itzjatinog.ogduels.managers.QueueManager;
import Itzjatinog.ogduels.managers.KillStreakManager; // Imported your new manager
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class OGDuels extends JavaPlugin implements Listener {

    private PartyManager partyManager;
    private QueueManager queueManager;
    private KillStreakManager killStreakManager; // Class field tracking instance
    
    private File guisFile;
    private FileConfiguration guisConfig;

    private final DummyConfigManager configManager = new DummyConfigManager();
    private final DummyEconomyRewardManager economyRewardManager = new DummyEconomyRewardManager();
    private final DummyMatchManager matchManager = new DummyMatchManager();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createGuisConfig();

        this.partyManager = new PartyManager();
        this.queueManager = new QueueManager(this);
        this.killStreakManager = new KillStreakManager(this); // Initialized alongside existing profiles

        // Register all core listener modules
        getServer().getPluginManager().registerEvents(this.queueManager, this);
        getServer().getPluginManager().registerEvents(this.killStreakManager, this); // Registered killstreak events
        getServer().getPluginManager().registerEvents(this, this);

        DuelPlayerCommand duelPlayerCommand = new DuelPlayerCommand(this);
        
        // Safely map and attach command flows (including our target ffa command)
        String[] playerCommands = {"join", "party", "ffa", "unranked"};
        for (String cmd : playerCommands) {
            if (getCommand(cmd) != null) {
                getCommand(cmd).setExecutor(duelPlayerCommand);
                getCommand(cmd).setTabCompleter(duelPlayerCommand);
            }
        }

        OGDuelsCommand adminCmd = new OGDuelsCommand(this);
        if (getCommand("ogduels") != null) {
            getCommand("ogduels").setExecutor(adminCmd);
            getCommand("ogduels").setTabCompleter(adminCmd);
        }
    }

    // ======================================================
    // LOBBY PROTECTOR EVENT HANDLERS
    // ======================================================
    @EventHandler
    public void onLobbyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        String lobbyWorldName = getConfig().getString("lobby.world");
        if (lobbyWorldName != null && event.getEntity().getWorld().getName().equalsIgnoreCase(lobbyWorldName)) {
            // Cancel any damage (fall, fire, void, etc) within the fallback configuration lobby world
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLobbyPvP(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        String lobbyWorldName = getConfig().getString("lobby.world");
        if (lobbyWorldName != null && event.getDamager().getWorld().getName().equalsIgnoreCase(lobbyWorldName)) {
            // Stop players from hitting entities/other players inside the lobby
            event.setCancelled(true);
        }
    }

    @Override
    public void onDisable() {
    }

    // Manager Getters
    public PartyManager getPartyManager() {
        return partyManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public KillStreakManager getKillStreakManager() {
        return killStreakManager;
    }

    public DummyConfigManager getConfigManager() {
        return configManager;
    }

    public DummyEconomyRewardManager getEconomyRewardManager() {
        return economyRewardManager;
    }

    public DummyMatchManager getMatchManager() {
        return matchManager;
    }
    
    public void createGuisConfig() {
        guisFile = new File(getDataFolder(), "guis.yml");
        if (!guisFile.exists()) {
            guisFile.getParentFile().mkdirs();
            saveResource("guis.yml", false);
        }
        guisConfig = YamlConfiguration.loadConfiguration(guisFile);
    }

    public FileConfiguration getGuisConfig() {
        if (guisConfig == null) {
            createGuisConfig();
        }
        return guisConfig;
    }

    public void reloadGuisConfig() {
        if (guisFile == null) {
            guisFile = new File(getDataFolder(), "guis.yml");
        }
        guisConfig = YamlConfiguration.loadConfiguration(guisFile);
    }

    public static class DummyConfigManager {
        public org.bukkit.Location getLobbyLocation() { return null; }
        
        public String getMessage(String key) {
            if (key.equalsIgnoreCase("only-players")) return "§cOnly players can execute this command.";
            if (key.equalsIgnoreCase("lobby-teleport")) return "§aTeleporting to the lobby...";
            return "§7[" + key + "]";
        }
        
        public String getMessageRaw(String key, String placeholder, String replacement) {
            String msg = "§7[" + key + "]";
            if (key.equalsIgnoreCase("friend-online")) msg = "§aYour friend {player} is now online!";
            if (key.equalsIgnoreCase("reward-receive")) msg = "§aYou received ${amount} as a victory payout!";
            return msg.replace(placeholder, replacement);
        }
    }

    public static class DummyEconomyRewardManager {
        public void rewardWinner(org.bukkit.entity.Player p, String type) {}
    }

    public static class DummyMatchManager {
        public void launchDuel(java.util.List<org.bukkit.entity.Player> team1, java.util.List<org.bukkit.entity.Player> team2) {}
        public java.util.UUID getDuelInviteSender(java.util.UUID target) { return null; }
        public void clearDuelInvite(java.util.UUID target) {}
    }
}