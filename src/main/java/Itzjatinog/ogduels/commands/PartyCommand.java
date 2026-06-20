package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import Itzjatinog.ogduels.managers.PartyManager.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyCommand implements CommandExecutor {
    private final OGDuels plugin;

    public PartyCommand(OGDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
            Player host = Bukkit.getPlayer(args[1]);
            if (host == null) {
                player.sendMessage("§cThat player is no longer online.");
                return true;
            }

            Party targetParty = plugin.getPartyManager().getParty(host);
            if (targetParty != null) {
                plugin.getPartyManager().addPlayer(targetParty, player);
                player.sendMessage("§aJoined the party!");
            } else {
                player.sendMessage("§cThat party session is no longer valid.");
            }
        }
        return true;
    }
}
