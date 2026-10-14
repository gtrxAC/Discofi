package fi.gtrxac.discord;

import java.util.*;
import java.io.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import javax.microedition.lcdui.Image;
import fi.gtrxac.bluewap.Util;
import javax.microedition.rms.*;

public class IconCache extends LRUCache {
    private static final int CACHE_SIZE = 32;

    public IconCache() {
        super(CACHE_SIZE);
        load();
    }

    public void readSaveData(DataInputStream saveDataStream) throws IOException {
        cache.clear();

        byte[] hashBuf = new byte[16];
        int numEntries = saveDataStream.readInt();

        for (int i = 0; i < numEntries; i++) {
            saveDataStream.read(hashBuf);
            String iconHash = Util.bytesToHex(hashBuf);

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

            saveDataStream.write(Util.hexToBytes(iconHash));
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

    private void load() {
        RecordStore rms = null;
        ByteArrayInputStream is = null;
        DataInputStream dis = null;

        try {
            rms = RecordStore.openRecordStore("b", false);
            byte[] record = rms.getRecord(1);
            is = new ByteArrayInputStream(record);
            dis = new DataInputStream(is);

            readSaveData(dis);
        }
        catch (RecordStoreNotFoundException e) {
            // ignore
        }
        catch (EOFException e) {
            // ignore
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Util.closeRecordStore(rms);
            try { is.close(); } catch (Exception e) {}
            try { dis.close(); } catch (Exception e) {}
        }
    }

    public void save() {
        RecordStore rms = null;
        ByteArrayOutputStream os = null;
        DataOutputStream dos = null;

        try {
            rms = RecordStore.openRecordStore("b", true);
            os = new ByteArrayOutputStream();
            dos = new DataOutputStream(os);

            writeSaveData(dos);
            Util.setOrAddRecord(rms, 1, os.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Util.closeRecordStore(rms);
            try { os.close(); } catch (Exception e) {}
            try { dos.close(); } catch (Exception e) {}
        }
    }
}