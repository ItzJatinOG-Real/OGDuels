package Itzjatinog.ogduels.economy;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.entity.Player;

public class EconomyRewardManager {
    private final OGDuels plugin;

    public EconomyRewardManager(OGDuels plugin) {
        this.plugin = plugin;
    }

    public void rewardWinner(Player winner, String matchCategory) {
        double payout = plugin.getConfig().getDouble("economy.win-rewards." + matchCategory, 20.0);
        plugin.getServer().getScheduler().runTask(plugin, () -> 
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "eco give " + winner.getName() + " " + payout)
        );
        winner.sendMessage(plugin.getConfigManager().getMessageRaw("reward-receive", "${amount}", String.valueOf(payout)));
    }
}
