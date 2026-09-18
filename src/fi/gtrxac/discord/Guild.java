package fi.gtrxac.discord;

import cc.nnproject.json.JSONObject;
import java.io.*;

/**
 * Created by gtrx on 30.8.2026.
 */

public class Guild {
    public String id;
    private String name;
    public String iconHash;

    public Guild(JSONObject data) {
        id = data.getString("id");

        if (data.has("name")) {
            System.out.println("Guild name from 'name'");
            name = data.getString("name");
        }
        else if (data.has("properties")) {
            System.out.println("Guild name from 'properties.name'");
            name = data.getObject("properties").getString("name");
        }
        else {
            name = "(unknown)";
        }

        iconHash = data.getString("icon", null);
    }

    public Guild(DataInputStream saveDataStream) throws IOException {
        id = Long.toString(saveDataStream.readLong());
        name = saveDataStream.readUTF();
        iconHash = saveDataStream.readUTF();

        if ("".equals(iconHash)) iconHash = null;
    }

    public void writeSaveData(DataOutputStream saveDataStream) throws IOException {
        saveDataStream.writeLong(Long.parseLong(id));
        saveDataStream.writeUTF(name);
        saveDataStream.writeUTF((iconHash != null) ? iconHash : "");
    }

    public String getName() {
        if (name == null) return "(unknown)";
        return name;
    }
}
