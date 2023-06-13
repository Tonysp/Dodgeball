package dev.tonysp.dodgeball;

import dev.tonysp.dodgeball.commands.DodgeballCommand;
import dev.tonysp.dodgeball.game.GameManager;
import dev.tonysp.dodgeball.game.arena.ArenaManager;
import dev.tonysp.dodgeball.game.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public class Dodgeball extends JavaPlugin {

    public static Dodgeball getInstance () {
        return getPlugin(Dodgeball.class);
    }

    private final PlayerManager playerManager = new PlayerManager(this);
    private final GameManager gameManager = new GameManager(this);
    private final ArenaManager arenaManager = new ArenaManager(this);

    private Placeholders placeholders;

    private File configFile;

    private boolean loaded = false;

    @Override
    public void onEnable () {
        log(enable());
    }

    @Override
    public void onDisable () {
        log(disable());
    }

    public String enable () {
        loadConfig();
        String failed = ChatColor.RED + "Plugin failed to enable, check console!";

        Message.loadFromConfig(getConfig());

        DodgeballCommand dodgeballCommand = new DodgeballCommand(this);
        Objects.requireNonNull(getCommand("dodgeball")).setExecutor(dodgeballCommand);
        Objects.requireNonNull(getCommand("db")).setExecutor(dodgeballCommand);

        if (!arenas().load()) {
            return failed + " (arena module error)";
        }
        if (!players().load()) {
            return failed + " (player module error)";
        }
        if (!games().load()) {
            return failed + " (game module error)";
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders = new Placeholders(this);
            placeholders.register();
            log("PlaceholderAPI integration enabled.");
        } else {
            log("PlaceholderAPI integration disabled.");
        }

        loaded = true;
        return ChatColor.GREEN + "Plugin enabled!";
    }

    public String disable () {
        players().unload();
        games().unload();
        arenas().unload();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && placeholders != null) {
            placeholders.unregister();
        }

        loaded = false;
        return ChatColor.GREEN + "Plugin disabled!";
    }

    private void loadConfig () {
        configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        try {
            YamlConfiguration yamlConfig = new YamlConfiguration();
            yamlConfig.load(new File(getDataFolder() + File.separator + "config.yml"));
        } catch (Exception exception) {
            log("There was a problem loading the config. More details bellow.");
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();
    }

    public GameManager games () {
        return gameManager;
    }

    public PlayerManager players () {
        return playerManager;
    }

    public ArenaManager arenas () {
        return arenaManager;
    }

    public static void logWarning (String text) {
        log(Level.INFO, "! ! ! " + text + " ! ! !");
    }

    public static void log (Level level, String text) {
        Bukkit.getLogger().log(level, "[Dodgeball] " + text);
    }

    public static void log (String text) {
        log(Level.INFO, text);
    }

    public File getConfigFile () {
        return configFile;
    }

    public boolean isLoaded () {
        return loaded;
    }
}
