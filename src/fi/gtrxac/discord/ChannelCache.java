package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;

/**
 * Maintains an LRU cache of up to CACHE_SIZE mappings of guild ID to
 * the guild's channel list (with category structure).
 */
public class ChannelCache extends LRUCache {
    private static final int CACHE_SIZE = 5;

    public ChannelCache() {
        super(CACHE_SIZE);
    }

    public void readSaveData(DataInputStream saveDataStream) throws IOException {
        cache.clear();

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

            LRUCacheEntry entry = new LRUCacheEntry(categories, lastUsed);
            set(guildID, entry);
        }
    }

    public void writeSaveData(DataOutputStream saveDataStream) throws IOException {
        saveDataStream.writeInt(cache.size());

        for (Enumeration e = cache.keys(); e.hasMoreElements(); ) {
            String guildID = (String) e.nextElement();
            LRUCacheEntry entry = (LRUCacheEntry) cache.get(guildID);
            Vector categories = (Vector) entry.getObjectWithoutUpdate();

            // subtract current usage counter so the saved last-used values are
            // relative to the 0 usage counter of the next session
            long lastUsed = entry.getLastUsed() - LRUCacheEntry.getUsageCounter();

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
        return (Vector) super.get(guildID);
    }
}