package Itzjatinog.ogduels.placeholders;

import Itzjatinog.ogduels.OGDuels;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class DuelsExpansion extends PlaceholderExpansion {
    private final OGDuels plugin;

    public DuelsExpansion(OGDuels plugin) { this.plugin = plugin; }

    @Override public @NotNull String getAuthor() { return "ItzJatinOG"; }
    @Override public @NotNull String getIdentifier() { return "ogduels"; }
    @Override public @NotNull String getVersion() { return "1.0.0"; }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) return "0";
        if (identifier.equalsIgnoreCase("kills")) return "0";
        return null;
    }
}
