package immersive_melodies.util;

import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.List;

public class Utils {
    public static List<ItemStack> getHandItems(LivingEntity entity) {
        return List.of(entity.getMainHandItem(), entity.getOffhandItem());
    }

    public static boolean hasPermissions(Player player, int level) {
        return level <= 0 || player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }
    public static String toTitle(String string) {
        return StringUtils.capitalize(string.replace("_", " "));
    }

    public static String getPlayerName(Player player) {
        return player.getGameProfile().name().toLowerCase(Locale.ROOT);
    }

    public static boolean isPlayerMelody(Identifier identifier) {
        return identifier.getNamespace().equals("player");
    }

    public static boolean ownsMelody(Identifier identifier, Player player) {
        return isPlayerMelody(identifier) && identifier.getPath().startsWith(getPlayerName(player) + "/");
    }

    public static boolean canDelete(Identifier identifier, Player player) {
        return ownsMelody(identifier, player) || (isPlayerMelody(identifier) && hasPermissions(player, 2));
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
            return input.substring(lastDotIndex + delimiter.length());
        } else {
            return input;
        }
    }
}
