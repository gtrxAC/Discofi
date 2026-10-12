package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;

/**
 * Maintains an LRU cache of up to CACHE_SIZE mappings of guild ID to
 * the guild's channel list (with category structure).
 */
public class ChannelCache {
    private static final int CACHE_SIZE = 5;

    private Hashtable cache;

    public ChannelCache() {
        cache = new Hashtable(CACHE_SIZE);
    }

    public void readSaveData(DataInputStream saveDataStream) throws IOException {
        cache = new Hashtable(CACHE_SIZE);

        int numEntries = saveDataStream.readInt();

        for (int i = 0; i < numEntries; i++) {
            String guildID = Long.toString(saveDataStream.readLong());
            long lastUsed = (long) saveDataStream.readInt();
            
            int numCategories = saveDataStream.readShort();
            Vector categories = new Vector(numCategories);

            for (int j = 0; j < numCategories; j++) {
                ChannelCategory category = new ChannelCategory(saveDataStream);
                categories.addElement(category);
            }

            CacheEntry entry = new CacheEntry(categories, lastUsed);
            set(guildID, entry);
        }
    }

    public void writeSaveData(DataOutputStream saveDataStream) throws IOException {
        saveDataStream.writeInt(cache.size());

        for (Enumeration e = cache.keys(); e.hasMoreElements(); ) {
            String guildID = (String) e.nextElement();
            CacheEntry entry = (CacheEntry) cache.get(guildID);
            Vector categories = (Vector) entry.getObjectWithoutUpdate();

            // subtract current usage counter so the saved last-used values are
            // relative to the 0 usage counter of the next session
            long lastUsed = entry.getLastUsed() - CacheEntry.getUsageCounter();

            saveDataStream.writeLong(Long.parseLong(guildID));
            saveDataStream.writeInt((int) lastUsed);
            saveDataStream.writeShort(categories.size());
            
            for (int i = 0; i < categories.size(); i++) {
                ChannelCategory category = (ChannelCategory) categories.elementAt(i);
                category.writeSaveData(saveDataStream);
            }
        }
    }

    public Vector get(String guildID) {
        CacheEntry entry = (CacheEntry) cache.get(guildID);
        if (entry == null) return null;
        return (Vector) entry.getObject();
    }

    private void set(String guildID, CacheEntry entry) {
        deleteOldestItem();
        cache.put(guildID, entry);
    }

    public void set(String guildID, Vector channelCategories) {
        CacheEntry entry = new CacheEntry(channelCategories);
        set(guildID, entry);
    }

    private void deleteOldestItem() {
        if (cache.size() < CACHE_SIZE) return;

        long oldestLastUsed = Integer.MAX_VALUE;
        Object oldestKey = null;

        for (Enumeration e = cache.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            CacheEntry entry = (CacheEntry) cache.get(key);

            long lastUsed = entry.getLastUsed();

            // System.out.println(((String) key) + " -> " + lastUsed);

            if (lastUsed < oldestLastUsed) {
                oldestLastUsed = lastUsed;
                oldestKey = key;
            }
        }

        // System.out.println(oldestKey);
        
        if (oldestKey != null) cache.remove(oldestKey);
    }
}