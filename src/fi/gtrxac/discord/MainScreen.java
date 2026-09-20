package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class MainScreen extends TabbedListScreen implements CommandListener, ListViewItemListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_QUIT = 1;
    public static final int CMD_LOG_OUT = 2;
    public static final int CMD_QUIT_PROMPT_YES = 3;
    public static final int CMD_QUIT_PROMPT_NO = 4;

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
        addCommand(new Command("Quit", Command.EXIT, CMD_QUIT));
        addCommand(new Command("Log out", Command.BACK, CMD_LOG_OUT));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                // selectItem();
                break;
            }
            case CMD_QUIT: {
                promptQuit();
                break;
            }
            case CMD_QUIT_PROMPT_YES: {
                App.instance.notifyDestroyed();
                break;
            }
            case CMD_LOG_OUT: {
                App.replaceScreen(new AccountSelectionScreen());
                break;
            }
            case CMD_QUIT_PROMPT_NO: {
                App.popScreen();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
    }

    private void promptQuit() {
        Dialog quitPrompt = new Dialog("Are you sure you want to quit Microcord?");
        quitPrompt.setCommandListener(this);
        quitPrompt.addCommand(new Command("Yes", Command.OK, CMD_QUIT_PROMPT_YES));
        quitPrompt.addCommand(new Command("No", Command.BACK, CMD_QUIT_PROMPT_NO));
        App.pushScreen(quitPrompt);
    }
}