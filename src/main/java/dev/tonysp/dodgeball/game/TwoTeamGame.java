package dev.tonysp.dodgeball.game;

import dev.tonysp.dodgeball.Message;
import dev.tonysp.dodgeball.game.arena.Arena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TwoTeamGame extends Game {

    private Map<Team, Set<Player>> teamPlayers = new HashMap<>();
    private Map<Player, Integer> score = new HashMap<>();

    private static final int maxGameDuration = 120;
    private static final int lobbyMaxWaitTime = 30;

    private int remainingLobbyTicks;

    public TwoTeamGame (Arena arena) {
        this.arena = arena;
        remainingLobbyTicks = lobbyMaxWaitTime * GameManager.TICKRATE;
        remainingTicks = remainingLobbyTicks + (maxGameDuration * GameManager.TICKRATE);

        teamPlayers.put(Team.RED, new HashSet<>());
        teamPlayers.put(Team.BLUE, new HashSet<>());
    }

    @Override
    public void tick () {
        remainingTicks --;

        if (remainingTicks <= 0) {
            finishGame();
            return;
        }

        if (getGameState() == GameState.IN_LOBBY) {
            remainingLobbyTicks--;

            if (remainingLobbyTicks <= 0) {
                startGame();
            }
        } else if (getGameState() == GameState.IN_PROGRESS) {
            if (countPlayersInGame(Team.RED) == 0 ||  countPlayersInGame(Team.BLUE) == 0) {
                finishGame();
            }
        }
    }

    public long countPlayersInGame (Team team) {
        return teamPlayers.get(team).stream()
                .filter(OfflinePlayer::isOnline)
                .filter(player -> arena.isLocationInArena(player.getLocation()))
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .count();
    }

    public void teleportAllPlayersBack () {
        getPlayers().forEach(player -> player.getInventory().clear());
        getPlayers().forEach(player -> player.teleport(arena.getLobbySpawn().getLocation().getWorld().getSpawnLocation()));
    }

    @Override
    public void finishGame () {
        setGameState(GameState.FINISHED);
        teleportAllPlayersBack();

        announceScore();

        String winningTeamName;
        long redTeamPlayerCount = countPlayersInGame(Team.RED);
        long blueTeamPlayerCount = countPlayersInGame(Team.BLUE);
        if (redTeamPlayerCount > 0) {
            winningTeamName = "RED";
        } else if (blueTeamPlayerCount > 0) {
            winningTeamName = "BLUE";
        } else {
            getPlayers().forEach(Message.TIE_ANNOUNCE::sendTo);
            return;
        }

        TextReplacementConfig replacement = TextReplacementConfig.builder().match("%TEAM%")
                .replacement(Component.text(winningTeamName))
                .build();
        getPlayers().forEach(player -> Message.TEAM_WIN_ANNOUNCE.sendTo(player, replacement));
    }

    private void announceScore () {
        getPlayers().forEach(Message.SCOREBOARD_TITLE::sendTo);
        TextReplacementConfig.Builder rankReplacement = TextReplacementConfig.builder().match("%RANK%");
        TextReplacementConfig.Builder nameReplacement = TextReplacementConfig.builder().match("%NAME%");
        TextReplacementConfig.Builder hitsReplacement = TextReplacementConfig.builder().match("%HITS%");

        AtomicInteger rank = new AtomicInteger(1);
        score.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(5)
                .forEach(entry -> getPlayers().forEach(player -> Message.TEAM_WIN_ANNOUNCE.sendTo(player,
                        rankReplacement.replacement(String.valueOf(rank.getAndIncrement())).build(),
                        nameReplacement.replacement(entry.getKey().getName()).build(),
                        hitsReplacement.replacement(String.valueOf(entry.getValue())).build()
                )));
    }

    @Override
    public void cancelGame () {
        setGameState(GameState.FINISHED);
        teleportAllPlayersBack();
    }

    @Override
    public void processKill (Player killer, Player victim) {
        score.put(killer, score.getOrDefault(killer, 0) + 1);
        victim.setGameMode(GameMode.SPECTATOR);
    }

    public void teleportAllPlayersToArena () {
        teamPlayers.get(Team.RED).forEach(player -> arena.pickNextSpawnPoint(Team.RED).teleport(player));
        teamPlayers.get(Team.BLUE).forEach(player -> arena.pickNextSpawnPoint(Team.BLUE).teleport(player));

        getPlayers().forEach(player -> player.getInventory().clear());
        getPlayers().forEach(player -> player.getInventory().addItem(new ItemStack(Material.SNOWBALL)));
    }

    @Override
    public void startGame () {
        setGameState(GameState.IN_PROGRESS);
        remainingTicks -= remainingLobbyTicks;

        teleportAllPlayersToArena();
    }

    @Override
    public boolean join (Player player) {
        if (super.join(player)) {
            if (teamPlayers.get(Team.RED).size() > teamPlayers.get(Team.BLUE).size()) {
                teamPlayers.get(Team.BLUE).add(player);
            } else {
                teamPlayers.get(Team.RED).add(player);
            }
            return true;
        } else {
            return false;
        }
    }
}