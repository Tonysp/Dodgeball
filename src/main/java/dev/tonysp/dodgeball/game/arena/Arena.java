package dev.tonysp.dodgeball.game.arena;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.tonysp.dodgeball.game.Game;
import dev.tonysp.dodgeball.game.Team;
import org.bukkit.Location;

import java.util.*;

public class Arena {

    private String name;
    private String arenaRegion, lineRegion;
    private SpawnPoint lobbySpawn;
    private Game activeGame;
    private Map<Team, List<SpawnPoint>> teamSpawns;
    private Map<Team, Integer> currentSpawnIndexes = new HashMap<>();

    public Arena (String name, String arenaRegion, String lineRegion, SpawnPoint lobbySpawn, Map<Team, List<SpawnPoint>> teamSpawns) {
        this.name = name;
        this.arenaRegion = arenaRegion;
        this.lineRegion = lineRegion;
        this.lobbySpawn = lobbySpawn;
        this.teamSpawns = teamSpawns;
    }

    public SpawnPoint getLobbySpawn () {
        return lobbySpawn;
    }

    public boolean isLocationInArena (Location location) {
        return isLocationInRegion(location, arenaRegion);
    }

    public boolean isLocationInMiddleLine (Location location) {
        return isLocationInRegion(location, lineRegion);
    }

    private boolean isLocationInRegion (Location location, String region) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (manager == null) {
            return false;
        }
        ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        for (ProtectedRegion each : set) {
            if (each.getId().equalsIgnoreCase(region)) {
                return true;
            }
        }
        return false;
    }


    public Game getActiveGame () {
        return activeGame;
    }

    public void setActiveGame (Game activeGame) {
        this.activeGame = activeGame;
    }

    public SpawnPoint pickNextSpawnPoint (Team team) {
        int currentSpawnIndex = currentSpawnIndexes.getOrDefault(team, 0);
        if (++ currentSpawnIndex >= teamSpawns.get(team).size()) {
            currentSpawnIndex = 0;
        }
        SpawnPoint spawnPoint = teamSpawns.get(team).get(currentSpawnIndex);
        currentSpawnIndexes.put(team, currentSpawnIndex);
        return spawnPoint;
    }

    public Collection<SpawnPoint> getPlayerSpawns () {
        List<SpawnPoint> spawns = new ArrayList<>();
        spawns.addAll(teamSpawns.get(Team.RED));
        spawns.addAll(teamSpawns.get(Team.BLUE));
        return spawns;
    }
}
