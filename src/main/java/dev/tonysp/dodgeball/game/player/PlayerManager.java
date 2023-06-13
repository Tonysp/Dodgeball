package dev.tonysp.dodgeball.game.player;

import dev.tonysp.dodgeball.Dodgeball;
import dev.tonysp.dodgeball.Manager;

public class PlayerManager extends Manager {
    public PlayerManager (Dodgeball plugin) {
        super(plugin);
    }

    @Override
    public boolean load () {
        return true;
    }

    @Override
    public boolean unload () {
        return true;
    }
}
