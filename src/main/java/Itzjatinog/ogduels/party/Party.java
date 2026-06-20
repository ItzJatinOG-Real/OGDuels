package Itzjatinog.ogduels.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final UUID leader;
    private final Set<UUID> members;

    public Party(UUID leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader);
    }

    public UUID getLeader() {
        return leader;
    }

    // Used by managers/PartyManager.java
    public Set<UUID> getMembers() {
        return members;
    }

    // Used by party/PartyManager.java
    public Set<UUID> getPlayers() {
        return members;
    }

    // Used by party/PartyManager.java
    public void add(UUID uuid) {
        members.add(uuid);
    }

    // Used by managers/PartyManager.java
    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    // Used by managers/PartyManager.java
    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}
