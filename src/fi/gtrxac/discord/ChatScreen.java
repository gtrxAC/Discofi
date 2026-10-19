package fi.gtrxac.discord;

import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.*;
import java.util.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;

public class ChatScreen extends ListScreen implements CommandListener {
    public static final int CMD_BACK = 1;
    public static final int CMD_SEND = 2;
    public static final int CMD_REPLY = 3;
    public static final int CMD_MESSAGE_BOX_SEND = 100;
    public static final int CMD_MESSAGE_BOX_BACK = 101;

    private Channel channel;
    private Vector messages;
    public String messageDraft = "";
    public int page;
    public boolean hasLoaded;

    private static SmallButtonItem olderMessagesButton = new SmallButtonItem("Older messages");
    private static SmallButtonItem newerMessagesButton = new SmallButtonItem("Newer messages");

    public ChatScreen(Channel channel) {
        // By default, fetch latest messages
        this(channel, null, null, 0);
    }

    public ChatScreen(Channel channel, String paginationType, String paginationMessage, int newPage) {
        super(0, Fonts.height/4);
        setTitle(channel.name);

        this.channel = channel;

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Send", Command.SCREEN, CMD_SEND));
        addCommand(new Command("Reply", Command.ITEM, CMD_REPLY));

        fetchMessages(paginationType, paginationMessage, newPage, true);
    }

    public void fetchMessages(String paginationType, String paginationMessage, int newPage, boolean showLoadingAnimation) {
        if (showLoadingAnimation) {
            removeAllItems();
            addItem(new LoadingItem());
        }
        else {
            setBannerText("Loading messages");
        }

        HTTPThread h = new HTTPThread(HTTPThread.ACTION_FETCH_MESSAGES, this);
        h.channel = channel;
        h.paginationType = paginationType;
        h.paginationMessage = paginationMessage;
        h.newPage = newPage;
        h.start();

        IconManager.iconCache.save();
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_BACK: {
                IconManager.iconCache.save();
                App.popScreen();
                break;
            }
            case CMD_SEND: {
                showMessageBox();
                break;
            }
            case CMD_REPLY: {
                Object i = items.elementAt(getHighlightedIndex());
                if (!(i instanceof MessageItem)) break;

                Message replyTarget = ((MessageItem) i).message;
                App.pushScreen(new ReplyScreen(channel, replyTarget, messageDraft));
                break;
            }
            case CMD_MESSAGE_BOX_SEND: {
                TextBox t = (TextBox) App.disp.getCurrent();
                HTTPThread h = new HTTPThread(HTTPThread.ACTION_SEND_MESSAGE, this);
                h.channel = channel;
                h.message = t.getString();
                h.start();

                App.disp.setCurrent(AppCanvas.instance);
                break;
            }
            case CMD_MESSAGE_BOX_BACK: {
                TextBox t = (TextBox) App.disp.getCurrent();
                messageDraft = t.getString();

                App.disp.setCurrent(AppCanvas.instance);
                break;
            }
        }
    }

    private void showMessageBox() {
        TextBox t = new TextBox("Message " + channel.name, messageDraft, 2000, 0);

        t.addCommand(new Command("Send", Command.OK, CMD_MESSAGE_BOX_SEND));
        t.addCommand(new Command("Back", Command.BACK, CMD_MESSAGE_BOX_BACK));
        t.setCommandListener(this);

        App.disp.setCurrent(t);
    }

    public void itemSelected(Item i) {
        if (i == olderMessagesButton) {
            Message first = (Message) messages.elementAt(0);
            fetchMessages("before", first.id, page + 1, false);
        }
        else if (i == newerMessagesButton) {
            Message last = (Message) messages.lastElement();
            fetchMessages("after", last.id, page - 1, false);
        }
    }

    public void populate(JSONArray messagesJson, boolean scrollToTop) {
        Vector newItems = new Vector();

        if (messagesJson.size() >= 20) {
            newItems.addElement(olderMessagesButton);
        }

        addMessageItems(messagesJson, newItems);
        calculateMessageGrouping();

        if (page > 0) {
            newItems.addElement(newerMessagesButton);
        }
        else {
            newItems.addElement(new BlankItem(-itemPadding));
        }

        removeAllItems();
        addItems(newItems);

        if (scrollToTop) {
            setHighlightedItem(olderMessagesButton);
        }
        else {
            setHighlightedItem((Item) items.lastElement());
        }

        hasLoaded = true;
    }

    private void addMessageItems(JSONArray messagesJson, Vector newItemsOutput) {
        messages = new Vector(messagesJson.size());

        for (int i = messagesJson.size() - 1; i >= 0; i--) {
            JSONObject messageJson = messagesJson.getObject(i);
            Message message = new Message(messageJson);

            messages.addElement(message);
            newItemsOutput.addElement(new MessageItem(message));

            if (message.attachments != null) {
                for (int j = 0; j < message.attachments.size(); j++) {
                    Attachment att = (Attachment) message.attachments.elementAt(j);

                    newItemsOutput.addElement(new AttachmentItem(att));
                }
            }
        }
    }

    private void calculateMessageGrouping() {
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
}