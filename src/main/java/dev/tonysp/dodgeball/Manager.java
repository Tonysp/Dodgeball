package dev.tonysp.dodgeball;

public abstract class Manager {

    protected final Dodgeball plugin;

    public Manager (Dodgeball plugin) {
        this.plugin = plugin;
    }

    public abstract boolean load ();

    public abstract boolean unload ();

}