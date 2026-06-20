package Itzjatinog.ogduels.engine;

import Itzjatinog.ogduels.OGDuels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelMatch {
    private final OGDuels plugin;
    private final MatchManager manager;
    private final List<UUID> competitiveSquad1 = new ArrayList<>();
    private final List<UUID> competitiveSquad2 = new ArrayList<>();
    private final List<BlockState> trackingBlockLogs = new ArrayList<>();

    public DuelMatch(OGDuels plugin, MatchManager manager, List<Player> t1, List<Player> t2) {
        this.plugin = plugin;
        this.manager = manager;
        t1.forEach(p -> competitiveSquad1.add(p.getUniqueId()));
        t2.forEach(p -> competitiveSquad2.add(p.getUniqueId()));
    }

    public void startCountdown() {
        new BukkitRunnable() {
            int time = 5;
            @Override
            public void run() {
                if (time > 0) {
                    broadcastMatchTitle("<red><bold>" + time, "<gray>Get ready to fight!");
                    broadcastMatchSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
                    time--;
                } else {
                    broadcastMatchTitle("<green><bold>FIGHT!", "");
                    broadcastMatchSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void logBlockState(BlockState state) { trackingBlockLogs.add(state); }

    public void endMatch(Player victor, String trackingMode) {
        if (victor != null) plugin.getEconomyRewardManager().rewardWinner(victor, trackingMode);
        trackingBlockLogs.forEach(b -> b.update(true, false));
        trackingBlockLogs.clear();
        manager.terminateMatchInstance(this);
    }

    private void broadcastMatchTitle(String main, String sub) {
        Component m = MiniMessage.miniMessage().deserialize(main);
        Component s = MiniMessage.miniMessage().deserialize(sub);
        Title t = Title.title(m, s, Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ZERO));
        executeOnAllActive(p -> p.showTitle(t));
    }

    private void broadcastMatchSound(Sound s, float p) {
        executeOnAllActive(player -> player.playSound(player.getLocation(), s, 1.0f, p));
    }

    public void executeOnAllActive(java.util.function.Consumer<Player> action) {
        competitiveSquad1.stream().map(Bukkit::getPlayer).filter(java.util.Objects::nonNull).forEach(action);
        competitiveSquad2.stream().map(Bukkit::getPlayer).filter(java.util.Objects::nonNull).forEach(action);
    }

    public List<UUID> getTeam1() { return competitiveSquad1; }
    public List<UUID> getTeam2() { return competitiveSquad2; }
}
