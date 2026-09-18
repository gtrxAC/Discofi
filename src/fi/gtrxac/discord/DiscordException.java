package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import java.util.*;

public class DiscordException extends Exception {
    public String friendlyMessage;
    public String message;
    public Vector tips;

    public DiscordException(Exception exception) {
        this(exception.toString());
    }

    public DiscordException(String message) {
        this.friendlyMessage = "Test user-friendly error message";
        this.message = message;
        this.tips = new Vector();
        this.tips.addElement("Test tip 1");
        this.tips.addElement("Test tip 2");
    }

    public DiscordException(String friendlyMessage, String message, Vector tips) {
        this.friendlyMessage = friendlyMessage;
        this.message = message;
        this.tips = tips;
    }

    public void show() {
        StringBuffer textBuf = new StringBuffer(friendlyMessage);
            
        if (tips != null) {
            textBuf.append("\n\nPlease try the following:");

            for (int i = 0; i < tips.size(); i++) {
                String tip = (String) tips.elementAt(i);

                textBuf.append("\n- ").append(tip);
            }
        }

        textBuf.append("\n\nDetails:\n")
            .append(message);

        Dialog d = new Dialog(textBuf.toString());
        App.pushScreen(d);
    }
}