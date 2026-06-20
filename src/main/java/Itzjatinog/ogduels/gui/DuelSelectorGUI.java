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

public class DuelSelectorGUI implements Listener {
    private final OGDuels plugin;
    private final Component guiTitle = Component.text("§8Match Category Selector");

    public DuelSelectorGUI(OGDuels plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void display(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, guiTitle);
        inv.setItem(11, buildItem(Material.DIAMOND_SWORD, "§a§lCompetitive Ranked"));
        inv.setItem(13, buildItem(Material.IRON_SWORD, "§e§lPractice Unranked"));
        inv.setItem(15, buildItem(Material.GOLDEN_APPLE, "§b§lTeam Party Entry"));
        p.openInventory(inv);
    }

    private ItemStack buildItem(Material m, String name) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(guiTitle)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || !(e.getWhoClicked() instanceof Player p)) return;
        p.closeInventory();
        new KitSelectorGUI(plugin).display(p);
    }
}
