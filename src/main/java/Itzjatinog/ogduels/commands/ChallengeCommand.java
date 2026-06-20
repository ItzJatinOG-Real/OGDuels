package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import Itzjatinog.ogduels.gui.DuelSelectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class ChallengeCommand implements CommandExecutor {
    private final OGDuels plugin;

    public ChallengeCommand(OGDuels plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return true;
        }
        if (args.length == 0) {
            new DuelSelectorGUI(plugin).display(player);
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) return true;

        plugin.getMatchManager().launchDuel(List.of(player), List.of(target));
        return true;
    }
}
