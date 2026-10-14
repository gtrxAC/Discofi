package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.tinytls.TLSTest;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import java.util.*;

public class App extends AppBase {
    // Account that is currently being created (or edited)
    public static Account pendingAccount;

    // State for the password login flow (fingerprint -> ticket/login instance ID -> TOTP)
    public static String pendingTicket;
    public static String pendingFingerprint;
    public static String pendingLoginInstanceID;

    public static final SmallButtonItem refreshButton = new SmallButtonItem("Refresh");

    public void init() {
        // TLSTest.test();
        Fonts.loadFonts(Settings.fontSize);
        pushScreen(new AccountListScreen());
    }

    public static void loginCompleted(String token) {
        pendingAccount.token = token;

        // Add account if it doesn't already exist.
        // It will already exist if we were editing a previously created account.
        if (!Accounts.list.contains(pendingAccount)) {
            Accounts.list.addElement(pendingAccount);
        }

        Settings.save();
        pendingAccount = null;
        replaceAllScreens(new AccountListScreen());
    }

    public static void showLoadError(ListScreen target) {
        target.removeAllItems();

        int align = Graphics.HCENTER | Graphics.TOP;
        target.addItem(new StringItem("Failed to load data", Fonts.bold, align));
        target.addItem(new StringItem("Check your connection and login details, then try again.", Fonts.plain, align));
        target.addItem(new BlankItem(0));
        target.addItem(refreshButton);

        target.setHighlightedItem(refreshButton);
    }
}