package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ConnectionModeScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;
    public static final int CMD_CONTINUE = 2;

    private RadioButtonItem directOption;
    private RadioButtonItem btOption;
    private RadioButtonItem proxyOption;
    private ButtonItem continueButton;
    private int apiUrlFieldPosition;
    private TextFieldItem apiUrlField;

    public ConnectionModeScreen() {
        super();
        setTitle("Connection setup");

        addItem(new StringItem("Select your connection method.", Fonts.get(0, true, false, false)));

        RadioButtonGroup group = new RadioButtonGroup();

        addItem(new BlankItem(0));

        addItem(directOption = new RadioButtonItem(group, "Direct"));
        addItem("Recommended - requires TLS 1.2 support (Symbian with nnproject's TLS patch, and select Nokia S40 and Sony Ericsson devices).");

        addItem(new BlankItem(0));

        addItem(btOption = new RadioButtonItem(group, "BT tethering"));
        addItem("Tether to a device running BlueWAP Server. Recommended when TLS 1.2 is unavailable, or in regions without 2G/3G networks.");

        addItem(new BlankItem(0));

        addItem(proxyOption = new RadioButtonItem(group, "Proxy"));
        addItem("Unsafe - may lead to account restrictions. Hosting your own proxy is recommended when using this option.");

        addItem(new BlankItem(0));

        group.setTickedIndex(App.pendingAccount.connectionMode);

        apiUrlFieldPosition = items.size();

        if (!Util.hideSelectCommand) {
            addItem(continueButton = new ButtonItem("Continue"));
        }

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));

        if (!Util.hideSelectCommand) {
            addCommand(new Command("Select", Command.OK, CMD_SELECT));
        }
        else {
            addCommand(new Command("Continue", Command.OK, CMD_CONTINUE));
        }
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
            case CMD_CONTINUE: {
                continueLogin();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
        if (i == directOption || i == btOption) {
            App.pendingAccount.connectionMode = (i == directOption) ?
                Account.CONNECTION_MODE_DIRECT :
                Account.CONNECTION_MODE_BLUETOOTH;

            if (apiUrlField == null) return;

            App.pendingAccount.apiUrl = apiUrlField.getValue();
            removeItem(apiUrlFieldPosition);
            removeItem(apiUrlFieldPosition);
            removeItem(apiUrlFieldPosition);
            apiUrlField = null;
        }
        else if (i == proxyOption) {
            App.pendingAccount.connectionMode = Account.CONNECTION_MODE_PROXY;

            if (apiUrlField != null) return;

            apiUrlField = new TextFieldItem("API URL", App.pendingAccount.apiUrl, 1000, 0);
            insertItem(new BlankItem(0), apiUrlFieldPosition);
            insertItem(apiUrlField, apiUrlFieldPosition);
            insertItem(new LabelItem("API URL"), apiUrlFieldPosition);
        }
        else if (i == continueButton) {
            continueLogin();
        }
    }

    private void continueLogin() {
        if (apiUrlField != null) {
            String apiUrl = apiUrlField.getValue();

            if (apiUrl.trim().length() == 0) {
                new DiscordException(
                    "Please enter an API URL or use a different connection mode",
                    "Proxy API URL is required when using a proxy", null)
                    .show();

                return;
            }

            App.pendingAccount.apiUrl = apiUrl;
        }

        App.pushScreen(new LoginScreen());
    }
}