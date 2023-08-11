package immersive_melodies.util;

import java.util.Locale;

public class Utils {
    public static String escapeString(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d_.-]", "");
    }
}
