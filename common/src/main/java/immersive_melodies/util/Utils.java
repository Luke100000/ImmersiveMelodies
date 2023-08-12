package immersive_melodies.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class Utils {
    public static String escapeString(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d_.-]", "");
    }
    public static String toTitle(String string) {
        return StringUtils.capitalize(string.replace("_", " "));
    }
}
