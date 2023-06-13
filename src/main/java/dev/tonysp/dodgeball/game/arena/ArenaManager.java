package dev.tonysp.dodgeball.game.arena;

import dev.tonysp.dodgeball.Dodgeball;
import dev.tonysp.dodgeball.Manager;
import dev.tonysp.dodgeball.game.GameState;
import dev.tonysp.dodgeball.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ArenaManager extends Manager {

    private final Map<String, Arena> arenas = new HashMap<>();
    private final HashSet<Arena> lockedArenas = new HashSet<>();

    public ArenaManager (Dodgeball plugin) {
        super(plugin);
    }

    @Override
    public boolean load () {
        arenas.clear();
        lockedArenas.clear();

        Dodgeball plugin = Dodgeball.getInstance();
        ConfigurationSection config = plugin.getConfig();

        ConfigurationSection arenasConfig = config.getConfigurationSection("arenas");
        if (arenasConfig == null) {
            return false;
        }
        for (String arenaName : arenasConfig.getKeys(false)) {
            Dodgeball.log("... loading arena " + arenaName);
            ConfigurationSection arenaConfig = config.getConfigurationSection("arenas." + arenaName);
            if (arenaConfig == null) {
                Dodgeball.logWarning("error while loading arena: invalid arena config");
                continue;
            }

            String name = arenaConfig.getString("name");
            String arenaRegionName = arenaConfig.getString("arena-region", "");

            if (arenaRegionName.isEmpty()) {
                Dodgeball.logWarning("error while loading arena: WorldGuard arena region name must be specified");
                return false;
            }

            String lineRegionName = arenaConfig.getString("line-region", "");
            if (lineRegionName.isEmpty()) {
                Dodgeball.logWarning("error while loading arena: WorldGuard line region name must be specified");
                return false;
            }

            Optional<SpawnPoint> lobbySpawnPoint = parseSpawnPointString(arenaConfig.getString("lobby-spawn-point"));
            if (lobbySpawnPoint.isEmpty()) {
                Dodgeball.logWarning("error while loading arena: no lobby teleport specified");
                continue;
            }

            List<String> redTeamSpawnsStrings = arenaConfig.getStringList("red-team-spawns");
            List<SpawnPoint> redTeamSpawns = loadSpawns(redTeamSpawnsStrings);
            if (redTeamSpawns.isEmpty()) {
                Dodgeball.logWarning("error while loading arena: no red team spawns loaded");
                continue;
            }

            List<String> blueTeamSpawnsStrings = arenaConfig.getStringList("blue-team-spawns");
            List<SpawnPoint> blueTeamSpawns = loadSpawns(blueTeamSpawnsStrings);
            if (blueTeamSpawns.isEmpty()) {
                Dodgeball.logWarning("error while loading arena: no blue team spawns loaded");
                continue;
            }
            Map<Team, List<SpawnPoint>> teamSpawns = new HashMap<>();
            teamSpawns.put(Team.RED, redTeamSpawns);
            teamSpawns.put(Team.BLUE, blueTeamSpawns);

            Arena arena = new Arena(name, arenaRegionName, lineRegionName, lobbySpawnPoint.get(), teamSpawns);

            if (!arena.getPlayerSpawns().stream().allMatch(spawnPoint -> arena.isLocationInArena(spawnPoint.getLocation()))) {
                Dodgeball.logWarning("error while loading arena: spawn points must be inside the arena region");
                continue;
            }

            arenas.put(name, arena);
        }


        return true;
    }

    private void loadArena (String arenaName) {
        Dodgeball.log("... loading arena " + arenaName);
        ConfigurationSection arenaConfig = config.getConfigurationSection("arenas." + arenaName);
        if (arenaConfig == null) {
            Dodgeball.logWarning("error while loading arena: invalid arena config");
            return;
        }

        String name = arenaConfig.getString("name");
        String arenaRegionName = arenaConfig.getString("arena-region", "");

        if (arenaRegionName.isEmpty()) {
            Dodgeball.logWarning("error while loading arena: WorldGuard arena region name must be specified");
            return false;
        }

        String lineRegionName = arenaConfig.getString("line-region", "");
        if (lineRegionName.isEmpty()) {
            Dodgeball.logWarning("error while loading arena: WorldGuard line region name must be specified");
            return false;
        }

        Optional<SpawnPoint> lobbySpawnPoint = parseSpawnPointString(arenaConfig.getString("lobby-spawn-point"));
        if (lobbySpawnPoint.isEmpty()) {
            Dodgeball.logWarning("error while loading arena: no lobby teleport specified");
            continue;
        }

        List<String> redTeamSpawnsStrings = arenaConfig.getStringList("red-team-spawns");
        List<SpawnPoint> redTeamSpawns = loadSpawns(redTeamSpawnsStrings);
        if (redTeamSpawns.isEmpty()) {
            Dodgeball.logWarning("error while loading arena: no red team spawns loaded");
            continue;
        }

        List<String> blueTeamSpawnsStrings = arenaConfig.getStringList("blue-team-spawns");
        List<SpawnPoint> blueTeamSpawns = loadSpawns(blueTeamSpawnsStrings);
        if (blueTeamSpawns.isEmpty()) {
            Dodgeball.logWarning("error while loading arena: no blue team spawns loaded");
            continue;
        }
        Map<Team, List<SpawnPoint>> teamSpawns = new HashMap<>();
        teamSpawns.put(Team.RED, redTeamSpawns);
        teamSpawns.put(Team.BLUE, blueTeamSpawns);

        Arena arena = new Arena(name, arenaRegionName, lineRegionName, lobbySpawnPoint.get(), teamSpawns);

        if (!arena.getPlayerSpawns().stream().allMatch(spawnPoint -> arena.isLocationInArena(spawnPoint.getLocation()))) {
            Dodgeball.logWarning("error while loading arena: spawn points must be inside the arena region");
            continue;
        }
    }

    @Override
    public boolean unload () {
        return true;
    }

    public Optional<Arena> getAndLockFreeArena () {
        for (Arena arena : arenas.values()) {
            if (!lockedArenas.contains(arena)) {
                lockedArenas.add(arena);
                return Optional.of(arena);
            }
        }

        return Optional.empty();
    }

    public void unlockArena (Arena arena) {
        lockedArenas.remove(arena);
    }

    private List<SpawnPoint> loadSpawns (List<String> locationStrings) {
        List<SpawnPoint> spawns = new ArrayList<>();
        for (String locationString : locationStrings) {
            parseSpawnPointString(locationString).ifPresent(spawns::add);
        }
        return spawns;
    }

    public Optional<SpawnPoint> parseSpawnPointString (String locationString) {
        Location location;
        try {
            String[] parts = locationString.split(",");
            World world;

            float yaw = 0, pitch = 0;
            if (parts.length >= 6) {
                yaw = Float.parseFloat(parts[3]);
                pitch = Float.parseFloat(parts[4]);
                world = Bukkit.getWorld(parts[5]);
            } else {
                world = Bukkit.getWorld(parts[3]);
            }

            if (world == null) {
                return Optional.empty();
            }

            location = new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), yaw, pitch);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        return Optional.of(new SpawnPoint(location));
    }

    public Optional<Arena> getArenaByName (String name) {
        return Optional.ofNullable(arenas.get(name));
    }
}
