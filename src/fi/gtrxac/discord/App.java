package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import java.util.*;

public class App extends AppBase {
    public static Account pendingAccount;

    public static final ButtonItem refreshButton = new ButtonItem("Refresh");

    public void init() {
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