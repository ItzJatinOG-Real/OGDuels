package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class AdminArenaCommand implements CommandExecutor, TabCompleter {
    private final OGDuels plugin;
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

    public AdminArenaCommand(OGDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // Handle reload first so console can run it if needed
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ogduels.admin")) {
                sender.sendMessage("§cYou do not have permission to execute this command.");
                return true;
            }
            plugin.reloadConfig();
            plugin.reloadGuisConfig();
            sender.sendMessage("§a[OGDuels] Configuration files and GUIs reloaded successfully!");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly in-game users can manage arena setups.");
            return true;
        }

        if (!player.hasPermission("ogduels.admin")) {
            player.sendMessage("§cYou do not have permission to execute this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        UUID uuid = player.getUniqueId();

        switch (args[0].toLowerCase()) {
            case "pos1":
                pos1Map.put(uuid, player.getLocation().getBlock().getLocation());
                player.sendMessage("§aPosition 1 marked at your current coordinates!");
                break;

            case "pos2":
                pos2Map.put(uuid, player.getLocation().getBlock().getLocation());
                player.sendMessage("§aPosition 2 marked at your current coordinates!");
                break;

            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /ogduels create <arenaName>");
                    return true;
                }
                String name = args[1].toLowerCase();
                config.set("arenas." + name + ".active", true);
                plugin.saveConfig();
                player.sendMessage("§aArena §e" + name + " §ahas been allocated and toggled active!");
                break;

            case "setspawn1":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /ogduels setspawn1 <arenaName>");
                    return true;
                }
                saveSpawn(player, args[1].toLowerCase(), "spawn1");
                break;

            case "setspawn2":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /ogduels setspawn2 <arenaName>");
                    return true;
                }
                saveSpawn(player, args[1].toLowerCase(), "spawn2");
                break;

            case "setkit":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /ogduels setkit <arenaName>");
                    return true;
                }
                String kitArena = args[1].toLowerCase();
                config.set("arenas." + kitArena + ".kit", Arrays.asList(player.getInventory().getContents()));
                plugin.saveConfig();
                player.sendMessage("§aInventory kit mapped to arena: §e" + kitArena);
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void saveSpawn(Player p, String arena, String spawnKey) {
        FileConfiguration config = plugin.getConfig();
        String path = "arenas." + arena + "." + spawnKey + ".";
        Location loc = p.getLocation();
        
        config.set(path + "world", loc.getWorld().getName());
        config.set(path + "x", loc.getX());
        config.set(path + "y", loc.getY());
        config.set(path + "z", loc.getZ());
        config.set(path + "yaw", loc.getYaw());
        config.set(path + "pitch", loc.getPitch());
        
        plugin.saveConfig();
        p.sendMessage("§aSuccessfully updated §e" + spawnKey + " §afor §e" + arena);
    }

    private void sendHelp(Player p) {
        p.sendMessage("§7§m======================================");
        p.sendMessage("§6§lOGDuels Admin Panel Utility Guide");
        p.sendMessage("§e/ogduels reload §7- Hot-reloads configuration layouts");
        p.sendMessage("§e/ogduels pos1 §7- Sets position 1 boundary node");
        p.sendMessage("§e/ogduels pos2 §7- Sets position 2 boundary node");
        p.sendMessage("§e/ogduels create <name> §7- Registers an active arena target");
        p.sendMessage("§e/ogduels setspawn1 <name> §7- Maps first player spawn vector");
        p.sendMessage("§e/ogduels setspawn2 <name> §7- Maps second player spawn vector");
        p.sendMessage("§e/ogduels setkit <name> §7- Stores your active hotbar kit configuration");
        p.sendMessage("§7§m======================================");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (!s.hasPermission("ogduels.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "pos1", "pos2", "create", "setspawn1", "setspawn2", "setkit");
        }
        
        if (args.length == 2 && !args[0].equalsIgnoreCase("pos1") && !args[0].equalsIgnoreCase("pos2") && !args[0].equalsIgnoreCase("reload")) {
            if (plugin.getConfig().getConfigurationSection("arenas") != null) {
                return new ArrayList<>(plugin.getConfig().getConfigurationSection("arenas").getKeys(false));
            }
        }
        return Collections.emptyList();
    }
}