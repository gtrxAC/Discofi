package fi.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.Font;
import fi.gtrxac.bluewap.Util;

public class Settings {
    public static int fontSize = Font.SIZE_SMALL;
    public static boolean getPairedDeviceNames = true;
    public static Hashtable deviceNameCache = new Hashtable();
    public static String deviceVendorID = DiscordUtil.generateRandomUUID();
    
    private static void readData(DataInputStream dis) throws Exception {
        int numAccounts = dis.readInt();

        if (numAccounts > 0) {
            Accounts.list = new Vector();

            for (int i = 0; i < numAccounts; i++) {
                Accounts.list.addElement(new Account(dis));
            }

            int activeAccount = dis.readInt();
            Accounts.setActive(activeAccount);
        }

        fontSize = dis.readUnsignedByte();
        getPairedDeviceNames = dis.readBoolean();

        int deviceNameCount = dis.readInt();
        deviceNameCache = new Hashtable(deviceNameCount);

        for (int i = 0; i < deviceNameCount; i++) {
            deviceNameCache.put(dis.readUTF(), dis.readUTF());
        }

        deviceVendorID = dis.readUTF();
    }

    private static void writeData(DataOutputStream dos) throws Exception {
        dos.writeInt(Accounts.list.size());

        for (int i = 0; i < Accounts.list.size(); i++) {
            Account acc = (Account) Accounts.list.elementAt(i);
            acc.writeSaveData(dos);
        }

        dos.writeInt(Accounts.getActiveIndex());

        dos.writeByte(fontSize);
        dos.writeBoolean(getPairedDeviceNames);

        dos.writeInt(deviceNameCache.size());

        for (Enumeration e = deviceNameCache.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = (String) deviceNameCache.get(key);
            dos.writeUTF(key);
            dos.writeUTF(value);
        }

        dos.writeUTF(deviceVendorID);
    }

    static {
        load();
    }

    private static void load() {
        RecordStore rms = null;
        ByteArrayInputStream is = null;
        DataInputStream dis = null;

        try {
            rms = RecordStore.openRecordStore("a", false);
            byte[] record = rms.getRecord(1);
            is = new ByteArrayInputStream(record);
            dis = new DataInputStream(is);

            readData(dis);
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

    public static void save() {
        RecordStore rms = null;
        ByteArrayOutputStream os = null;
        DataOutputStream dos = null;

        try {
            rms = RecordStore.openRecordStore("a", true);
            os = new ByteArrayOutputStream();
            dos = new DataOutputStream(os);

            writeData(dos);
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