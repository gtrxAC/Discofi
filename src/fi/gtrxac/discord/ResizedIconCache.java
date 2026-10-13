package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import javax.microedition.lcdui.Image;

/**
 * LRU Cache for resized user avatars.
 * 
 * Unlike the IconCache, which is for 16px avatars,
 * these resized avatars are not saved to persistent storage.
 */
public class ResizedIconCache {
    private static final int CACHE_SIZE = 16;

    private static final Hashtable cache = new Hashtable(CACHE_SIZE);

    public static Image get(String iconHash, int size) {
        String key = iconHash + size;
        CacheEntry entry = (CacheEntry) cache.get(key);
        if (entry == null) return null;
        
        return (Image) entry.getObject();
    }

    private static void set(String key, CacheEntry entry) {
        deleteOldestItem();
        cache.put(key, entry);
    }

    public static void set(String iconHash, int size, Image icon) {
        String key = iconHash + size;
        CacheEntry entry = new CacheEntry(icon);
        set(key, entry);
    }

    private static void deleteOldestItem() {
        if (cache.size() < CACHE_SIZE) return;

        long oldestLastUsed = Integer.MAX_VALUE;
        Object oldestKey = null;

        for (Enumeration e = cache.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            CacheEntry entry = (CacheEntry) cache.get(key);

            long lastUsed = entry.getLastUsed();

            if (lastUsed < oldestLastUsed) {
                oldestLastUsed = lastUsed;
                oldestKey = key;
            }
        }
        
        if (oldestKey != null) cache.remove(oldestKey);
    }
}