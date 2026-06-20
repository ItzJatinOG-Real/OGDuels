package Itzjatinog.ogduels.party;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {
    private final Map<UUID, Party> listings = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> activePartyInvites = new ConcurrentHashMap<>(); // Target -> Sender
    private final Map<UUID, UUID> activeDuelInvites = new ConcurrentHashMap<>();  // Target -> Sender

    public void createParty(Player p) { 
        listings.put(p.getUniqueId(), new Party(p.getUniqueId())); 
    }
    
    public Party getParty(Player p) { 
        return listings.get(p.getUniqueId()); 
    }
    
    public void addPlayer(Party party, Player p) { 
        party.add(p.getUniqueId()); 
        listings.put(p.getUniqueId(), party); 
    }
    
    public void clear(Party party) { 
        party.getPlayers().forEach(listings::remove); 
    }

    public void sendPartyInvite(UUID sender, UUID target) { activePartyInvites.put(target, sender); }
    public UUID getPartyInviteSender(UUID target) { return activePartyInvites.get(target); }
    public void clearPartyInvite(UUID target) { activePartyInvites.remove(target); }

    public void sendDuelInvite(UUID sender, UUID target) { activeDuelInvites.put(target, sender); }
    public UUID getDuelInviteSender(UUID target) { return activeDuelInvites.get(target); }
    public void clearDuelInvite(UUID target) { activeDuelInvites.remove(target); }
}
