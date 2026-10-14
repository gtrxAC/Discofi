package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import fi.gtrxac.bluewap.Util;
import fi.gtrxac.discord.model.*;
import javax.microedition.lcdui.Image;
import tube42.lib.imagelib.ImageUtils;

public class IconManager implements Runnable {
    private static final IconCache iconCache = new IconCache();
    private static final ResizedIconCache resizedIconCache = new ResizedIconCache();

    private static final Vector requests = new Vector();

    public static Image get(User user, int size, MessageItem callback) {
        // User doesn't have an icon -> always null
        if (user.iconHash == null) return null;

        // Icon of the exact size directly available -> return it
        Image resizedIcon = resizedIconCache.get(user.iconHash, size);
        if (resizedIcon != null) return resizedIcon;

        // Icon available in cache -> queue a resize for it
        Image icon = iconCache.get(user.iconHash);
        if (icon != null) {
            addRequest(user, size, callback);
            return null;
        }

        // Icon not available -> queue a fetch for it
        addRequest(user, 0, callback);

        return null;
    }

    private static void addRequest(User user, int size, MessageItem callback) {
        synchronized (requests) {
            // Find if there is an existing matching request
            for (int i = 0; i < requests.size(); i++) {
                IconRequest req = (IconRequest) requests.elementAt(i);
                
                if (req.user.id.equals(user.id) && req.targetSize == size) {
                    // matching request -> join it
                    req.callbacks.addElement(callback);
                    return;
                }
            }

            // no matching request -> make a new one
            IconRequest req = new IconRequest(user, size, callback);
            requests.addElement(req);
        }
    }

    /**
     * Worker thread that handles icon requests.
     */
    public void run() {
        while (true) {
            try {
                IconRequest req = null;

                // Get the next pending request or wait for one
                synchronized (requests) {
                    if (requests.size() > 0) {
                        req = (IconRequest) requests.elementAt(0);
                        requests.removeElementAt(0);
                    }
                }
                if (req == null) {
                    Util.sleep(250);
                    continue;
                }

                if (req.targetSize == 0) {
                    // Handle fetch request
                    InputStream test = getClass().getResourceAsStream("/test.jpg");
                    Image img = Image.createImage(test);
                    iconCache.set(req.user.iconHash, img);
                }
                else {
                    // Handle resize request
                    Image icon = iconCache.get(req.user.iconHash);
                    icon = ImageUtils.resize(icon, req.targetSize, req.targetSize, true, true);

                    resizedIconCache.set(req.user.iconHash, req.targetSize, icon);
                }

                req.runCallbacks();
            }
            catch (Exception e) {
                e.printStackTrace();
                Util.sleep(250);
            }
        }
    }

    /**
     * Code required to start the worker thread.
     */
    private IconManager() {}

    static {
        new Thread(new IconManager()).start();
    }
}