package fi.gtrxac.discord;

import fi.gtrxac.bluewap.Util;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class AccountSelectionScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_QUIT = 1;
    public static final int CMD_ADD_ACCOUNT = 2;

    private ButtonItem addAccountButton;

    public AccountSelectionScreen() {
        super();
        setTitle("Select account");

        int numAccounts = Accounts.list.size();

        if (numAccounts == 0) {
            addItem("No accounts defined. Select 'Add account' to log in.");
        }
        else {
            addItem(new BlankItem(0));

            for (int i = 0; i < numAccounts; i++) {
                Account acc = (Account) Accounts.list.elementAt(i);

                try {
                    addItem(new ListItem(acc.getDisplayTitle()));
                }
                catch (DiscordException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!Util.hideSelectCommand) {
            addItem(addAccountButton = new ButtonItem("Add account"));

            if (numAccounts == 0) {
                setHighlightedItem(addAccountButton);
            }
        }

        setCommandListener(this);
        addCommand(new Command("Quit", Command.EXIT, CMD_QUIT));

        if (!Util.hideSelectCommand) {
            addCommand(new Command("Select", Command.OK, CMD_SELECT));
        }
        else {
            addCommand(new Command("Add", Command.OK, CMD_ADD_ACCOUNT));
        }
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
            case CMD_ADD_ACCOUNT: {
                addAccount();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
        if (i == addAccountButton) {
            addAccount();
        }
        else {
            // -1 because of the blank item
            int accountIndex = getHighlightedIndex() - 1;
            Accounts.setActive(accountIndex);
            App.replaceScreen(new MainScreen());
        }
    }

    private void addAccount() {
        App.pendingAccount = new Account();
        App.pushScreen(new ConnectionModeScreen());
    }
}