package fi.gtrxac.discord;

// import fi.gtrxac.bluewap.Settings;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Displayable;
import java.util.*;

public class App extends AppBase {
    public static Account pendingAccount;

    public void init() {
        Fonts.loadFonts(Settings.fontSize);
        pushScreen(new AccountSelectionScreen());
    }

    public static void loginCompleted(String token) {
        pendingAccount.token = token;
        Accounts.list.addElement(pendingAccount);
        Settings.save();
        pendingAccount = null;
        replaceAllScreens(new AccountSelectionScreen());
    }
}