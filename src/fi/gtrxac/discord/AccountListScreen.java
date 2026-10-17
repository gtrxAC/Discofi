package fi.gtrxac.discord;

import fi.gtrxac.tinytls.*;
import fi.gtrxac.bluewap.Util;
import fi.gtrxac.bluewap.http.*;
import fi.gtrxac.bluewap.client.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.io.*;
import java.io.*;

public class AccountListScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_QUIT = 1;
    public static final int CMD_ADD_ACCOUNT = 2;
    public static final int CMD_EDIT = 3;
    public static final int CMD_DELETE = 4;
    public static final int CMD_SETTINGS = 5;
    public static final int CMD_DELETE_PROMPT_YES = 100;
    public static final int CMD_DELETE_PROMPT_NO = 101;

    private ButtonItem addAccountButton;

    public AccountListScreen() {
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
        addCommand(new Command("Edit", Command.ITEM, CMD_EDIT));
        addCommand(new Command("Delete", Command.ITEM, CMD_DELETE));
        addCommand(new Command("Settings", Command.SCREEN, CMD_SETTINGS));

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
            case CMD_EDIT: {
                Account acc = getHighlightedAccount();
                if (acc == null) break;

                App.pendingAccount = acc;
                App.pushScreen(new ConnectionModeScreen());
                break;
            }
            case CMD_DELETE: {
                promptDelete();
                break;
            }
            case CMD_SETTINGS: {
                App.pushScreen(new SettingsScreen());
                break;
            }
            case CMD_DELETE_PROMPT_YES: {
                Account acc = getHighlightedAccount();
                if (acc == null) break;

                Accounts.list.removeElement(acc);
                Settings.save();

                App.popScreen();

                AccountListScreen newScreen = new AccountListScreen();
                newScreen.setHighlightedIndex(getHighlightedIndex() - 1);
                App.replaceScreen(newScreen);
                break;
            }
            case CMD_DELETE_PROMPT_NO: {
                App.popScreen();
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
            Account acc = Accounts.getActive();

            if (acc.connectionMode == Account.CONNECTION_MODE_BLUETOOTH &&
                BluetoothHTTP.selectedConnectionUrl == null) {

                App.replaceScreen(new BluetoothDeviceScreen());
            }
            else {
                App.replaceScreen(new MainScreen());
            }
        }
    }

    private void addAccount() {
        App.pendingAccount = new Account();
        App.pushScreen(new ConnectionModeScreen());
    }

    private Account getHighlightedAccount() {
        int accountIndex = getHighlightedIndex() - 1;
        if (accountIndex < 0 || accountIndex >= Accounts.list.size()) return null;

        return (Account) Accounts.list.elementAt(accountIndex);
    }

    private void promptDelete() {
        Account acc = getHighlightedAccount();
        if (acc == null) return;
        
        try {
            Dialog prompt = new Dialog(
                "Are you sure you want to delete the account '" +
                acc.getDisplayTitle() +
                "' from the list?");

            prompt.addCommand(new Command("Yes", Command.OK, CMD_DELETE_PROMPT_YES));
            prompt.addCommand(new Command("No", Command.BACK, CMD_DELETE_PROMPT_NO));
            prompt.setCommandListener(this);

            App.pushScreen(prompt);
        }
        catch (DiscordException e) {
            e.show();
        }
    }
}