package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import Itzjatinog.ogduels.gui.DuelSelectorGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuelCommand implements CommandExecutor {
    private final OGDuels plugin;

    public DuelCommand(OGDuels plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, String[] args) {
        if (s instanceof Player player) {
            // Intercept command name or first argument execution for ffa
            if (l.equalsIgnoreCase("ffa") || (args.length > 0 && args[0].equalsIgnoreCase("ffa"))) {
                if (!player.hasPermission("ogduels.ffa")) {
                    player.sendMessage("§cYou do not have permission to join the FFA arena.");
                    return true;
                }
                // Open the dynamic FFA Selection interface populated from guis.yml
                plugin.getQueueManager().openFFAArenaGUI(player);
                return true;
            }

            new DuelSelectorGUI(plugin).display(player);
        }
        return true;
    }
}