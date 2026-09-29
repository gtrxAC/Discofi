package fi.gtrxac.discord;

import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ChatScreen extends ListScreen implements CommandListener {
    // public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

    public ChatScreen(Channel channel) {
        super(Fonts.height/4, Fonts.height/4);
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

    public void populate(JSONArray messages) {
        removeAllItems();

        for (int i = messages.size() - 1; i >= 0; i--) {
            JSONObject messageJson = messages.getObject(i);
            Message message = new Message(messageJson);

            addItem(new MessageItem(message));
        }

        BlankItem blank = new BlankItem(-itemPadding);
        addItem(blank);
        setHighlightedItem(blank);
    }

    public void showError() {
        removeAllItems();
        App.showLoadError(this);
    }
}