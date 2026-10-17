package fi.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.Font;
import fi.gtrxac.bluewap.*;
import fi.gtrxac.discord.util.*;

public class Settings implements RecordIOImpl {
    /**
     * Global font size preset used by the UI framework.
     */
    public static int fontSize = Font.SIZE_SMALL;
    
    /**
     * Whether to fetch paired BT device names. On some devices this is instant,
     * while on some this may freeze the app for e.g. up to 10 seconds per device.
     * This gets automatically disabled if fetching takes too long.
     */
    public static boolean getPairedDeviceNames = true;

    /**
     * MAC address - name mappings of Bluetooth devices
     */
    public static Hashtable deviceNameCache = new Hashtable();

    /**
     * Device vendor ID used in Discord X-Super-Properties header (persistent random UUID for each device)
     */
    public static String deviceVendorID = DiscordUtil.generateRandomUUID();

    /**
     * Use light UI theme instead of dark theme.
     * This may be reworked later if/when more themes are added.
     */
    public static boolean lightTheme = false;
    
    public void readData(DataInputStream dis) throws IOException {
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
        lightTheme = dis.readBoolean();
    }

    public void writeData(DataOutputStream dos) throws IOException {
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
        dos.writeBoolean(lightTheme);
    }

    private Settings() {}
    private static Settings instance = new Settings();

    public static void save() {
        RecordIO.save("a", instance);
    }

    static {
        RecordIO.load("a", instance);
    }
}