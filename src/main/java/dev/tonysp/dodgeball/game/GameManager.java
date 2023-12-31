package dev.tonysp.dodgeball.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import dev.tonysp.dodgeball.Dodgeball;
import dev.tonysp.dodgeball.Manager;
import dev.tonysp.dodgeball.Message;
import dev.tonysp.dodgeball.game.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GameManager extends Manager implements Listener {


    // How many times per second does the game tick. Cannot be greater than 20, or smaller than 1.
    public static int TICKRATE = 4;

    private int gameTickTaskId;

    private List<Game> games = new ArrayList<>();

    public GameManager (Dodgeball plugin) {
        super(plugin);
    }

    @Override
    public boolean load () {
        games.clear();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        if (TICKRATE > 20) {
            TICKRATE = 20;
        } else if (TICKRATE < 1) {
            TICKRATE = 1;
        }

        gameTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Game game : games) {
                game.tick();
                if (game.getGameState() == GameState.FINISHED) {
                    plugin.arenaManager().unlockArena(game.getArena());
                }
            }
            games.removeIf(game -> game.getGameState() == GameState.FINISHED);
        }, 0, 20 / TICKRATE);

        return true;
    }

    @Override
    public boolean unload () {
        ProjectileHitEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);

        Bukkit.getScheduler().cancelTask(gameTickTaskId);
        games.forEach(Game::cancelGame);

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileHitEvent (ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player shooter) {
            Location hitLocation;
            if (event.getHitEntity() != null) {
                hitLocation = event.getHitEntity().getLocation();
                games.stream()
                        .filter(game -> game.getGameState() == GameState.IN_PROGRESS)
                        .filter(game -> game.getPlayers().contains(event.getHitEntity()))
                        .findFirst().ifPresent(game -> game.processKill(shooter, (Player) event.getHitEntity()));
            } else if (event.getHitBlock() != null) {
                hitLocation = event.getHitBlock().getLocation().add(0, 1, 0);
            } else {
                return;
            }
            hitLocation.getWorld().dropItemNaturally(hitLocation, new ItemStack(Material.SNOWBALL));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent (PlayerQuitEvent event) {
        games.stream()
                .filter(game -> game.getPlayers().contains(event.getPlayer()))
                .forEach(game -> game.leave(event.getPlayer()));
    }

    public Optional<Game> getOrCreateJoinableGame () {
        Optional<Game> gameOptional = games.stream()
                .filter(game -> game.getGameState() == GameState.IN_LOBBY)
                .filter(game -> !game.isFull())
                .findFirst();
        if (gameOptional.isPresent()) {
            return gameOptional;
        } else {
            Optional<Arena> arena = plugin.arenaManager().getAndLockFreeArena();
            if (arena.isEmpty()) {
                return Optional.empty();
            }
            Game game = new TwoTeamGame(arena.get());
            arena.get().setActiveGame(game);
            games.add(game);
            return Optional.of(game);
        }
    }

    public void joinGame (Player player) {
        if (games.stream().anyMatch(game -> game.getPlayers().contains(player))) {
            Message.ALREADY_IN_GAME.sendTo(player);
            return;
        }

        getOrCreateJoinableGame().ifPresentOrElse(game -> game.join(player), () -> {
            Message.ALL_ARENAS_FULL.sendTo(player);
        });
    }
}
