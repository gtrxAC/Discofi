package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import java.util.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

public class MainScreen extends TabbedListScreen implements CommandListener, ListViewItemListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_QUIT = 1;
    public static final int CMD_REFRESH = 2;
    public static final int CMD_LOG_OUT = 3;
    public static final int CMD_QUIT_PROMPT_YES = 100;
    public static final int CMD_QUIT_PROMPT_NO = 101;

    public static final int TAB_GUILDS = 0;
    public static final int TAB_DMS = 1;
    public static final int TAB_INBOX = 2;

    private Account acc;
    private ListView guildsTab;
    private ListView dmsTab;
    private ListView inboxTab;
    private boolean dmChannelsLoaded;
    private boolean inboxLoaded;

    public MainScreen() {
        super();
        setTitle(null);

        acc = Accounts.getActive();

        createGuildsTab();
        createDmsTab();
        createInboxTab();

        addTab("Servers", guildsTab);
        addTab("DMs", dmsTab);
        addTab("Inbox", inboxTab);

        setCommandListener(this);
        addCommand(new Command("Quit", Command.EXIT, CMD_QUIT));
        addCommand(new Command("Refresh", Command.SCREEN, CMD_REFRESH));
        addCommand(new Command("Log out", Command.SCREEN, CMD_LOG_OUT));

        if (!Util.hideSelectCommand) {
            addCommand(new Command("Select", Command.OK, CMD_SELECT));
        }
    }

    private void createGuildsTab() {
        guildsTab = new ListView();
        guildsTab.setItemListener(this);

        if (acc.guildsAvailable()) {
            try {
                Vector guilds = acc.getGuilds(false);
                populateGuildsTab(guilds);
            }
            catch (DiscordException e) {
                e.show();
            }
        }
        else {
            guildsTab.addItem(new LoadingItem());
            fetchTabData(HTTPThread.ACTION_FETCH_GUILDS);
        }
    }

    private void createDmsTab() {
        dmsTab = new ListView();
        dmsTab.setItemListener(this);

        if (acc.dmChannelsAvailable()) {
            try {
                Vector dmChannels = acc.getDMChannels(false);
                populateDMsTab(dmChannels);
            }
            catch (DiscordException e) {
                e.show();
            }
        }
        // If tab data is unavailable, it is only fetched when the tab is selected
    }

    private void createInboxTab() {
        inboxTab = new ListView();
        inboxTab.setItemListener(this);
        // Tab data is only fetched when the tab is selected
    }

    public void populateGuildsTab(Vector guilds) {
        guildsTab.removeAllItems();

        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            
            guildsTab.addItem(new ListItem(g.getName(), g));
        }
    }

    public void populateDMsTab(Vector dmChannels) {
        dmsTab.removeAllItems();

        for (int i = 0; i < dmChannels.size(); i++) {
            DMChannel c = (DMChannel) dmChannels.elementAt(i);
            
            dmsTab.addItem(new ListItem(c.getName(), c));
        }
        dmChannelsLoaded = true;
    }

    public void populateInboxTab(Vector inbox) {
        inboxTab.removeAllItems();

        for (int i = 0; i < inbox.size(); i++) {
            Message m = (Message) inbox.elementAt(i);
            
            inboxTab.addItem(new MessageItem(m));
        }
        inboxLoaded = true;
    }

    public void showErrorOnTab(int tabIndex) {
        ListView tab = (ListView) tabs.elementAt(tabIndex);
        App.showLoadError(tab);

        // Mark tab as loaded so it won't immediately reload when switching to it again
        if (tabIndex == TAB_DMS) dmChannelsLoaded = true;
        else if (tabIndex == TAB_INBOX) inboxLoaded = true;
    }

    private void fetchTabData(int action) {
        new HTTPThread(action, this).start();
        // HTTPThread calls populate*Tab or showErrorOnTab
    }

    private boolean currentTabNeedsLoading() {
        return (currentTabIndex == TAB_DMS && !dmChannelsLoaded) ||
            (currentTabIndex == TAB_INBOX && !inboxLoaded);
    }

    public void currentTabChanged(int currentTabIndex) {
        if (currentTabNeedsLoading()) refreshCurrentTab();
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                getCurrentTab().selectItem();
                break;
            }
            case CMD_QUIT: {
                promptQuit();
                break;
            }
            case CMD_REFRESH: {
                refreshCurrentTab();
                break;
            }
            case CMD_LOG_OUT: {
                App.replaceScreen(new AccountListScreen());
                break;
            }
            case CMD_QUIT_PROMPT_YES: {
                App.instance.notifyDestroyed();
                break;
            }
            case CMD_QUIT_PROMPT_NO: {
                App.popScreen();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
        if (i == App.refreshButton) {
            refreshCurrentTab();
        }
        else if (currentTabIndex == TAB_GUILDS) {
            ListItem li = (ListItem) i;
            Guild guild = (Guild) li.object;
            App.pushScreen(new ChannelListScreen(guild));
        }
        else if (currentTabIndex == TAB_DMS) {
            ListItem li = (ListItem) i;
            DMChannel dmChannel = (DMChannel) li.object;
            App.pushScreen(new ChatScreen(dmChannel));
        }
    }

    private void promptQuit() {
        Dialog quitPrompt = new Dialog("Are you sure you want to quit Microcord?");
        quitPrompt.setCommandListener(this);
        quitPrompt.addCommand(new Command("Yes", Command.OK, CMD_QUIT_PROMPT_YES));
        quitPrompt.addCommand(new Command("No", Command.BACK, CMD_QUIT_PROMPT_NO));
        App.pushScreen(quitPrompt);
    }

    private void refreshCurrentTab() {
        ListView tab = getCurrentTab();
        tab.removeAllItems();
        tab.addItem(new LoadingItem());

        switch (currentTabIndex) {
            case TAB_GUILDS: {
                fetchTabData(HTTPThread.ACTION_FETCH_GUILDS);
                break;
            }
            case TAB_DMS: {
                fetchTabData(HTTPThread.ACTION_FETCH_DM_CHANNELS);
                break;
            }
            case TAB_INBOX: {
                fetchTabData(HTTPThread.ACTION_FETCH_INBOX);
                break;
            }
        }
    }
}