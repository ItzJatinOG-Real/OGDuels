package Itzjatinog.ogduels.engine;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MatchManager {
    private final OGDuels plugin;
    private final List<DuelMatch> matchingContexts = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Map<UUID, UUID> activeDuelInvites = new ConcurrentHashMap<>();

    public MatchManager(OGDuels plugin) { this.plugin = plugin; }

    public void launchDuel(List<Player> sideA, List<Player> sideB) {
        DuelMatch match = new DuelMatch(plugin, this, sideA, sideB);
        matchingContexts.add(match);
        match.startCountdown();
    }

    public void sendDuelInvite(UUID sender, UUID target) { activeDuelInvites.put(target, sender); }
    public UUID getDuelInviteSender(UUID target) { return activeDuelInvites.get(target); }
    public void clearDuelInvite(UUID target) { activeDuelInvites.remove(target); }

    public void terminateMatchInstance(DuelMatch match) { matchingContexts.remove(match); }
    public void terminateAllMatches() { matchingContexts.forEach(m -> m.endMatch(null, "unranked-1v1")); matchingContexts.clear(); }
}
