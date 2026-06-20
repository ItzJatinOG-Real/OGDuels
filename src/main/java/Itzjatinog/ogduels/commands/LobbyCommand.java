package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LobbyCommand implements CommandExecutor {
    private final OGDuels plugin;

    public LobbyCommand(OGDuels plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String l, String[] args) {
        if (!(s instanceof Player p)) return true;
        p.teleport(plugin.getConfigManager().getLobbyLocation());
        p.sendMessage(plugin.getConfigManager().getMessage("lobby-teleport"));
        return true;
    }
}
