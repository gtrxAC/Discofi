package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import javax.microedition.lcdui.Image;

public class IconCache extends LRUCache {
    private static final int CACHE_SIZE = 32;

    public IconCache() {
        super(CACHE_SIZE);
    }

    public void readSaveData(DataInputStream saveDataStream) throws IOException {
        cache.clear();

        int numEntries = saveDataStream.readInt();

        for (int i = 0; i < numEntries; i++) {
            String iconHash =
                Long.toString(saveDataStream.readLong()) +
                Long.toString(saveDataStream.readLong());

            long lastUsed = (long) saveDataStream.readInt();
            
            byte[] iconData = new byte[160];
            saveDataStream.read(iconData, 0, iconData.length);
            
            LRUCacheEntry entry = new LRUCacheEntry(iconData, lastUsed);
            set(iconHash, entry);
        }
    }

    public void writeSaveData(DataOutputStream saveDataStream) throws IOException {
        saveDataStream.writeInt(cache.size());

        for (Enumeration e = cache.keys(); e.hasMoreElements(); ) {
            String iconHash = (String) e.nextElement();
            LRUCacheEntry entry = (LRUCacheEntry) cache.get(iconHash);
            byte[] iconData = (byte[]) entry.getObjectWithoutUpdate();

            // subtract current usage counter so the saved last-used values are
            // relative to the 0 usage counter of the next session
            long lastUsed = entry.getLastUsed() - LRUCacheEntry.getUsageCounter();

            saveDataStream.writeLong(Long.parseLong(iconHash.substring(0, 32)));
            saveDataStream.writeLong(Long.parseLong(iconHash.substring(32)));

            saveDataStream.writeInt((int) lastUsed);
            saveDataStream.write(iconData, 0, iconData.length);
        }
    }

    public Image get(String iconHash) {
        byte[] iconData = (byte[]) super.get(iconHash);
        if (iconData == null) return null;
        
        return AvatarCodec.decode(iconData);
    }

    public void set(String iconHash, Image icon) {
        byte[] iconData = AvatarCodec.encode(icon);
        super.set(iconHash, iconData);
    }
}