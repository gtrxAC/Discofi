package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import java.util.Vector;
import cc.nnproject.json.*;
import fi.gtrxac.bluewap.http.HTTP;
import java.io.*;

/**
 * Created by gtrx on 29.8.2026.
 */

public class Account {
    // HTTP connection methods (direct over system HTTP, direct over BlueWAP, proxy over system HTTP)
    public static final int CONNECTION_MODE_DIRECT = 0;
    public static final int CONNECTION_MODE_BLUETOOTH = 1;
    public static final int CONNECTION_MODE_PROXY = 2;

    // Version numbers for account save data.
    public static final int VERSION_BASE = 1;
    public static final int VERSION_CURRENT = 1;

    public String token = "";
    public String username;
    public int connectionMode;
    public String apiUrl = "";
    public String email = "";
    public String password = "";

    private final Vector guildsUnsorted = new Vector();
    private final Vector guilds = new Vector();
    private final Vector dmChannels = new Vector();
    private final Vector inbox = new Vector();

    public Account() {
    }

    public Account(DataInputStream saveDataStream) throws Exception {
        this();
        int version = saveDataStream.readUnsignedByte();
        token = saveDataStream.readUTF();
        username = saveDataStream.readUTF();
        connectionMode = saveDataStream.readUnsignedByte();
        apiUrl = saveDataStream.readUTF();
        email = saveDataStream.readUTF();
        password = saveDataStream.readUTF();

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
        saveDataStream.writeByte(VERSION_CURRENT);
        saveDataStream.writeUTF(token);
        saveDataStream.writeUTF((username != null) ? username : "");
        saveDataStream.writeByte(connectionMode);
        saveDataStream.writeUTF(apiUrl);
        saveDataStream.writeUTF(email);
        saveDataStream.writeUTF(password);

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
        return (connectionMode == CONNECTION_MODE_PROXY) ? apiUrl : "https://discord.com";
    }

