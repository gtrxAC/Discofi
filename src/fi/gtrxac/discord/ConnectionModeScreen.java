package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

public class ConnectionModeScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

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

        // addItem(new StringItem(
        //     "Using a proxy is unsafe and may lead to account restrictions, but it is required on devices where TLS 1.2 is unavailable.",
        //     Fonts.get(0, true, false, false)));

        // addItem(new StringItem(
        //     "Using a proxy is unsafe and may lead to account restrictions.",
        //     Fonts.get(0, true, false, false)));

        // addItem("TLS 1.2 is available on Symbian with nnproject's TLS patch, and on select Nokia S40 and Sony Ericsson devices.");

        // addItem("To use Bluetooth tethering, connect to a device running BlueWAP Server. This is useful when TLS 1.2 or 2G/3G networks are unavailable. The server device must be accessible at all times.");

        RadioButtonGroup group = new RadioButtonGroup();

        // addItem(directOption = new RadioButtonItem(group, "Direct"));
        // addItem(proxyOption = new RadioButtonItem(group, "Proxy"));

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

        apiUrlFieldPosition = items.size();

        addItem(continueButton = new ButtonItem("Continue"));

        setCommandListener(this);
        addCommand(new Command("Select", Command.OK, CMD_SELECT));
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
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
        if (i == directOption || i == btOption) {
            if (apiUrlField == null) return;

            App.pendingAccount.apiUrl = apiUrlField.getValue();
            removeItem(apiUrlFieldPosition);
            removeItem(apiUrlFieldPosition);
            removeItem(apiUrlFieldPosition);
            apiUrlField = null;
        }
        else if (i == proxyOption) {
            if (apiUrlField != null) return;

            apiUrlField = new TextFieldItem("API URL", App.pendingAccount.apiUrl, 1000, 0);
            insertItem(new BlankItem(0), apiUrlFieldPosition);
            insertItem(apiUrlField, apiUrlFieldPosition);
            insertItem(new LabelItem("API URL"), apiUrlFieldPosition);
        }
        else if (i == continueButton) {
            boolean useProxy = (apiUrlField != null);
            App.pendingAccount.useProxy = useProxy;

            if (useProxy) {
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
}