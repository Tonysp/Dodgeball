package dev.tonysp.dodgeball.commands;

import dev.tonysp.dodgeball.Dodgeball;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DodgeballCommand implements CommandExecutor {

    private final Dodgeball plugin;

    public DodgeballCommand (Dodgeball plugin) {
        this.plugin = plugin;
    }

    public static final String TITLE = ChatColor.GOLD + "--// " + ChatColor.GRAY + "Dodgeball" + ChatColor.GOLD + " §l//--";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        String usedCommand = label.toLowerCase();

        if (args.length == 0) {
            sender.sendMessage(TITLE);
            sender.sendMessage(ChatColor.GOLD + "/" + usedCommand + " " + ChatColor.YELLOW + "join" + ChatColor.GRAY + " - Join lobby");
            sender.sendMessage(ChatColor.GOLD + "/" + usedCommand + " " + ChatColor.YELLOW + "player [name]" + ChatColor.GRAY + " - Shows player's stats");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("dodgeball.reload")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }

            sender.sendMessage(plugin.disable());
            sender.sendMessage(plugin.enable());
            return true;
        }

        if (!Dodgeball.getInstance().isLoaded()) {
            if (sender.hasPermission("dodgeball.reload")) {
                sender.sendMessage(ChatColor.RED + "The plugin failed to enable properly. Please check the console!");
                sender.sendMessage(ChatColor.RED + "You can reload the plugin with /" + usedCommand + " reload");
            } else {
                sender.sendMessage(ChatColor.RED + "Oops! There was an error. Please contact the administrator.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("join")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + "This command can be only used in game.");
                return true;
            }

            Player player = (Player) sender;
            plugin.games().joinGame(player);
        }
        return true;
    }
}