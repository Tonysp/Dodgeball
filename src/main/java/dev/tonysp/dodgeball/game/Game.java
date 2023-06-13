package dev.tonysp.dodgeball.game;

import dev.tonysp.dodgeball.Message;
import dev.tonysp.dodgeball.game.arena.Arena;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Game {

    private final UUID uuid = UUID.randomUUID();
    private GameState gameState = GameState.IN_LOBBY;
    protected int remainingTicks, maxPlayers = 20;
    private Set<Player> players = new HashSet<>();
    protected Arena arena;

    public abstract void tick ();

    public abstract void finishGame ();

    public abstract void cancelGame ();

    public abstract void processKill (Player killer, Player victim);

    public UUID getUuid () {
        return uuid;
    }

    public void setGameState (GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState () {
        return gameState;
    }

    public Set<Player> getPlayers () {
        return players;
    }

    public void broadcastMessage (Message message, TextReplacementConfig... replacements) {
        getPlayers().forEach(player -> message.sendTo(player, replacements));
    }

    public abstract void startGame ();

    public boolean join (Player player) {
        if (isFull()) {
            return false;
        }

        players.add(player);
        Message.YOU_JOINED.sendTo(player);
        return true;
    }

    public boolean isFull () {
        return players.size() >= maxPlayers;
    }

    public Arena getArena () {
        return arena;
    }
}

