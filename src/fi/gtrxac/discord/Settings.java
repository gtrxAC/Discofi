package fi.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import fi.gtrxac.bluewap.Util;

public class Settings {

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
    }

    private static void writeData(DataOutputStream dos) throws Exception {
        dos.writeInt(Accounts.list.size());

        for (int i = 0; i < Accounts.list.size(); i++) {
            Account acc = (Account) Accounts.list.elementAt(i);
            acc.writeSaveData(dos);
        }

        dos.writeInt(Accounts.getActiveIndex());
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