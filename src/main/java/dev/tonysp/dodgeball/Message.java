package dev.tonysp.dodgeball;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public enum Message {
    PREFIX,
    ALREADY_IN_GAME,
    YOU_JOINED,
    ALL_ARENAS_FULL,
    TEAM_WIN_ANNOUNCE,
    TIE_ANNOUNCE,
    SCOREBOARD_TITLE,
    SCOREBOARD_ENTRY,
    ;

    private TextComponent message;
    private boolean isMessageSet = false;

    public static final String MESSAGES_SECTION = "messages";
    private static final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public void setMessage (TextComponent message) {
        this.message = message;
        this.isMessageSet = true;
    }

    public String getMessageContent () {
        return this.message.content();
    }

    public TextComponent getMessage () {
        return message;
    }

    public static LegacyComponentSerializer getSerializer () {
        return serializer;
    }

    public static void loadFromConfig (FileConfiguration config) {
        for (Message message : values()) {
            String key = MESSAGES_SECTION + "." + message.toString().toLowerCase().replaceAll("_", "-");
            String stringMessage = config.getString(key, "");
            if (stringMessage.isEmpty())
                continue;

            message.setMessage(getSerializer().deserialize(stringMessage));
        }
    }

    public void sendTo (Player player, Iterable<? extends TextReplacementConfig> replacements) {
        if (!isMessageSet) {
            return;
        }

        TextComponent finalMessage = PREFIX.getMessage().append(getMessage());
        for (TextReplacementConfig variable : replacements) {
            finalMessage = (TextComponent) finalMessage.replaceText(variable);
        }
        player.sendMessage(finalMessage);
    }

    public void sendTo (Player player, TextReplacementConfig... replacements) {
        sendTo(player, Arrays.asList(replacements));
    }
}