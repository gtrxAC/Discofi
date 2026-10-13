package fi.gtrxac.discord;

import javax.microedition.lcdui.Image;

public class IconManager {
    public static Image get(String hash, int size) {
        // Icon of the exact size directly available -> return it

        // Icon available in cache -> queue a resize for it

        // Icon not available -> queue a fetch for it

        return null;
    }
}