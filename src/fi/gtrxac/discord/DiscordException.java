package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import java.util.*;

public class DiscordException extends Exception {
    public String friendlyMessage;
    public String message;
    public Vector tips;

    public DiscordException(Exception exception) {
        this(exception.toString());
    }

    public DiscordException(String message) {
        this.message = message;
        this.tips = new Vector();

        if (message.equals("java.io.IOException")) {
            this.friendlyMessage = "Failed to load data";
            this.tips.addElement("try again");
            this.tips.addElement("check that your internet connection is working");
            this.tips.addElement("check that other online apps can connect");
            this.tips.addElement("if you're using mobile data, check that your APN settings are correct");
        }
        else if (message.equals("401: Unauthorized")) {
            this.friendlyMessage = "Invalid session";
            this.tips.addElement("log in again");
            this.tips.addElement("if you're using token login, make sure the token is written correctly");
            this.tips.addElement("use a different connection method");
            this.tips.addElement("use a different account");
        }
    }

    public DiscordException(String friendlyMessage, String message, Vector tips) {
        this.friendlyMessage = friendlyMessage;
        this.message = message;
        this.tips = tips;
    }

    public void show() {
        printStackTrace();

        Dialog d = new Dialog();

        Font largerBold = Fonts.get(1, true, false, false);
        String title = (friendlyMessage != null) ? friendlyMessage : message;

        d.listView.addItem(new StringItem(title, largerBold));

        if (tips != null) {
            d.listView.addItem("Please try the following:");

            String[] tipsArray = new String[tips.size()];
            tips.copyInto(tipsArray);
            d.listView.addItem(new StringListItem(tipsArray));
        }

        if (friendlyMessage != null) {
            d.listView.addItem("Details:\n" + message);
        }

        App.pushScreen(d);
    }

    public String toString() {
        return friendlyMessage + " (" + message + ")";
    }
}