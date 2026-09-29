package fi.gtrxac.discord;

import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import java.util.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ChatScreen extends ListScreen implements CommandListener {
    // public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

    private Vector messages;

    public ChatScreen(Channel channel) {
        super(0, Fonts.height/4);
        setTitle(channel.name);

        addItem(new LoadingItem());

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));

        // if (!Util.hideSelectCommand) {
        //     addCommand(new Command("Select", Command.OK, CMD_SELECT));
        // }

        HTTPThread h = new HTTPThread(HTTPThread.ACTION_FETCH_MESSAGES, this);
        h.channel = channel;
        h.start();
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            // case CMD_SELECT: {
            //     selectItem();
            //     break;
            // }
            case CMD_BACK: {
                App.popScreen();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
    }

    public void populate(JSONArray messagesJson) {
        removeAllItems();

        messages = new Vector(messagesJson.size());

        for (int i = messagesJson.size() - 1; i >= 0; i--) {
            JSONObject messageJson = messagesJson.getObject(i);
            Message message = new Message(messageJson);

            messages.addElement(message);
            addItem(new MessageItem(message));
        }

        calculateMessageGrouping();

        BlankItem blank = new BlankItem(-itemPadding);
        addItem(blank);
        setHighlightedItem(blank);
    }

    public void calculateMessageGrouping() {
        if (messages.size() == 0) return;

        Message first = (Message) messages.elementAt(0);
        Message above = first;
        String clusterStartID = first.id;
        first.showAuthor = true;

        if (messages.size() == 1) return;

        for (int i = 1; i < messages.size(); i++) {
            Message msg = (Message) messages.elementAt(i);

            // Recalculate "should show author" value for message
            msg.showAuthor = msg.shouldShowAuthor(above, clusterStartID);

            if (msg.showAuthor) clusterStartID = msg.id;
            above = msg;
        }
    }

    public void showError() {
        removeAllItems();
        App.showLoadError(this);
    }
}