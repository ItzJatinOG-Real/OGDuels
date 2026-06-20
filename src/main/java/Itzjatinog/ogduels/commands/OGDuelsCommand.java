package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OGDuelsCommand implements CommandExecutor, TabCompleter {
    private final OGDuels plugin;

    public OGDuelsCommand(OGDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!s.hasPermission("ogduels.admin")) {
            s.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            s.sendMessage("§e--- OGDuels Admin Configuration ---");
            s.sendMessage("§e/ogduels reload §7- Reload data parameters");
            s.sendMessage("§e/ogduels setlobby §7- Set global fallback lobby spawn location");
            s.sendMessage("§6-- Duel Setup commands --");
            s.sendMessage("§e/ogduels create <arena> §7- Instantiate new profile");
            s.sendMessage("§e/ogduels setspawn <arena> <1|2> §7- Track positions");
            s.sendMessage("§e/ogduels savekit <arena> §7- Save hotbar inventory");
            s.sendMessage("§6-- FFA Setup commands --");
            s.sendMessage("§e/ogduels createffa <arenaName> §7- Registers an active FFA target");
            s.sendMessage("§e/ogduels setffaspawn <arenaName> §7- Maps central drop zone vector");
            s.sendMessage("§e/ogduels setffakit <arenaName> §7- Stores active inventory loadout");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.reloadGuisConfig();
            s.sendMessage("§aConfiguration profile and GUIs reloaded cleanly.");
            return true;
        }

        if (!(s instanceof Player p)) {
            s.sendMessage("§cOnly players can configure map spaces.");
            return true;
        }

        if (sub.equalsIgnoreCase("setlobby")) {
            plugin.getConfig().set("lobby.world", p.getWorld().getName());
            plugin.getConfig().set("lobby.x", p.getLocation().getX());
            plugin.getConfig().set("lobby.y", p.getLocation().getY());
            plugin.getConfig().set("lobby.z", p.getLocation().getZ());
            plugin.saveConfig();
            p.sendMessage("§aFallback global lobby point established at your location.");
            return true;
        }

        if (args.length < 2) return true;
        String arenaId = args[1].toLowerCase();

        // ======================================================
        // ORIGINAL DUEL CONFIGURATION FEATURES
        // ======================================================
        if (sub.equalsIgnoreCase("create")) {
            plugin.getConfig().set("arenas." + arenaId + ".active", true);
            plugin.saveConfig();
            p.sendMessage("§aArena layout initialized: " + arenaId);
            return true;
        }

        if (sub.equalsIgnoreCase("setspawn") && args.length > 2) {
            String idx = args[2];
            String path = "arenas." + arenaId + ".spawn" + idx + ".";
            plugin.getConfig().set(path + "world", p.getWorld().getName());
            plugin.getConfig().set(path + "x", p.getLocation().getX());
            plugin.getConfig().set(path + "y", p.getLocation().getY());
            plugin.getConfig().set(path + "z", p.getLocation().getZ());
            plugin.getConfig().set(path + "yaw", p.getLocation().getYaw());
            plugin.getConfig().set(path + "pitch", p.getLocation().getPitch());
            plugin.saveConfig();
            p.sendMessage("§aSpawn point " + idx + " configured for arena: " + arenaId);
            return true;
        }

        if (sub.equalsIgnoreCase("savekit")) {
            ItemStack[] items = p.getInventory().getContents();
            plugin.getConfig().set("arenas." + arenaId + ".kit", items);
            plugin.saveConfig();
            p.sendMessage("§aInventory layout mapped for kit: " + arenaId);
            return true;
        }

        // ======================================================
        // ADDED FFA CONFIGURATION FEATURES
        // ======================================================
        if (sub.equalsIgnoreCase("createffa")) {
            plugin.getConfig().set("ffa-arenas." + arenaId + ".active", true);
            plugin.saveConfig();
            p.sendMessage("§aFFA Arena §e" + arenaId + " §ahas been allocated and toggled active!");
            return true;
        }

        if (sub.equalsIgnoreCase("setffaspawn")) {
            if (!plugin.getConfig().contains("ffa-arenas." + arenaId)) {
                p.sendMessage("§cArena §e" + arenaId + " §cdoes not exist. Create it first using /ogduels createffa!");
                return true;
            }
            String path = "ffa-arenas." + arenaId + ".spawn.";
            plugin.getConfig().set(path + "world", p.getWorld().getName());
            plugin.getConfig().set(path + "x", p.getLocation().getX());
            plugin.getConfig().set(path + "y", p.getLocation().getY());
            plugin.getConfig().set(path + "z", p.getLocation().getZ());
            plugin.getConfig().set(path + "yaw", p.getLocation().getYaw());
            plugin.getConfig().set(path + "pitch", p.getLocation().getPitch());
            plugin.saveConfig();
            p.sendMessage("§aSuccessfully marked mid spawn point vector for FFA §e" + arenaId);
            return true;
        }

        if (sub.equalsIgnoreCase("setffakit")) {
            if (!plugin.getConfig().contains("ffa-arenas." + arenaId)) {
                p.sendMessage("§cArena §e" + arenaId + " §cdoes not exist.");
                return true;
            }
            ItemStack[] items = p.getInventory().getContents();
            plugin.getConfig().set("ffa-arenas." + arenaId + ".kit", items);
            plugin.saveConfig();
            p.sendMessage("§aActive hotbar setup saved as custom kit for FFA Arena: §e" + arenaId);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.addAll(Arrays.asList("reload", "setlobby", "create", "setspawn", "savekit", "createffa", "setffaspawn", "setffakit"));
        } else if (args.length == 2) {
            String baseCmd = args[0].toLowerCase();
            if (baseCmd.equals("create") || baseCmd.equals("setspawn") || baseCmd.equals("savekit")) {
                if (plugin.getConfig().getConfigurationSection("arenas") != null) {
                    list.addAll(plugin.getConfig().getConfigurationSection("arenas").getKeys(false));
                }
            } else if (baseCmd.equals("createffa") || baseCmd.equals("setffaspawn") || baseCmd.equals("setffakit")) {
                if (plugin.getConfig().getConfigurationSection("ffa-arenas") != null) {
                    list.addAll(plugin.getConfig().getConfigurationSection("ffa-arenas").getKeys(false));
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setspawn")) {
            list.add("1");
            list.add("2");
        }
        return list;
    }
}