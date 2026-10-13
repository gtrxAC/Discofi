package fi.gtrxac.discord;

import javax.microedition.lcdui.Image;

public class IconManager {
    private static final IconCache iconCache = new IconCache();
    private static final ResizedIconCache resizedIconCache = new ResizedIconCache();

    public static Image get(String hash, int size) {
        // Icon of the exact size directly available -> return it
        Image resizedIcon = resizedIconCache.get(hash, size);
        if (resizedIcon != null) return resizedIcon;

        // Icon available in cache -> queue a resize for it
        Image icon = iconCache.get(hash);
        if (icon != null) {
            // ...
            return null;
        }

        // Icon not available -> queue a fetch for it
        // ...

        return null;
    }
}