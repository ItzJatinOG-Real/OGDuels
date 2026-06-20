package Itzjatinog.ogduels.commands;

import Itzjatinog.ogduels.OGDuels;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class DuelPlayerCommand implements CommandExecutor, TabCompleter, Listener {
    private final OGDuels plugin;
    private final Map<UUID, UUID> pendingPartyInvites = new HashMap<>();

    public DuelPlayerCommand(OGDuels plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(s instanceof Player p)) return true;

        // Catch the /ffa command directly or /duel ffa /join ffa variants
        if (cmd.getName().equalsIgnoreCase("ffa") || label.equalsIgnoreCase("ffa") || (args.length > 0 && args[0].equalsIgnoreCase("ffa"))) {
            if (!p.hasPermission("ogduels.ffa")) {
                p.sendMessage("§cYou do not have permission to join the FFA arena.");
                return true;
            }
            if (args.length > 0 && !args[0].equalsIgnoreCase("ffa")) {
                // If they typed '/ffa <arena_name>', process dynamic entry profile directly
                plugin.getQueueManager().joinFFAArena(p, args[0].toLowerCase());
            } else {
                // Otherwise, open up the selection interface populated from guis.yml
                plugin.getQueueManager().openFFAArenaGUI(p);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("join") || cmd.getName().equalsIgnoreCase("unranked")) {
            if (plugin.getPartyManager().isInParty(p) && !plugin.getPartyManager().isLeader(p)) {
                p.sendMessage("§cOnly the party leader can select maps or queue.");
                return true;
            }
            if (args.length > 0) {
                plugin.getQueueManager().toggleQueue(p, args[0].toLowerCase());
            } else {
                plugin.getQueueManager().openArenaGUI(p);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("party")) {
            if (args.length == 0) {
                openPartyGUI(p);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    plugin.getPartyManager().createParty(p);
                    p.sendMessage("§aParty successfully generated!");
                    break;
                case "leave":
                    plugin.getPartyManager().leaveParty(p);
                    p.sendMessage("§cYou left the party.");
                    break;
                case "invite":
                    if (args.length > 1) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null && target != p) {
                            pendingPartyInvites.put(target.getUniqueId(), p.getUniqueId());
                            p.sendMessage("§aParty invite sent to " + target.getName());
                            target.sendMessage("§a" + p.getName() + " invited you to join their party!");
                        }
                    }
                    break;
                case "accept":
                    UUID inviterId = pendingPartyInvites.remove(p.getUniqueId());
                    if (inviterId != null) {
                        Player inviter = Bukkit.getPlayer(inviterId);
                        if (inviter != null) {
                            plugin.getPartyManager().joinParty(inviter, p);
                            p.sendMessage("§aSuccessfully connected to " + inviter.getName() + "'s party.");
                        }
                    } else {
                        p.sendMessage("§cNo active invitations found.");
                    }
                    break;
            }
        }
        return true;
    }

    public void openPartyGUI(Player player) {
        FileConfiguration guiFile = plugin.getGuisConfig();
        String title = guiFile.getString("party-menu.title", "§8Party Management Profile");
        int size = guiFile.getInt("party-menu.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Profile Card
        int profSlot = guiFile.getInt("party-menu.items.profile.slot", 10);
        Material profMat = Material.matchMaterial(guiFile.getString("party-menu.items.profile.material", "BOOK"));
        ItemStack profileCard = new ItemStack(profMat != null ? profMat : Material.BOOK);
        ItemMeta profMeta = profileCard.getItemMeta();
        if (profMeta != null) {
            profMeta.setDisplayName(guiFile.getString("party-menu.items.profile.display-name", "§eYour Active Profile"));
            boolean active = plugin.getPartyManager().isInParty(player);
            List<String> rawLore = guiFile.getStringList(active ? "party-menu.items.profile.lore-party" : "party-menu.items.profile.lore-solo");
            List<String> lore = new ArrayList<>();
            String roleStr = plugin.getPartyManager().isLeader(player) ? "§6Leader" : "§aMember";
            for (String line : rawLore) {
                lore.add(line.replace("%role%", roleStr));
            }
            profMeta.setLore(lore);
            profileCard.setItemMeta(profMeta);
        }
        gui.setItem(profSlot, profileCard);

        // Invite Exec Panel
        int invSlot = guiFile.getInt("party-menu.items.invite.slot", 13);
        Material invMat = Material.matchMaterial(guiFile.getString("party-menu.items.invite.material", "PLAYER_HEAD"));
        ItemStack inviteCard = new ItemStack(invMat != null ? invMat : Material.PLAYER_HEAD);
        ItemMeta invMeta = inviteCard.getItemMeta();
        if (invMeta != null) {
            invMeta.setDisplayName(guiFile.getString("party-menu.items.invite.display-name", "§aInvite Active Online Players"));
            invMeta.setLore(guiFile.getStringList("party-menu.items.invite.lore"));
            inviteCard.setItemMeta(invMeta);
        }
        gui.setItem(invSlot, inviteCard);

        // Pending Checker
        int pendSlot = guiFile.getInt("party-menu.items.pending.slot", 16);
        Material pendMat = Material.matchMaterial(guiFile.getString("party-menu.items.pending.material", "WRITABLE_BOOK"));
        ItemStack invitesChecker = new ItemStack(pendMat != null ? pendMat : Material.WRITABLE_BOOK);
        ItemMeta checkMeta = invitesChecker.getItemMeta();
        if (checkMeta != null) {
            checkMeta.setDisplayName(guiFile.getString("party-menu.items.pending.display-name", "§bReview Pending Invites"));
            UUID sender = pendingPartyInvites.get(player.getUniqueId());
            List<String> rawLore = guiFile.getStringList(sender != null ? "party-menu.items.pending.lore-has-invite" : "party-menu.items.pending.lore-no-invite");
            List<String> lore = new ArrayList<>();
            Player senderP = sender != null ? Bukkit.getPlayer(sender) : null;
            String nameStr = senderP != null ? senderP.getName() : "Unknown";
            for (String line : rawLore) {
                lore.add(line.replace("%sender%", nameStr));
            }
            checkMeta.setLore(lore);
            invitesChecker.setItemMeta(checkMeta);
        }
        gui.setItem(pendSlot, invitesChecker);

        player.openInventory(gui);
    }

    public void openOnlinePlayersGUI(Player player) {
        FileConfiguration guiFile = plugin.getGuisConfig();
        String title = guiFile.getString("invite-players-menu.title", "§8Invite Active Players");
        int size = guiFile.getInt("invite-players-menu.size", 36);
        Inventory gui = Bukkit.createInventory(null, size, title);

        Material mat = Material.matchMaterial(guiFile.getString("invite-players-menu.player-item.material", "PLAYER_HEAD"));
        if (mat == null) mat = Material.PLAYER_HEAD;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == player) continue;
            ItemStack skull = new ItemStack(mat);
            ItemMeta m = skull.getItemMeta();
            if (m != null) {
                String rawName = guiFile.getString("invite-players-menu.player-item.display-name", "§f%player%");
                m.setDisplayName(rawName.replace("%player%", online.getName()));
                m.setLore(guiFile.getStringList("invite-players-menu.player-item.lore"));
                skull.setItemMeta(m);
            }
            gui.addItem(skull);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        FileConfiguration guiFile = plugin.getGuisConfig();
        String mainTitle = guiFile.getString("party-menu.title", "§8Party Management Profile");
        String inviteTitle = guiFile.getString("invite-players-menu.title", "§8Invite Active Players");
        
        String title = event.getView().getTitle();
        if (!title.equals(mainTitle) && !title.equals(inviteTitle)) return;
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = item.getItemMeta().getDisplayName();
        String action = null;

        if (title.equals(mainTitle)) {
            String inviteTargetName = guiFile.getString("party-menu.items.invite.display-name", "§aInvite Active Online Players");
            String pendTargetName = guiFile.getString("party-menu.items.pending.display-name", "§bReview Pending Invites");
            String profTargetName = guiFile.getString("party-menu.items.profile.display-name", "§eYour Active Profile");

            if (name.equals(inviteTargetName)) {
                action = guiFile.getString("party-menu.items.invite.click-action", "[OPEN_GUI] invite-players-menu");
            } else if (name.equals(pendTargetName)) {
                action = guiFile.getString("party-menu.items.pending.click-action", "[COMMAND] party accept");
            } else if (name.equals(profTargetName)) {
                action = guiFile.getString("party-menu.items.profile.click-action", "[CLOSE]");
            }
        } else if (title.equals(inviteTitle)) {
            action = guiFile.getString("invite-players-menu.player-item.click-action", "[COMMAND] party invite %player%");
            String rawTemplate = guiFile.getString("invite-players-menu.player-item.display-name", "§f%player%").replace("%player%", "");
            String strippedTemplate = org.bukkit.ChatColor.stripColor(rawTemplate);
            String targetPlayerName = org.bukkit.ChatColor.stripColor(name).replace(strippedTemplate, "").trim();
            if (action != null) {
                action = action.replace("%player%", targetPlayerName);
            }
        }

        if (action != null) {
            executeCustomAction(player, action);
        }
    }

    private void executeCustomAction(Player player, String action) {
        if (action.equalsIgnoreCase("[CLOSE]")) {
            player.closeInventory();
        } else if (action.equalsIgnoreCase("[OPEN_GUI] invite-players-menu")) {
            openOnlinePlayersGUI(player);
        } else if (action.equalsIgnoreCase("[OPEN_GUI] party-menu")) {
            openPartyGUI(player);
        } else if (action.startsWith("[COMMAND]")) {
            String cmd = action.replace("[COMMAND]", "").trim();
            player.closeInventory();
            player.performCommand(cmd);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        
        if ((cmd.getName().equalsIgnoreCase("join") || cmd.getName().equalsIgnoreCase("unranked")) && args.length == 1) {
            if (plugin.getConfig().getConfigurationSection("arenas") != null) {
                list.addAll(plugin.getConfig().getConfigurationSection("arenas").getKeys(false));
            }
        } else if (cmd.getName().equalsIgnoreCase("ffa") && args.length == 1) {
            if (plugin.getConfig().getConfigurationSection("ffa-arenas") != null) {
                list.addAll(plugin.getConfig().getConfigurationSection("ffa-arenas").getKeys(false));
            }
        } else if (cmd.getName().equalsIgnoreCase("party")) {
            if (args.length == 1) {
                list.addAll(Arrays.asList("create", "invite", "leave", "accept"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
            }
        }
        return list;
    }
}