    public HTTP createApiRequest(String method, String route) throws Exception {
        HTTP result = HTTP.createRequest(method, getApiUrl() + "/api/v9" + route);
        
        if (token != null && token.length() > 0) {
            result.setHeader("Authorization", token);
        }
        result.setHeader("X-Super-Properties", SuperProperties.get());
        result.setHeader("X-Discord-Locale", "en-US");
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

    public boolean inboxAvailable() {
        return inbox.size() != 0;
    }

    public AbstractJSON fetchJSON(String route) throws DiscordException {
        int responseCode;
        String responseText;

        try {
            HTTP h = createApiRequest("GET", route);
            responseCode = h.getResponseCode();
            responseText = h.getResponseString();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DiscordException(e);
        }
        
        return handleJSONResponse(responseCode, responseText);
    }

    /**
     * Perform an authenticated POST request with a JSON body and parse the response as JSON.
     */
    public AbstractJSON postJSON(String route, JSONObject body) throws DiscordException {
        int responseCode;
        String responseText;

        try {
            HTTP h = createApiRequest("POST", route);
            h.setData(body.build());
            responseCode = h.getResponseCode();
            responseText = h.getResponseString();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DiscordException(e);
        }

        return handleJSONResponse(responseCode, responseText);
    }

    private AbstractJSON handleJSONResponse(int responseCode, String responseText) throws DiscordException {
        if (responseCode < 200 || responseCode >= 300) {
            throw new DiscordException(getApiErrorMessage(responseCode, responseText));
        }

        return JSON.get(responseText);
    }

    /**
     * Get user's guild (server) list. Returns vector of Guilds.
     * As the list is sorted by user-defined order, this makes up to two HTTP requests; the second one is for the user's settings.
     */
    public Vector getGuilds(boolean refresh) throws DiscordException {
        if (!refresh && guildsAvailable()) return guilds;

        if (refresh || guildsUnsorted.size() == 0) {
            JSONArray guildsJson = (JSONArray) fetchJSON("/users/@me/guilds");
            guildsUnsorted.setSize(0);

            for (int i = 0; i < guildsJson.size(); i++) {
                JSONObject guildJson = guildsJson.getObject(i);
                guildsUnsorted.addElement(new Guild(guildJson));
            }
        }

        // fetch user's guild folders which we can use to sort guilds by
        // user-defined order like in official clients
        JSONObject userSettings = (JSONObject) fetchJSON("/users/@me/settings");
        JSONArray guildFolders = userSettings.getArray("guild_folders");

        guilds.setSize(0);

        for (int i = 0; i < guildFolders.size(); i++) {
            JSONArray guildIds = guildFolders.getObject(i).getArray("guild_ids");

            for (int j = 0; j < guildIds.size(); j++) {
                String guildId = guildIds.getString(j);

                for (int k = 0; k < guildsUnsorted.size(); k++) {
                    Guild guild = (Guild) guildsUnsorted.elementAt(k);

                    if (guild.id.equals(guildId)) {
                        guilds.addElement(guild);
                        guildsUnsorted.removeElementAt(k);
                        break;
                    }
                }
            }
        }

        // If the user doesn't have guild folders, some or all of the guilds may have been
        // missed by the above sort, so add them at the end of the list
        for (int i = 0; i < guildsUnsorted.size(); i++) {
            guilds.addElement(guildsUnsorted.elementAt(i));
        }
        guildsUnsorted.setSize(0);

        return guilds;
    }

    public Vector getDMChannels(boolean refresh) throws DiscordException {
        if (!refresh && dmChannelsAvailable()) return dmChannels;

        JSONArray dmChannelsJson = (JSONArray) fetchJSON("/users/@me/channels");
        Vector dmChannelsUnsorted = new Vector();

        for (int i = 0; i < dmChannelsJson.size(); i++) {
            JSONObject dmChannelJson = dmChannelsJson.getObject(i);
            dmChannelsUnsorted.addElement(new DMChannel(dmChannelJson));
        }

        dmChannels.setSize(0);
        sortDMChannels(dmChannelsUnsorted);
        return dmChannels;
    }

    private void sortDMChannels(Vector dmChannelsUnsorted) {
        // Get the 20 latest DMs (add into another vector sorted by highest to lowest last message ID)
        int count = Math.min(20, dmChannelsUnsorted.size());
        
        for (int i = 0; i < count; i++) {
            long highestID = 0;
            int highestIndex = 0;

            for (int u = 0; u < dmChannelsUnsorted.size(); u++) {
                DMChannel ch = (DMChannel) dmChannelsUnsorted.elementAt(u);

                if (ch.lastMessageID <= highestID) continue;

                // Don't repeat ones that we've already added
                boolean alreadyHave = false;

                for (int l = 0; l < dmChannels.size(); l++) {
                    DMChannel compare = (DMChannel) dmChannels.elementAt(l);

                    if (compare.lastMessageID == ch.lastMessageID) {
                        alreadyHave = true;
                        break;
                    }
                }
                if (alreadyHave) continue;

                highestID = ch.lastMessageID;
                highestIndex = u;
            }
            dmChannels.addElement(dmChannelsUnsorted.elementAt(highestIndex));
        }
    }

    public Vector getInbox(boolean refresh) throws DiscordException {
        if (!refresh && inboxAvailable()) return inbox;

        JSONArray inboxJson = (JSONArray) fetchJSON("/users/@me/mentions");

        for (int i = 0; i < inboxJson.size(); i++) {
            JSONObject mentionJson = inboxJson.getObject(i);
            inbox.addElement(new Message(mentionJson));
        }

        return inbox;
    }

    /**
     * Send a message to a channel. Returns the created message.
     */
    public Message sendMessage(Channel channel, String content, String recipientMessageId, boolean mention) throws DiscordException {
        boolean isDM = (channel instanceof DMChannel);

        JSONObject body = new JSONObject();
        body.put("content", content);
        body.put("flags", 0);
        body.put("mobile_network_type", "unknown");
        body.put("tts", false);

        // Reply
        if (recipientMessageId != null) {
            JSONObject ref = new JSONObject();
            ref.put("message_id", recipientMessageId);
            body.put("message_reference", ref);

            if (!mention && !isDM) {
                JSONObject ping = new JSONObject();
                ping.put("replied_user", false);
                body.put("allowed_mentions", ping);
            }
        }

        JSONObject created = (JSONObject) postJSON("/channels/" + channel.id + "/messages", body);
        return new Message(created);
    }
}
