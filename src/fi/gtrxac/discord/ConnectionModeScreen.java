package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ConnectionModeScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

    private RadioButtonItem directOption;
    private RadioButtonItem proxyOption;
    private ButtonItem continueButton;
    private int apiUrlFieldPosition;
    private TextFieldItem apiUrlField;

    public ConnectionModeScreen() {
        super();
        setTitle("Connection setup");

        addItem("Select your connection method.");

        addItem(new StringItem(
            "Using a proxy is unsafe and may lead to account restrictions, but it is required on devices where TLS 1.2 is not available.",
            Fonts.get(0, true, false, false)));

        addItem("TLS 1.2 is available on Symbian with nnproject's TLS patch, and on select Nokia S40 and Sony Ericsson devices.");

        RadioButtonGroup group = new RadioButtonGroup();

        addItem(directOption = new RadioButtonItem(group, "Direct"));
        addItem(proxyOption = new RadioButtonItem(group, "Proxy"));

        apiUrlFieldPosition = items.size();

        addItem(continueButton = new ButtonItem("Continue"));

        setCommandListener(this);
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_SELECT: {
                break;
            }
            case CMD_BACK: {
                App.popScreen();
                break;
            }
        }
    }

    public void itemSelected(Item i) {
        if (i == directOption) {
            if (apiUrlField == null) return;

            App.pendingAccount.apiUrl = apiUrlField.getValue();
            removeItem(apiUrlFieldPosition);
            removeItem(apiUrlFieldPosition);
            apiUrlField = null;
        }
        else if (i == proxyOption) {
            if (apiUrlField != null) return;

            apiUrlField = new TextFieldItem("API URL", App.pendingAccount.apiUrl, 1000, 0);
            insertItem(apiUrlField, apiUrlFieldPosition);
            insertItem("API URL", apiUrlFieldPosition);
        }
        else if (i == continueButton) {
            boolean useProxy = (apiUrlField != null);
            App.pendingAccount.useProxy = useProxy;

            if (useProxy) {
                App.pendingAccount.apiUrl = apiUrlField.getValue();
            }

            App.pushScreen(new LoginScreen());
        }
    }
}