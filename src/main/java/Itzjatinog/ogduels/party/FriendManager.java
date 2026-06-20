package Itzjatinog.ogduels.party;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendManager implements Listener {
    private final OGDuels plugin;
    private final Map<UUID, Set<UUID>> friendRegistry = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();

    public FriendManager(OGDuels plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void sendFriendRequest(UUID sender, UUID target) {
        pendingRequests.computeIfAbsent(target, k -> ConcurrentHashMap.newKeySet()).add(sender);
    }

    public boolean acceptFriendRequest(UUID target, UUID sender) {
        if (!pendingRequests.containsKey(target) || !pendingRequests.get(target).contains(sender)) return false;
        pendingRequests.get(target).remove(sender);
        friendRegistry.computeIfAbsent(target, k -> ConcurrentHashMap.newKeySet()).add(sender);
        friendRegistry.computeIfAbsent(sender, k -> ConcurrentHashMap.newKeySet()).add(target);
        return true;
    }

    public Set<UUID> getFriends(UUID player) {
        return friendRegistry.getOrDefault(player, Collections.emptySet());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Set<UUID> friends = getFriends(p.getUniqueId());
        for (UUID fUUID : friends) {
            Player friend = Bukkit.getPlayer(fUUID);
            if (friend != null && friend.isOnline()) {
                friend.sendMessage(plugin.getConfigManager().getMessageRaw("friend-online", "{player}", p.getName()));
            }
        }
    }
}
