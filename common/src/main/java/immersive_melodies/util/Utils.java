package immersive_melodies.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class Utils {
    public static String escapeString(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d_.-]", "");
    }

    public static String toTitle(String string) {
        return StringUtils.capitalize(string.replace("_", " "));
    }

    public static String getPlayerName(PlayerEntity player) {
        return escapeString(player.getGameProfile().getName());
    }

    public static boolean isPlayerMelody(Identifier identifier) {
        return identifier.getNamespace().equals("player");
    }

    public static boolean ownsMelody(Identifier identifier, PlayerEntity player) {
        return isPlayerMelody(identifier) && identifier.getPath().startsWith(getPlayerName(player) + "/");
    }

    public static boolean canDelete(Identifier identifier, PlayerEntity player) {
        return ownsMelody(identifier, player) || (isPlayerMelody(identifier) && player.hasPermissionLevel(2));
    }

    public static String removeLastPart(String input, String delimiter) {
        int lastDotIndex = input.lastIndexOf(delimiter);

        if (lastDotIndex != -1) {
            return input.substring(0, lastDotIndex);
        } else {
            return input;
        }
    }

    public static String getLastPart(String input, String delimiter) {
        int lastDotIndex = input.lastIndexOf(delimiter);

        if (lastDotIndex != -1) {
            return input.substring(lastDotIndex);
        } else {
            return input;
        }
    }
}
