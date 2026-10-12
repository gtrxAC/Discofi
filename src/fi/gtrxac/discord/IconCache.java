package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import javax.microedition.lcdui.Image;

public class IconCache {
    private static final int CACHE_SIZE = 32;

    private Hashtable cache;

    public IconCache() {
        cache = new Hashtable(CACHE_SIZE);
    }

    public void readSaveData(DataInputStream saveDataStream) throws IOException {
        cache = new Hashtable(CACHE_SIZE);

        int numEntries = saveDataStream.readInt();

        for (int i = 0; i < numEntries; i++) {
            String iconHash =
                Long.toString(saveDataStream.readLong()) +
                Long.toString(saveDataStream.readLong());

            long lastUsed = (long) saveDataStream.readInt();
            
            byte[] iconData = new byte[160];
            saveDataStream.read(iconData, 0, iconData.length);
            
            CacheEntry entry = new CacheEntry(iconData, lastUsed);
            set(iconHash, entry);
        }
    }

    public void writeSaveData(DataOutputStream saveDataStream) throws IOException {
        saveDataStream.writeInt(cache.size());

        for (Enumeration e = cache.keys(); e.hasMoreElements(); ) {
            String iconHash = (String) e.nextElement();
            CacheEntry entry = (CacheEntry) cache.get(iconHash);
            byte[] iconData = (byte[]) entry.getObjectWithoutUpdate();

            // subtract current usage counter so the saved last-used values are
            // relative to the 0 usage counter of the next session
            long lastUsed = entry.getLastUsed() - CacheEntry.getUsageCounter();

            saveDataStream.writeLong(Long.parseLong(iconHash.substring(0, 32)));
            saveDataStream.writeLong(Long.parseLong(iconHash.substring(32)));

            saveDataStream.writeInt((int) lastUsed);
            saveDataStream.write(iconData, 0, iconData.length);
        }
    }

    public Image get(String iconHash) {
        CacheEntry entry = (CacheEntry) cache.get(iconHash);
        if (entry == null) return null;

        byte[] iconData = (byte[]) entry.getObject();
        return AvatarCodec.decode(iconData);
    }

    private void set(String iconHash, CacheEntry entry) {
        deleteOldestItem();
        cache.put(iconHash, entry);
    }

    public void set(String iconHash, Image icon) {
        byte[] iconData = AvatarCodec.encode(icon);
        CacheEntry entry = new CacheEntry(iconData);
        set(iconHash, entry);
    }

    private void deleteOldestItem() {
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