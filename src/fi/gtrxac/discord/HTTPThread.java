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

    private int action;
    private Screen callerScreen;
    private Account acc;

    public String email;
    public String password;
    public String totp;
    public Guild guild;

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

        Vector categories = new Vector();
        Vector textChannelsJson = new Vector();
        Vector voiceChannelsJson = new Vector();

        ChannelCategory defaultCategory = new ChannelCategory((String) null);
        categories.addElement(defaultCategory);

        // Sort all channels and divide them into text/voice channels and channel categories.
        // Voice channels are separate so that they can be added last.
        // If we were to go just by position numbers, VC's would show first.
        // Channels with unsupported types are discarded.
        for (int i = 0; i < numChannels; i++) {
            for (int a = 0; a < numChannels; a++) {
                JSONObject channelJson = channelsJson.getObject(a);
                int position = channelJson.getInt("position", i);
                if (position != i) continue;

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
        }

        groupChannelsIntoCategories(textChannelsJson, categories);
        groupChannelsIntoCategories(voiceChannelsJson, categories);

        if (defaultCategory.children.size() == 0) {
            categories.removeElementAt(0);
        }

        // for (int i = 0; i < categories.size(); i++) {
        //     ChannelCategory c = (ChannelCategory) categories.elementAt(i);

        //     System.out.println("--- " + c.name + " ---");

        //     for (int j = 0; j < c.children.size(); j++) {
        //         Channel cc = (Channel) c.children.elementAt(j);

        //         System.out.println(cc.name);
        //     }

        //     System.out.println("");
        // }

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
}