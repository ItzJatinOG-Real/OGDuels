package Itzjatinog.ogduels.managers;

import org.bukkit.entity.Player;
import java.util.*;

public class PartyManager {

    public static class Party {
        private final UUID leader;
        private final List<UUID> members = new ArrayList<>();

        public Party(UUID leader) {
            this.leader = leader;
        }

        public UUID getLeader() {
            return leader;
        }

        public List<UUID> getMembers() {
            return members;
        }
    }

    private final Map<UUID, Party> parties = new HashMap<>();
    private final Map<UUID, UUID> playerPartyMap = new HashMap<>();
    private final Map<UUID, UUID> partyInvites = new HashMap<>();

    public Party getParty(Player player) {
        UUID uuid = player.getUniqueId();
        if (parties.containsKey(uuid)) {
            return parties.get(uuid);
        }
        UUID leaderUUID = playerPartyMap.get(uuid);
        if (leaderUUID != null) {
            return parties.get(leaderUUID);
        }
        return null;
    }

    public void createParty(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInParty(player)) {
            leaveParty(player);
        }
        Party party = new Party(uuid);
        parties.put(uuid, party);
    }

    public void addPlayer(Party party, Player player) {
        if (party == null || player == null) return;
        UUID playerUUID = player.getUniqueId();
        
        if (isInParty(player)) {
            leaveParty(player);
        }

        party.getMembers().add(playerUUID);
        playerPartyMap.put(playerUUID, party.getLeader());
    }

    public void joinParty(Player leader, Player member) {
        Party party = getParty(leader);
        if (party == null) {
            createParty(leader);
            party = getParty(leader);
        }
        addPlayer(party, member);
    }

    public void leaveParty(Player player) {
        UUID uuid = player.getUniqueId();

        if (playerPartyMap.containsKey(uuid)) {
            UUID leaderUUID = playerPartyMap.remove(uuid);
            Party party = parties.get(leaderUUID);
            if (party != null) {
                party.getMembers().remove(uuid);
            }
            return;
        }

        if (parties.containsKey(uuid)) {
            Party party = parties.remove(uuid);
            for (UUID memberUUID : party.getMembers()) {
                playerPartyMap.remove(memberUUID);
            }
        }
    }

    public boolean isInParty(Player player) {
        UUID uuid = player.getUniqueId();
        return parties.containsKey(uuid) || playerPartyMap.containsKey(uuid);
    }

    public boolean isLeader(Player player) {
        return parties.containsKey(player.getUniqueId());
    }

    public void sendPartyInvite(UUID sender, UUID target) {
        partyInvites.put(target, sender);
    }

    public UUID getPartyInviteSender(UUID target) {
        return partyInvites.get(target);
    }

    public void clearPartyInvite(UUID target) {
        partyInvites.remove(target);
    }
}
