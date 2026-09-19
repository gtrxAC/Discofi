package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class MainScreen extends TabbedListScreen implements CommandListener, ListViewItemListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

    private ListView guildsTab;
    private ListView dmsTab;

    public MainScreen() {
        super();
        setTitle("");

        guildsTab = new ListView();
        dmsTab = new ListView();

        addTab("Servers", guildsTab);
        addTab("DMs", dmsTab);

        setCommandListener(this);
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                // selectItem();
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
}