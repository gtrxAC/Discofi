package fi.gtrxac.discord;

import java.util.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;

public class HTTPThread extends Thread {
    public static final int ACTION_PASSWORD_LOGIN = 0;
    public static final int ACTION_SUBMIT_TOTP = 1;
    public static final int ACTION_FETCH_GUILDS = 2;
    public static final int ACTION_FETCH_DM_CHANNELS = 3;
    public static final int ACTION_FETCH_INBOX = 4;

    private int action;

    public String email;
    public String password;
    public String totp;
    public MainScreen mainScreen;

    public HTTPThread(int action) {
        this.action = action;
    }

    public void run() {
        try {
            switch (action) {
                case ACTION_PASSWORD_LOGIN: runPasswordLogin(); break;
                case ACTION_SUBMIT_TOTP: runSubmitTotp(); break;
                case ACTION_FETCH_GUILDS: runFetchGuilds(); break;
                case ACTION_FETCH_DM_CHANNELS: runFetchDMChannels(); break;
                case ACTION_FETCH_INBOX: runFetchInbox(); break;
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
        try {
            Account acc = Accounts.getActive();
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
        try {
            Account acc = Accounts.getActive();
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
        try {
            Account acc = Accounts.getActive();
            Vector inbox = acc.getInbox(true);
            mainScreen.populateInboxTab(inbox);
        }
        catch (DiscordException e) {
            mainScreen.showErrorOnTab(MainScreen.TAB_INBOX);
            throw e;
        }
    }
}