package Itzjatinog.ogduels.gui;

import Itzjatinog.ogduels.OGDuels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvitesGUI implements Listener {
    private final OGDuels plugin;

    public InvitesGUI(OGDuels plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void display(Player p) {
        FileConfiguration config = plugin.getConfig();
        String title = config.getString("invites-gui.title", "§8Invites Menu");
        int size = config.getInt("invites-gui.size", 27);
        
        Inventory inv = Bukkit.createInventory(null, size, Component.text(title));
        UUID pUUID = p.getUniqueId();

        // 1. Build Party Invite Item
        UUID partySenderUUID = plugin.getPartyManager().getPartyInviteSender(pUUID);
        int partySlot = config.getInt("invites-gui.party-item.slot", 11);
        if (partySenderUUID != null) {
            Player sender = Bukkit.getPlayer(partySenderUUID);
            String name = sender != null ? sender.getName() : "Unknown";
            inv.setItem(partySlot, buildConfiguredItem("party-item", name));
        } else {
            inv.setItem(partySlot, buildEmptyItem());
        }

        // 2. Build Duel Invite Item
        UUID duelSenderUUID = plugin.getMatchManager().getDuelInviteSender(pUUID);
        int duelSlot = config.getInt("invites-gui.duel-item.slot", 15);
        if (duelSenderUUID != null) {
            Player sender = Bukkit.getPlayer(duelSenderUUID);
            String name = sender != null ? sender.getName() : "Unknown";
            inv.setItem(duelSlot, buildConfiguredItem("duel-item", name));
        } else {
            inv.setItem(duelSlot, buildEmptyItem());
        }

        p.openInventory(inv);
    }

    private ItemStack buildConfiguredItem(String path, String senderName) {
        FileConfiguration c = plugin.getConfig();
        Material mat = Material.matchMaterial(c.getString("invites-gui." + path + ".material", "PAPER"));
        ItemStack item = new ItemStack(mat == null ? Material.PAPER : mat);
        ItemMeta meta = item.getItemMeta();

        String rawName = c.getString("invites-gui." + path + ".display-name", "Invite");
        meta.displayName(MiniMessage.miniMessage().deserialize(rawName));

        List<Component> loreLines = new ArrayList<>();
        for (String line : c.getStringList("invites-gui." + path + ".lore")) {
            loreLines.add(MiniMessage.miniMessage().deserialize(line.replace("{sender}", senderName)));
        }
        meta.lore(loreLines);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildEmptyItem() {
        FileConfiguration c = plugin.getConfig();
        Material mat = Material.matchMaterial(c.getString("invites-gui.empty-item.material", "BARRIER"));
        ItemStack item = new ItemStack(mat == null ? Material.BARRIER : mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(c.getString("invites-gui.empty-item.display-name", "")));
        
        List<Component> loreLines = new ArrayList<>();
        for (String line : c.getStringList("invites-gui.empty-item.lore")) {
            loreLines.add(MiniMessage.miniMessage().deserialize(line));
        }
        meta.lore(loreLines);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = plugin.getConfig().getString("invites-gui.title", "§8Invites Menu");
        if (!e.getView().title().equals(Component.text(title))) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || !(e.getWhoClicked() instanceof Player p)) return;
        int slot = e.getSlot();
        FileConfiguration c = plugin.getConfig();

        if (slot == c.getInt("invites-gui.party-item.slot", 11)) {
            UUID senderUUID = plugin.getPartyManager().getPartyInviteSender(p.getUniqueId());
            if (senderUUID == null) return;
            
            if (e.getClick() == ClickType.LEFT) {
                Player sender = Bukkit.getPlayer(senderUUID);
                if (sender != null) {
                    var party = plugin.getPartyManager().getParty(sender);
                    if (party != null) plugin.getPartyManager().addPlayer(party, p);
                }
            }
            plugin.getPartyManager().clearPartyInvite(p.getUniqueId());
            p.closeInventory();
        } 
        else if (slot == c.getInt("invites-gui.duel-item.slot", 15)) {
            UUID senderUUID = plugin.getMatchManager().getDuelInviteSender(p.getUniqueId());
            if (senderUUID == null) return;

            if (e.getClick() == ClickType.LEFT) {
                Player sender = Bukkit.getPlayer(senderUUID);
                if (sender != null) {
                    plugin.getMatchManager().launchDuel(List.of(sender), List.of(p));
                }
            }
            plugin.getMatchManager().clearDuelInvite(p.getUniqueId());
            p.closeInventory();
        }
    }
}
