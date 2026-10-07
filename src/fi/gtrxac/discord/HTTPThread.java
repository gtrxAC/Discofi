package fi.gtrxac.discord;

import java.util.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import fi.gtrxac.discord.model.*;
import cc.nnproject.json.*;

public class HTTPThread extends Thread {
    public static final int ACTION_PASSWORD_LOGIN = 0;
    public static final int ACTION_SUBMIT_TOTP = 1;
    public static final int ACTION_FETCH_GUILDS = 2;
    public static final int ACTION_FETCH_DM_CHANNELS = 3;
    public static final int ACTION_FETCH_INBOX = 4;
    public static final int ACTION_FETCH_GUILD_CHANNELS = 5;
    public static final int ACTION_FETCH_MESSAGES = 6;
    public static final int ACTION_SEND_MESSAGE = 7;

    private int action;
    private Screen callerScreen;
    private Account acc;

    public String email;
    public String password;
    public String totp;
    public Guild guild;
    public Channel channel;
    public String message;
    public String paginationType;
    public String paginationMessage;
    public int newPage;
	String recipientMessage;
	boolean sendPing;

    public HTTPThread(int action) {
        this(action, App.getCurrentScreen());
    }

    public HTTPThread(int action, Screen callerScreen) {
        this.action = action;
        this.callerScreen = callerScreen;
        this.acc = Accounts.getActive();
    }

    public void run() {
        try {
            switch (action) {
                case ACTION_PASSWORD_LOGIN: runPasswordLogin(); break;
                case ACTION_SUBMIT_TOTP: runSubmitTotp(); break;
                case ACTION_FETCH_GUILDS: runFetchGuilds(); break;
                case ACTION_FETCH_DM_CHANNELS: runFetchDMChannels(); break;
                case ACTION_FETCH_INBOX: runFetchInbox(); break;
                case ACTION_FETCH_GUILD_CHANNELS: runFetchGuildChannels(); break;
                case ACTION_FETCH_MESSAGES: runFetchMessages(); break;
                case ACTION_SEND_MESSAGE: runSendMessage(); break;
            }
        }
        catch (DiscordException e) {
            e.show();
        }
    }

    private void runPasswordLogin() {
        ListScreen l = new ListScreen();
        l.addItem(new LoadingItem());
        App.pushScreen(l);

        Util.sleep(5000);

        System.out.println("'"+email+"', '"+password+"'");
        App.replaceScreen(new AuthCodeScreen());
    }

