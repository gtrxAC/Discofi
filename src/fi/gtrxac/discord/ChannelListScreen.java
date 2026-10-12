package fi.gtrxac.discord;

import java.util.*;
import cc.nnproject.json.*;
import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ChannelListScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;
    public static final int CMD_REFRESH = 2;

    private Guild guild;

    public ChannelListScreen(Guild guild) {
        super();
        setTitle(guild.getName());

        this.guild = guild;

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(new Command("Refresh", Command.SCREEN, CMD_REFRESH));

        if (!Util.hideSelectCommand) {
            addCommand(new Command("Select", Command.OK, CMD_SELECT));
        }
        
        fetchData();
    }

    private void fetchData() {
        removeAllItems();
        addItem(new LoadingItem());

        HTTPThread h = new HTTPThread(HTTPThread.ACTION_FETCH_GUILD_CHANNELS, this);
        h.guild = guild;
        h.start();
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                selectItem();
                break;
            }
            case CMD_BACK: {
                App.popScreen();
                break;
            }
            case CMD_REFRESH: {
                fetchData();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
        if (i instanceof ListItem) {
            ListItem li = (ListItem) i;
            Channel channel = (Channel) li.object;
            App.pushScreen(new ChatScreen(channel));
        }
        else if (i == App.refreshButton) {
            fetchData();
        }
    }

    public void populate(Vector categories) {
        removeAllItems();

        for (int i = 0; i < categories.size(); i++) {
            ChannelCategory category = (ChannelCategory) categories.elementAt(i);

            if (category.name != null) {
                addItem(new ChannelCategoryItem(category.name));
                addItem(new BlankItem(0));
            }
            
            for (int j = 0; j < category.children.size(); j++) {
                Channel channel = (Channel) category.children.elementAt(j);

                addItem(new ListItem(channel.name, channel));
            }
        }
    }

    public void showError() {
        removeAllItems();
        App.showLoadError(this);
    }
}