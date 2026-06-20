package Itzjatinog.ogduels.managers;

import Itzjatinog.ogduels.OGDuels;
import Itzjatinog.ogduels.managers.PartyManager.Party;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class QueueManager implements Listener {
    private final OGDuels plugin;
    private final Map<String, List<UUID>> soloQueues = new HashMap<>();
    private final Map<UUID, UUID> activeMatches = new HashMap<>();
    private final Map<UUID, String> playerArenaMap = new HashMap<>();
    private final Map<UUID, String> activeFFAPlayers = new HashMap<>();
    private final Map<String, Map<Location, BlockState>> arenaBlockRollbacks = new HashMap<>();

    public QueueManager(OGDuels plugin) {
        this.plugin = plugin;
    }

    public boolean isCurrentlyFighting(Player player) {
        return activeMatches.containsKey(player.getUniqueId()) || activeFFAPlayers.containsKey(player.getUniqueId());
    }

    public boolean isInFFA(Player player) {
        return activeFFAPlayers.containsKey(player.getUniqueId());
    }

    public int getQueueCount(String arena) {
        List<UUID> q = soloQueues.get(arena.toLowerCase());
        return q != null ? q.size() : 0;
    }

    public int getInMatchCount(String arena) {
        int count = 0;
        for (String activeArena : playerArenaMap.values()) {
            if (activeArena.equalsIgnoreCase(arena)) count++;
        }
        return count;
    }

    public int getFFAArenaPlayerCount(String arena) {
        int count = 0;
        for (String ffaArena : activeFFAPlayers.values()) {
            if (ffaArena.equalsIgnoreCase(arena)) count++;
        }
        return count;
    }

    public void openArenaGUI(Player player) {
        FileConfiguration guiFile = plugin.getGuisConfig();
        
        if (!guiFile.getBoolean("arenas-menu.enabled", true)) {
            String fallback = guiFile.getString("arenas-menu.disabled-message", "§cThis GUI is disabled.");
            player.sendMessage(fallback);
            return;
        }

        String title = guiFile.getString("arenas-menu.title", "Arenas Matchmaking");
        int size = guiFile.getInt("arenas-menu.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        if (guiFile.getConfigurationSection("arenas-menu.extra-items") != null) {
            for (String extraKey : guiFile.getConfigurationSection("arenas-menu.extra-items").getKeys(false)) {
                String path = "arenas-menu.extra-items." + extraKey + ".";
                String matName = guiFile.getString(path + "material", "AIR");
                Material mat = Material.matchMaterial(matName);
                if (mat == null || mat == Material.AIR) continue;

                int slot = guiFile.getInt(path + "slot", 0);
                if (slot < 0 || slot >= size) continue;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(guiFile.getString(path + "display-name", " "));
                    List<String> rawLore = guiFile.getStringList(path + "lore");
                    if (rawLore != null && !rawLore.isEmpty()) {
                        meta.setLore(rawLore);
                    }
                    item.setItemMeta(meta);
                }
                gui.setItem(slot, item);
            }
        }

        FileConfiguration config = plugin.getConfig();
        if (config.getConfigurationSection("arenas") != null) {
            String fallbackMatName = guiFile.getString("arenas-menu.item.default-fallback.material", "IRON_SWORD");
            int autoSlot = guiFile.getInt("arenas-menu.item.default-fallback.start-slot", 10);

            for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
                if (!config.getBoolean("arenas." + key + ".active", false)) continue;

                String matName = config.getString("arenas." + key + ".icon", fallbackMatName);
                Material mat = Material.matchMaterial(matName);
                if (mat == null) mat = Material.matchMaterial(fallbackMatName);
                if (mat == null) mat = Material.IRON_SWORD;

                int targetSlot = config.getInt("arenas." + key + ".slot", -1);
                if (targetSlot == -1) {
                    targetSlot = autoSlot;
                    autoSlot++;
                }

                if (targetSlot >= size || targetSlot < 0) continue;

                ItemStack icon = new ItemStack(mat);
                ItemMeta meta = icon.getItemMeta();
                if (meta != null) {
                    String rawName = guiFile.getString("arenas-menu.item.display-name", "§e§l%arena_display% Map");
                    String cleanName = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
                    meta.setDisplayName(rawName.replace("%arena_display%", cleanName));
                    
                    List<String> rawLore = guiFile.getStringList("arenas-menu.item.lore");
                    List<String> finishedLore = new ArrayList<>();
                    if (rawLore != null) {
                        for (String line : rawLore) {
                            finishedLore.add(line
                                .replace("%queue%", String.valueOf(getQueueCount(key)))
                                .replace("%in_match%", String.valueOf(getInMatchCount(key)))
                            );
                        }
                    }
                    meta.setLore(finishedLore);
                    icon.setItemMeta(meta);
                }
                gui.setItem(targetSlot, icon);
            }
        }
        player.openInventory(gui);
    }

    public void openFFAArenaGUI(Player player) {
        FileConfiguration guiFile = plugin.getGuisConfig();
        
        String title = guiFile.getString("ffa-menu.title", "§0Unranked FFA Arenas");
        int size = guiFile.getInt("ffa-menu.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, title);

        if (guiFile.getConfigurationSection("ffa-menu.extra-items") != null) {
            for (String extraKey : guiFile.getConfigurationSection("ffa-menu.extra-items").getKeys(false)) {
                String path = "ffa-menu.extra-items." + extraKey + ".";
                Material mat = Material.matchMaterial(guiFile.getString(path + "material", "AIR"));
                if (mat == null || mat == Material.AIR) continue;

                int slot = guiFile.getInt(path + "slot", 0);
                if (slot < 0 || slot >= size) continue;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(guiFile.getString(path + "display-name", " "));
                    gui.setItem(slot, item);
                }
            }
        }

        FileConfiguration config = plugin.getConfig();
        if (config.getConfigurationSection("ffa-arenas") != null) {
            int slotTracker = 11;
            NamespacedKey arenaKey = new NamespacedKey(plugin, "ffa_arena_id");
            
            for (String key : config.getConfigurationSection("ffa-arenas").getKeys(false)) {
                if (!config.getBoolean("ffa-arenas." + key + ".active", false)) continue;

                Material mat = Material.matchMaterial(config.getString("ffa-arenas." + key + ".icon", "DIAMOND_SWORD"));
                if (mat == null) mat = Material.DIAMOND_SWORD;

                ItemStack icon = new ItemStack(mat);
                ItemMeta meta = icon.getItemMeta();
                if (meta != null) {
                    // Inject raw configuration mapping identification directly into hidden metadata
                    meta.getPersistentDataContainer().set(arenaKey, PersistentDataType.STRING, key);

                    String customNamePath = "ffa-arenas." + key + ".display-name";
                    if (config.contains(customNamePath)) {
                        meta.setDisplayName(config.getString(customNamePath));
                    } else {
                        String cleanName = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
                        meta.setDisplayName("§6§lFFA: §e" + cleanName);
                    }
                    
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
                    
                    List<String> finishedLore = new ArrayList<>();
                    String arenaLorePath = "ffa-arenas." + key + ".lore";
                    
                    if (config.contains(arenaLorePath)) {
                        List<String> customLore = config.getStringList(arenaLorePath);
                        for (String line : customLore) {
                            finishedLore.add(line.replace("%ffa_players%", String.valueOf(getFFAArenaPlayerCount(key))));
                        }
                    } else {
                        finishedLore.add("§7Drop into instant open chaos fight.");
                        finishedLore.add("");
                        finishedLore.add("§7Active Combatants: §a" + getFFAArenaPlayerCount(key));
                        finishedLore.add("§eClick to deploy and engage!");
                    }
                    
                    meta.setLore(finishedLore);
                    icon.setItemMeta(meta);
                }
                gui.setItem(slotTracker++, icon);
            }
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration guiFile = plugin.getGuisConfig();
        String duelTitle = guiFile.getString("arenas-menu.title", "Arenas Matchmaking");
        String ffaTitle = guiFile.getString("ffa-menu.title", "§0Unranked FFA Arenas");
        String currentTitle = event.getView().getTitle();

        boolean isDuel = currentTitle.equals(duelTitle);
        boolean isFfa = currentTitle.equals(ffaTitle);

        if (!isDuel && !isFfa) return;
        
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        String name = meta.getDisplayName();
        if (name.trim().isEmpty() || name.contains("Close Menu")) {
            player.closeInventory();
            return;
        }

        player.closeInventory();

        if (isDuel) {
            // UNTOUCHED ORIGINAL DUELS PROCESSING ENGINE FLOW
            String rawNameTemplate = guiFile.getString("arenas-menu.item.display-name", "§e§l%arena_display% Map")
                    .replace("%arena_display%", "");
            String strippedTemplate = org.bukkit.ChatColor.stripColor(rawNameTemplate).trim();
            String strippedName = org.bukkit.ChatColor.stripColor(name).trim();
            String arena = strippedName.replace(strippedTemplate, "").toLowerCase().trim();
            toggleQueue(player, arena);
        } else {
            NamespacedKey arenaKey = new NamespacedKey(plugin, "ffa_arena_id");
            String arenaTarget;
            
            // Query persistent target tags first to support complete customization configurations safely
            if (meta.getPersistentDataContainer().has(arenaKey, PersistentDataType.STRING)) {
                arenaTarget = meta.getPersistentDataContainer().get(arenaKey, PersistentDataType.STRING);
            } else {
                // Safe legacy layout backup processing
                arenaTarget = org.bukkit.ChatColor.stripColor(name).replace("FFA:", "").trim().toLowerCase();
            }
            
            if (arenaTarget != null && !arenaTarget.isEmpty()) {
                joinFFAArena(player, arenaTarget);
            }
        }
    }

    public void joinFFAArena(Player player, String arena) {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("ffa-arenas." + arena) || !config.getBoolean("ffa-arenas." + arena + ".active", false)) {
            player.sendMessage("§cArena workspace profile target unverified.");
            return;
        }

        if (plugin.getPartyManager().isInParty(player)) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party != null) {
                if (!party.getLeader().equals(player.getUniqueId())) {
                    player.sendMessage("§cOnly the party leader can deploy the group into FFA!");
                    return;
                }

                List<UUID> allPartyUUIDs = new ArrayList<>();
                allPartyUUIDs.add(party.getLeader());
                allPartyUUIDs.addAll(party.getMembers());

                for (UUID id : allPartyUUIDs) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null && p.isOnline()) {
                        activeFFAPlayers.put(p.getUniqueId(), arena);
                        p.getInventory().clear();
                        p.getInventory().setArmorContents(null);
                        teleportToFFASpawn(p, arena);
                        loadFFAArenaKit(p, arena);
                        p.sendMessage("§aYour party dropped into FFA Arena: §e" + arena);
                        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
                    }
                }
                return;
            }
        }

        activeFFAPlayers.put(player.getUniqueId(), arena);
        
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        teleportToFFASpawn(player, arena);
        loadFFAArenaKit(player, arena);

        player.sendMessage("§aDropped into FFA Arena: §e" + arena + "§a! Instant combat active.");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
    }

    public void toggleQueue(Player player, String arena) {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("arenas." + arena)) return;
        if (!config.getBoolean("arenas." + arena + ".active", false)) {
            player.sendMessage("§cThis arena configuration setup is not verified or marked active.");
            return;
        }

        if (plugin.getPartyManager().isInParty(player)) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party != null) {
                if (!party.getLeader().equals(player.getUniqueId())) {
                    player.sendMessage("§cOnly the party leader can join duels for the party!");
                    return;
                }

                List<Player> partyMembers = new ArrayList<>();
                
                Player leaderPlayer = Bukkit.getPlayer(party.getLeader());
                if (leaderPlayer != null && leaderPlayer.isOnline()) {
                    partyMembers.add(leaderPlayer);
                }
                
                for (UUID id : party.getMembers()) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null && p.isOnline()) {
                        partyMembers.add(p);
                    }
                }

                if (partyMembers.size() < 2) {
                    player.sendMessage("§cYou need at least 2 online party members to duel each other.");
                    return;
                }

                for (Player p : partyMembers) {
                    removeFromAllQueues(p.getUniqueId());
                }

                player.sendMessage("§aDeploying your full party into arena match context: §e" + arena);
                for (int i = 0; i < partyMembers.size() - 1; i += 2) {
                    startMatch(partyMembers.get(i), partyMembers.get(i + 1), arena);
                }
                
                if (partyMembers.size() % 2 != 0 && partyMembers.size() > 2) {
                    startMatch(partyMembers.get(partyMembers.size() - 1), partyMembers.get(0), arena);
                }
                return;
            }
        }

        List<UUID> queue = soloQueues.computeIfAbsent(arena, k -> new ArrayList<>());
        UUID uuid = player.getUniqueId();

        if (queue.contains(uuid)) {
            queue.remove(uuid);
            player.sendMessage("§cLeft queue for: §e" + arena);
            return;
        }

        queue.add(uuid);
        player.sendMessage("§aQueued for: §e" + arena + " §7(" + queue.size() + "/2)");

        if (queue.size() >= 2) {
            Player p1 = Bukkit.getPlayer(queue.remove(0));
            Player p2 = Bukkit.getPlayer(queue.remove(0));
            if (p1 != null && p2 != null) {
                startMatch(p1, p2, arena);
            }
        }
    }

    public void removeFromAllQueues(UUID uuid) {
        for (List<UUID> queue : soloQueues.values()) {
            queue.remove(uuid);
        }
    }

    public void startMatch(Player p1, Player p2, String arena) {
        activeMatches.put(p1.getUniqueId(), p2.getUniqueId());
        activeMatches.put(p2.getUniqueId(), p1.getUniqueId());
        playerArenaMap.put(p1.getUniqueId(), arena);
        playerArenaMap.put(p2.getUniqueId(), arena);

        arenaBlockRollbacks.put(arena, new HashMap<>());

        Bukkit.getScheduler().runTask(plugin, () -> {
            p1.setGameMode(GameMode.SURVIVAL);
            p2.setGameMode(GameMode.SURVIVAL);
            
            teleportToSpawn(p1, arena, "spawn1");
            teleportToSpawn(p2, arena, "spawn2");

            p1.getInventory().clear();
            p1.getInventory().setArmorContents(null);
            p2.getInventory().clear();
            p2.getInventory().setArmorContents(null);

            loadArenaKit(p1, arena);
            loadArenaKit(p2, arena);
        });

        new BukkitRunnable() {
            int countdown = 3;
            @Override
            public void run() {
                if (!p1.isOnline() || !p2.isOnline()) {
                    cancel();
                    return;
                }
                if (countdown > 0) {
                    p1.sendTitle("§e§l" + countdown, "§7Get Ready...", 0, 22, 0);
                    p2.sendTitle("§e§l" + countdown, "§7Get Ready...", 0, 22, 0);
                    p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    countdown--;
                } else {
                    p1.sendTitle("§a§lGO!", "§7The fight has begun!", 5, 20, 5);
                    p2.sendTitle("§a§lGO!", "§7The fight has begun!", 5, 20, 5);
                    p1.playSound(p1.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 1f);
                    p2.playSound(p2.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 1f);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!isCurrentlyFighting(p) || isInFFA(p)) return;
        
        String arena = playerArenaMap.get(p.getUniqueId());
        if (arena != null) {
            Map<Location, BlockState> history = arenaBlockRollbacks.get(arena);
            if (history != null) {
                history.putIfAbsent(event.getBlock().getLocation(), event.getBlock().getState());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (!isCurrentlyFighting(p) || isInFFA(p)) return;
        
        String arena = playerArenaMap.get(p.getUniqueId());
        if (arena != null) {
            Map<Location, BlockState> history = arenaBlockRollbacks.get(arena);
            if (history != null) {
                history.putIfAbsent(event.getBlockReplacedState().getLocation(), event.getBlockReplacedState());
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        boolean matchExplosion = false;
        String detectedArena = null;
        
        for (Block block : event.blockList()) {
            for (UUID uuid : activeMatches.keySet()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    detectedArena = playerArenaMap.get(uuid);
                    if (detectedArena != null) {
                        matchExplosion = true;
                        break;
                    }
                }
            }
            if (matchExplosion) break;
        }

        if (matchExplosion && detectedArena != null) {
            Map<Location, BlockState> history = arenaBlockRollbacks.get(detectedArena);
            if (history != null) {
                for (Block b : event.blockList()) {
                    history.putIfAbsent(b.getLocation(), b.getState());
                }
            }
        }
    }

    @EventHandler
    public void onVoidOrLobbyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        
        if (!isCurrentlyFighting(p) || p.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        
        // --- FFA DEATH HANDLING ---
        if (isInFFA(loser)) {
            event.getDrops().clear();
            activeFFAPlayers.remove(loser.getUniqueId());

            Player killer = loser.getKiller();
            if (killer != null && killer.isOnline() && isInFFA(killer)) {
                String killerArena = activeFFAPlayers.get(killer.getUniqueId());
                
                killer.setHealth(20.0);
                killer.setFoodLevel(20);
                killer.setFireTicks(0);
                
                killer.getInventory().clear();
                loadFFAArenaKit(killer, killerArena);
                
                killer.sendMessage("§a§lKILL! §7Your health, hunger, and kit have been refreshed.");
                killer.playSound(killer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
            }

            loser.sendTitle("§c§lELIMINATED!", "§7Returning to lobby...", 10, 20, 10);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        loser.spigot().respawn();
                    } catch (Exception | NoSuchMethodError e) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "respawn " + loser.getName());
                    }
                    
                    loser.setGameMode(GameMode.ADVENTURE);
                    loser.setHealth(20.0);
                    loser.setFoodLevel(20);
                    loser.setFireTicks(0);
                    loser.getInventory().clear();
                    loser.getInventory().setArmorContents(null);
                    
                    Location lobby = plugin.getConfigManager().getLobbyLocation();
                    if (lobby != null && lobby.getWorld() != null) {
                        loser.teleport(lobby);
                    } else {
                        World primaryWorld = Bukkit.getWorld("world");
                        if (primaryWorld != null) {
                            loser.teleport(primaryWorld.getSpawnLocation());
                        } else if (!Bukkit.getWorlds().isEmpty()) {
                            loser.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                        }
                    }
                }
            }.runTaskLater(plugin, 1L); 
            return; 
        }

        // --- 1V1 DUELS MATCHMAKING HANDLING (UNTOUCHED) ---
        UUID winnerUUID = activeMatches.remove(loser.getUniqueId());
        if (winnerUUID == null) return;

        activeMatches.remove(winnerUUID);
        Player winner = Bukkit.getPlayer(winnerUUID);
        
        String arena = playerArenaMap.remove(loser.getUniqueId());
        if (winner != null) playerArenaMap.remove(winner.getUniqueId());

        event.getDrops().clear();
        loser.setGameMode(GameMode.SPECTATOR);

        loser.sendTitle("§c§lDEFEAT", "§7Spectating... Teleporting in 5s", 10, 40, 10);
        loser.playSound(loser.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);

        if (winner != null) {
            winner.sendTitle("§a§lVICTORY!", "§7You won! Teleporting in 5s", 10, 40, 10);
            winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
            plugin.getEconomyRewardManager().rewardWinner(winner, "solo");
        }

        if (arena != null) {
            Map<Location, BlockState> history = arenaBlockRollbacks.remove(arena);
            if (history != null) {
                for (Map.Entry<Location, BlockState> entry : history.entrySet()) {
                    entry.getValue().update(true, false);
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                loser.getInventory().clear();
                loser.getInventory().setArmorContents(null);
                if (winner != null && winner.isOnline()) {
                    winner.getInventory().clear();
                    winner.getInventory().setArmorContents(null);
                }

                Location lobby = plugin.getConfigManager().getLobbyLocation();
                if (lobby != null && lobby.getWorld() != null) {
                    loser.teleport(lobby);
                    if (winner != null && winner.isOnline()) winner.teleport(lobby);
                } else {
                    World primaryWorld = Bukkit.getWorlds().get(0);
                    if (primaryWorld != null) {
                        loser.teleport(primaryWorld.getSpawnLocation());
                        if (winner != null && winner.isOnline()) winner.teleport(primaryWorld.getSpawnLocation());
                    }
                }

                loser.setGameMode(GameMode.ADVENTURE);
                loser.setHealth(20.0);
                loser.setFoodLevel(20);
                loser.setFireTicks(0);

                if (winner != null && winner.isOnline()) {
                    winner.setGameMode(GameMode.ADVENTURE);
                    winner.setHealth(20.0);
                    winner.setFoodLevel(20);
                    winner.setFireTicks(0);
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    @SuppressWarnings("unchecked")
    private void loadArenaKit(Player player, String arena) {
        FileConfiguration config = plugin.getConfig();
        String path = "arenas." + arena + ".kit";
        if (config.contains(path)) {
            try {
                List<ItemStack> items = (List<ItemStack>) config.getList(path);
                if (items != null) {
                    player.getInventory().setContents(items.toArray(new ItemStack[0]));
                    player.updateInventory();
                }
            } catch (Exception e) {
                Object obj = config.get(path);
                if (obj instanceof ItemStack[] stock) {
                    player.getInventory().setContents(stock);
                    player.updateInventory();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFFAArenaKit(Player player, String arena) {
        FileConfiguration config = plugin.getConfig();
        String path = "ffa-arenas." + arena + ".kit";
        if (config.contains(path)) {
            try {
                List<ItemStack> items = (List<ItemStack>) config.getList(path);
                if (items != null) {
                    player.getInventory().setContents(items.toArray(new ItemStack[0]));
                    player.updateInventory();
                }
            } catch (Exception e) {
                Object obj = config.get(path);
                if (obj instanceof ItemStack[] stock) {
                    player.getInventory().setContents(stock);
                    player.updateInventory();
                }
            }
        }
    }

    private void teleportToSpawn(Player player, String arena, String spawnKey) {
        FileConfiguration config = plugin.getConfig();
        String path = "arenas." + arena + "." + spawnKey + ".";
        String worldName = config.getString(path + "world");
        if (worldName == null) return;
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        double x = config.getDouble(path + "x");
        double y = config.getDouble(path + "y");
        double z = config.getDouble(path + "z");
        float yaw = (float) config.getDouble(path + "yaw");
        float pitch = (float) config.getDouble(path + "pitch");
        player.teleport(new Location(world, x, y, z, yaw, pitch));
    }

    private void teleportToFFASpawn(Player player, String arena) {
        FileConfiguration config = plugin.getConfig();
        String path = "ffa-arenas." + arena + ".spawn.";
        String worldName = config.getString(path + "world");
        if (worldName == null) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        double x = config.getDouble(path + "x");
        double y = config.getDouble(path + "y");
        double z = config.getDouble(path + "z");
        float yaw = (float) config.getDouble(path + "yaw");
        float pitch = (float) config.getDouble(path + "pitch");
        player.teleport(new Location(world, x, y, z, yaw, pitch));
    }
}