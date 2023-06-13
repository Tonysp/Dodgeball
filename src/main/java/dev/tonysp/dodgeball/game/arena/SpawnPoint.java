package dev.tonysp.dodgeball.game.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpawnPoint {

    private Location location;

    public SpawnPoint (Location location) {
        this.location = location;
    }

    public void teleport (Player player) {
        player.teleport(location);
    }

    public Location getLocation () {
        return location;
    }
}
