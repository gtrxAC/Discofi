package fi.gtrxac.discord;

import java.util.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class MainScreen extends TabbedListScreen implements CommandListener, ListViewItemListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_QUIT = 1;
    public static final int CMD_REFRESH = 2;
    public static final int CMD_LOG_OUT = 3;
    public static final int CMD_QUIT_PROMPT_YES = 100;
    public static final int CMD_QUIT_PROMPT_NO = 101;

    private Account acc;
    private ListView guildsTab;
    private ListView dmsTab;
    private boolean dmChannelsLoaded;

    public MainScreen() {
        super();
        setTitle("");

        acc = Accounts.getActive();

        createGuildsTab();
        createDmsTab();

        addTab("Servers", guildsTab);
        addTab("DMs", dmsTab);

        setCommandListener(this);
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Quit", Command.EXIT, CMD_QUIT));
        addCommand(new Command("Refresh", Command.EXIT, CMD_REFRESH));
        addCommand(new Command("Log out", Command.BACK, CMD_LOG_OUT));
    }

    private void createGuildsTab() {
        guildsTab = new ListView();

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
            guildsTab.addItem("Loading...");
            fetchGuildsOrDMChannels(HTTPThread.ACTION_FETCH_GUILDS);
        }
    }

    private void createDmsTab() {
        dmsTab = new ListView();

        if (acc.dmChannelsAvailable()) {
            try {
                Vector dmChannels = acc.getDMChannels(false);
                populateDMsTab(dmChannels);
            }
            catch (DiscordException e) {
                e.show();
            }
        }
        else {
            dmsTab.addItem("Loading...");
            // DMs fetching is started when the DMs tab is selected
        }
    }

    public void populateGuildsTab(Vector guilds) {
        guildsTab.removeAllItems();
        guildsTab.addItem(new BlankItem(0));

        for (int i = 0; i < guilds.size(); i++) {
            Guild g = (Guild) guilds.elementAt(i);
            
            guildsTab.addItem(new ListItem(g.getName()));
        }
    }

    public void populateDMsTab(Vector dmChannels) {
        dmsTab.removeAllItems();
        dmsTab.addItem(new BlankItem(0));

        for (int i = 0; i < dmChannels.size(); i++) {
            DMChannel c = (DMChannel) dmChannels.elementAt(i);
            
            dmsTab.addItem(new ListItem(c.getName()));
        }
        dmChannelsLoaded = true;
    }

    private void fetchGuildsOrDMChannels(int action) {
        HTTPThread h = new HTTPThread(action);
        h.mainScreen = this;
        h.start();
        // HTTPThread calls populateGuilds/DMsTab
    }

    public void currentTabChanged(int currentTabIndex) {
        if (currentTabIndex == 1 && !dmChannelsLoaded) {
            fetchGuildsOrDMChannels(HTTPThread.ACTION_FETCH_DM_CHANNELS);
        }
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                // selectItem();
                break;
            }
            case CMD_QUIT: {
                promptQuit();
                break;
            }
            case CMD_REFRESH: {
                if (currentTabIndex == 0) {
                    guildsTab.removeAllItems();
                    guildsTab.addItem("Loading...");
                    fetchGuildsOrDMChannels(HTTPThread.ACTION_FETCH_GUILDS);
                }
                else {
                    dmsTab.removeAllItems();
                    dmsTab.addItem("Loading...");
                    fetchGuildsOrDMChannels(HTTPThread.ACTION_FETCH_DM_CHANNELS);
                }
                break;
            }
            case CMD_QUIT_PROMPT_YES: {
                App.instance.notifyDestroyed();
                break;
            }
            case CMD_LOG_OUT: {
                App.replaceScreen(new AccountSelectionScreen());
                break;
            }
            case CMD_QUIT_PROMPT_NO: {
                App.popScreen();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
    }

    private void promptQuit() {
        Dialog quitPrompt = new Dialog("Are you sure you want to quit Microcord?");
        quitPrompt.setCommandListener(this);
        quitPrompt.addCommand(new Command("Yes", Command.OK, CMD_QUIT_PROMPT_YES));
        quitPrompt.addCommand(new Command("No", Command.BACK, CMD_QUIT_PROMPT_NO));
        App.pushScreen(quitPrompt);
    }
}