package Itzjatinog.ogduels.gui;

import Itzjatinog.ogduels.OGDuels;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitSelectorGUI implements Listener {
    private final OGDuels plugin;
    private final Component kitTitle = Component.text("§8Select Tactical Kit Setup");

    public KitSelectorGUI(OGDuels plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void display(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, kitTitle);
        inv.setItem(3, buildItem(Material.DIAMOND_CHESTPLATE, "§b§lNoDebuff Profile"));
        inv.setItem(5, buildItem(Material.FLINT_AND_STEEL, "§6§lBuildUHC Arena"));
        p.openInventory(inv);
    }

    private ItemStack buildItem(Material m, String name) {
        ItemStack i = new ItemStack(m);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(Component.text(name));
        i.setItemMeta(meta);
        return i;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(kitTitle)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || !(e.getWhoClicked() instanceof Player p)) return;
        
        p.closeInventory();
        p.sendMessage("§aQueued into kit selection environment matching parameters.");
    }
}