    private void runSubmitTotp() {
        ListScreen l = new ListScreen();
        l.addItem(new LoadingItem());
        App.pushScreen(l);

        Util.sleep(5000);

        App.loginCompleted("Mzk4ODc4NzI3OTM4ODk5OTc3.AAAAAA.AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    private void runFetchGuilds() throws DiscordException {
        MainScreen mainScreen = (MainScreen) callerScreen;

        try {
            Vector guilds = acc.getGuilds(true);
            Settings.save();
            mainScreen.populateGuildsTab(guilds);
        }
        catch (DiscordException e) {
            mainScreen.showErrorOnTab(MainScreen.TAB_GUILDS);
            throw e;
        }
    }

    private void runFetchDMChannels() throws DiscordException {
        MainScreen mainScreen = (MainScreen) callerScreen;

        try {
            Vector dmChannels = acc.getDMChannels(true);
            Settings.save();
            mainScreen.populateDMsTab(dmChannels);
        }
        catch (DiscordException e) {
            mainScreen.showErrorOnTab(MainScreen.TAB_DMS);
            throw e;
        }
    }

    private void runFetchInbox() throws DiscordException {
        MainScreen mainScreen = (MainScreen) callerScreen;

        try {
            Vector inbox = acc.getInbox(true);
            mainScreen.populateInboxTab(inbox);
        }
        catch (DiscordException e) {
            mainScreen.showErrorOnTab(MainScreen.TAB_INBOX);
            throw e;
        }
    }

    private void runFetchGuildChannels() throws DiscordException {
        ChannelListScreen list = (ChannelListScreen) callerScreen;

        try {
            JSONArray channelsJson = (JSONArray) acc.fetchJSON("/guilds/" + guild.id + "/channels");
            Vector channels = parseGuildChannels(channelsJson);
            list.populate(channels);
        }
        catch (DiscordException e) {
            list.showError();
            throw e;
        }
    }

    /**
     * Parse fetched channel list into sorted categories and channels.
     * Returns vector of ChannelCategory.
     */
    private Vector parseGuildChannels(JSONArray channelsJson) {
        int numChannels = channelsJson.size();

        Vector sortedChannels = new Vector();
        Vector sortedPositions = new Vector();

        for (int i = 0; i < numChannels; i++) {
            JSONObject channelJson = channelsJson.getObject(i);
            int position = channelJson.getInt("position", i);
            int insertAt = sortedChannels.size();

            while (insertAt > 0) {
                Integer previousPosition = (Integer) sortedPositions.elementAt(insertAt - 1);
                if (previousPosition.intValue() <= position) break;

                insertAt--;
            }

            sortedChannels.insertElementAt(channelJson, insertAt);
            sortedPositions.insertElementAt(new Integer(position), insertAt);
        }

        Vector textChannelsJson = new Vector();
        Vector voiceChannelsJson = new Vector();
        Vector categories = new Vector();

        ChannelCategory defaultCategory = new ChannelCategory((String) null);
        categories.addElement(defaultCategory);

        // Divide all channels into text/voice channels and channel categories.
        // Voice channels are separate so that they can be added last.
        // If we were to go just by position numbers, VC's would show first.
        // Channels with unsupported types are discarded.
        for (int i = 0; i < sortedChannels.size(); i++) {
            JSONObject channelJson = (JSONObject) sortedChannels.elementAt(i);
            int type = channelJson.getInt("type", 0);

            switch (type) {
                case Channel.TYPE_GUILD_CATEGORY: {
                    categories.addElement(new ChannelCategory(channelJson));
                    break;
                }
                case Channel.TYPE_GUILD_TEXT:
                case Channel.TYPE_GUILD_NEWS:
                case Channel.TYPE_GUILD_FORUM:
                case Channel.TYPE_GUILD_MEDIA: {
                    textChannelsJson.addElement(channelJson);
                    break;
                }
                case Channel.TYPE_GUILD_VOICE: {
                    voiceChannelsJson.addElement(channelJson);
                    break;
                }
            }
        }

        groupChannelsIntoCategories(textChannelsJson, categories);
        groupChannelsIntoCategories(voiceChannelsJson, categories);

        if (defaultCategory.children.size() == 0) {
            categories.removeElementAt(0);
        }

        return categories;
    }

    private void groupChannelsIntoCategories(Vector channels, Vector categories) {
        for (int i = 0; i < channels.size(); i++) {
            JSONObject channelJson = (JSONObject) channels.elementAt(i);
            String parentID = channelJson.getString("parent_id", null);

            // Fallback to default (topmost) category
            ChannelCategory parentCategory = (ChannelCategory) categories.elementAt(0);

            // Find the parent category
            for (int j = 1; j < categories.size(); j++) {
                ChannelCategory category = (ChannelCategory) categories.elementAt(j);

                if (category.id.equals(parentID)) {
                    parentCategory = category;
                    break;
                }
            }

            Channel channel = new Channel(channelJson);
            parentCategory.children.addElement(channel);
        }
    }

    private void runFetchMessages() throws DiscordException {
        ChatScreen chatScreen = (ChatScreen) callerScreen;

        try {
            String url = createFetchMessagesUrl(channel.id, paginationType, paginationMessage);
            JSONArray messagesJson = (JSONArray) acc.fetchJSON(url);
            boolean scrollToTop = ("after".equals(paginationType));

            chatScreen.page = newPage;
            chatScreen.populate(messagesJson, scrollToTop);
        }
        finally {
            chatScreen.setBannerText(null);
        }
    }

    private String createFetchMessagesUrl(String channelID, String paginationType, String paginationMessage) {
        StringBuffer url = new StringBuffer()
            .append("/channels/")
            .append(channel.id)
            .append("/messages");

        boolean hasParameter = false;

        if (paginationType != null) {
            url.append('?').append(paginationType)
                .append('=').append(paginationMessage);

            hasParameter = true;
        }

        url.append(hasParameter ? '&' : '?')
            .append("limit=20");

        return url.toString();
    }

    private void runSendMessage() throws DiscordException {
        ChatScreen chatScreen = (ChatScreen) callerScreen;
        chatScreen.setBannerText("Sending message");

        acc.sendMessage(channel, message, recipientMessage, sendPing);
        chatScreen.setBannerText(null);
        chatScreen.fetchMessages(null, null, 0, false);
    }
}