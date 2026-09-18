package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class AccountSelectionScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_QUIT = 1;

    private ButtonItem addAccountButton;

    public AccountSelectionScreen() {
        super();
        setTitle("Select account");

        if (Accounts.list.size() == 0) {
            addItem("No accounts defined. Select 'Add account' to log in.");
        }

        else addItem("" + Accounts.list.size());

        addAccountButton = new ButtonItem("Add account");
        addItem(addAccountButton);

        if (Accounts.list.size() == 0) {
            setHighlightedItem(addAccountButton);
        }

        setCommandListener(this);
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Quit", Command.EXIT, CMD_QUIT));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                selectItem();
                break;
            }
            case CMD_QUIT: {
                App.instance.notifyDestroyed();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
        if (i == addAccountButton) {
            App.pendingAccount = new Account();
            App.pushScreen(new ConnectionModeScreen());
        }
    }
}