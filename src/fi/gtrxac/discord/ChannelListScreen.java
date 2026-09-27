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

    public ChannelListScreen(Guild guild) {
        super();
        setTitle(guild.getName());

        addItem(new LoadingItem());

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));

        if (!Util.hideSelectCommand) {
            addCommand(new Command("Select", Command.OK, CMD_SELECT));
        }

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
        }
    }

    public void itemSelected(Item i) {
    }

    public void populate(Vector categories) {
        removeAllItems();

        for (int i = 0; i < categories.size(); i++) {
            ChannelCategory category = (ChannelCategory) categories.elementAt(i);
            addItem(category.name);
            
            for (int j = 0; j < category.children.size(); j++) {
                Channel channel = (Channel) category.children.elementAt(j);

                addItem(new ListItem(channel.name));
            }
        }
    }

    public void showError() {
        removeAllItems();
        App.showLoadError(this);
    }
}