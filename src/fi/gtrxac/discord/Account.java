package fi.gtrxac.discord;

import java.util.Vector;
import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONException;
import cc.nnproject.json.JSONObject;
import fi.gtrxac.bluewap.http.HTTP;
import java.io.*;

/**
 * Created by gtrx on 29.8.2026.
 */

public class Account {
    public String token;
    public String username;
    public boolean useProxy;
    public String apiUrl;

    // public String email;
    // public String password;

    private Vector guilds;
    private Vector dmChannels;

    public Account() {
        guilds = new Vector();
        dmChannels = new Vector();
        apiUrl = "";
    }

    public Account(DataInputStream saveDataStream) throws Exception {
        this();
        token = saveDataStream.readUTF();
        username = saveDataStream.readUTF();
        useProxy = saveDataStream.readBoolean();
        apiUrl = saveDataStream.readUTF();

        if ("".equals(username)) username = null;

        int numGuilds = saveDataStream.readInt();

        for (int i = 0; i < numGuilds; i++) {
            guilds.addElement(new Guild(saveDataStream));
        }

        int numDMChannels = saveDataStream.readInt();

        for (int i = 0; i < numDMChannels; i++) {
            dmChannels.addElement(new DMChannel(saveDataStream));
        }
    }

    public void writeSaveData(DataOutputStream saveDataStream) throws Exception {
        saveDataStream.writeUTF(token);
        saveDataStream.writeUTF((username != null) ? username : "");
        saveDataStream.writeBoolean(useProxy);
        saveDataStream.writeUTF(apiUrl);

        saveDataStream.writeInt(guilds.size());

        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            g.writeSaveData(saveDataStream);
        }

        saveDataStream.writeInt(dmChannels.size());

        for (int i = 0; i < dmChannels.size(); i++) {
            DMChannel c = (DMChannel) dmChannels.elementAt(i);
            c.writeSaveData(saveDataStream);
        }
    }

    public String getDisplayTitle() throws DiscordException {
        if (username != null) return username;
        return getUserID();
    }

    public String getUserID() throws DiscordException {
        return DiscordUtil.getUserIDFromToken(token);
    }

    public String getApiUrl() {
        return useProxy ? apiUrl : "https://discord.com";
    }

    public HTTP createApiRequest(String method, String route) throws Exception {
        HTTP result = HTTP.createRequest(method, getApiUrl() + "/api/v9" + route);
        result.setHeader("Authorization", token);
        result.setHeader("X-Super-Properties", SuperProperties.get());
        return result;
    }

    public String getApiErrorMessage(int responseCode, String responseText) {
        try {
            JSONObject json = JSON.getObject(responseText);
            return json.getString("message");
        }
        catch (JSONException e) {}

        return "HTTP error " + responseCode;
    }

    public boolean guildsAvailable() {
        return guilds.size() != 0;
    }

    public boolean dmChannelsAvailable() {
        return dmChannels.size() != 0;
    }

    private JSONArray fetchGuildsOrDms(String route) throws DiscordException {
        int responseCode;
        String responseText;

        try {
            HTTP h = createApiRequest("GET", route);
            responseCode = h.getResponseCode();
            responseText = h.getResponseString();
        }
        catch (Exception e) {
            throw new DiscordException(e);
        }

        if (responseCode < 200 || responseCode >= 300) {
            throw new DiscordException(getApiErrorMessage(responseCode, responseText));
        }

        return JSON.getArray(responseText);
    }

    public Vector getGuilds(boolean refresh) throws DiscordException {
        if (!refresh && guildsAvailable()) return guilds;

        JSONArray guildsJson = fetchGuildsOrDms("/users/@me/guilds");

        for (int i = 0; i < guildsJson.size(); i++) {
            JSONObject guildJson = guildsJson.getObject(i);
            guilds.addElement(new Guild(guildJson));
        }

        return guilds;
    }

    public Vector getDmChannels(boolean refresh) throws DiscordException {
        if (!refresh && dmChannelsAvailable()) return dmChannels;

        JSONArray dmsJson = fetchGuildsOrDms("/users/@me/channels");

        for (int i = 0; i < dmsJson.size(); i++) {
            JSONObject dmJson = dmsJson.getObject(i);
            dmChannels.addElement(new DMChannel(dmJson));
        }

        return dmChannels;
    }
}
