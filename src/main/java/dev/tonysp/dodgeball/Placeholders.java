package dev.tonysp.dodgeball;

import dev.tonysp.dodgeball.game.GameState;
import dev.tonysp.dodgeball.game.Team;
import dev.tonysp.dodgeball.game.TwoTeamGame;
import dev.tonysp.dodgeball.game.arena.Arena;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Placeholders extends PlaceholderExpansion {

    private final Dodgeball plugin;

    public Placeholders (Dodgeball plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dodgeball";
    }

    @Override
    public @NotNull String getAuthor () {
        return plugin.getPluginMeta().getAuthors().toString();
    }


    @Override
    public @NotNull String getVersion(){
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String getRequiredPlugin() {
        return "Dodgeball";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest (OfflinePlayer offlinePlayer, String param) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) { return ""; }

        param = param.toLowerCase();

        if (param.contains("alive")) {
            Team team = Team.valueOf(param.split("alive")[1].split("-")[1].toUpperCase());
            String arenaName = param.split("alive")[1].split("-")[2];
            Optional<Arena> arena = plugin.arenas().getArenaByName(arenaName);
            if (arena.isEmpty()
                    || arena.get().getActiveGame() == null
                    || arena.get().getActiveGame().getGameState() != GameState.IN_PROGRESS) {
                return "";
            }
            TwoTeamGame twoTeamGame = (TwoTeamGame) arena.get().getActiveGame();
            return String.valueOf(twoTeamGame.countPlayersInGame(team));
        }

        return null;
    }
}