package Itzjatinog.ogduels.npc;

import Itzjatinog.ogduels.gui.DuelSelectorGUI;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class DuelNPCTrait extends Trait {
    public DuelNPCTrait() { super("duelnpc"); }

    @EventHandler
    public void onInteract(NPCRightClickEvent event) {
        if (event.getNPC() != this.getNPC()) return;
        Player p = event.getClicker();
        var plugin = (Itzjatinog.ogduels.OGDuels) Bukkit.getPluginManager().getPlugin("OGDuels");
        if (plugin != null) new DuelSelectorGUI(plugin).display(p);
    }
}
