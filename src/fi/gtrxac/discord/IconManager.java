package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import fi.gtrxac.bluewap.Util;
import javax.microedition.lcdui.Image;
import tube42.lib.imagelib.ImageUtils;

public class IconManager implements Runnable {
    private static final IconCache iconCache = new IconCache();
    private static final ResizedIconCache resizedIconCache = new ResizedIconCache();

    private static final Vector requests = new Vector();

    public static Image get(String hash, int size, MessageItem callback) {
        // Icon of the exact size directly available -> return it
        Image resizedIcon = resizedIconCache.get(hash, size);
        if (resizedIcon != null) return resizedIcon;

        // Icon available in cache -> queue a resize for it
        Image icon = iconCache.get(hash);
        if (icon != null) {
            addRequest(hash, size, callback);
            return null;
        }

        // Icon not available -> queue a fetch for it
        addRequest(hash, 0, callback);

        return null;
    }

    private static void addRequest(String hash, int size, MessageItem callback) {
        synchronized (requests) {
            // Find if there is an existing matching request
            for (int i = 0; i < requests.size(); i++) {
                IconRequest req = (IconRequest) requests.elementAt(i);
                
                if (req.hash.equals(hash) && req.targetSize == size) {
                    // matching request -> join it
                    req.callbacks.addElement(callback);
                    return;
                }
            }

            // no matching request -> make a new one
            IconRequest req = new IconRequest();
            req.hash = hash;
            req.targetSize = size;
            req.callbacks.addElement(callback);

            requests.addElement(req);
        }
    }

    /**
     * Used for both fetch requests and resize requests.
     * For fetch requests, target size is 0.
     */
    private static class IconRequest {
        public String hash;
        public int targetSize;
        public Vector callbacks = new Vector();
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
                    iconCache.set(req.hash, img);
                }
                else {
                    // Handle resize request
                    Image icon = iconCache.get(req.hash);
                    icon = ImageUtils.resize(icon, req.targetSize, req.targetSize, true, true);

                    resizedIconCache.set(req.hash, req.targetSize, icon);
                }

                // Call callbacks
                for (int i = 0; i < req.callbacks.size(); i++) {
                    MessageItem item = (MessageItem) req.callbacks.elementAt(i);
                    item.avatarLoaded();
                }
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