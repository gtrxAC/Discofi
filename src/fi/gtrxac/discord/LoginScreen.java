package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;

public class LoginScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

    private TextFieldItem emailField;
    private TextFieldItem passwordField;
    private TextFieldItem tokenField;
    private ButtonItem continueButton;

    public LoginScreen() {
        super();
        setTitle("Log in");

        addItem("To log in with email and password, enable multi-factor authentication (authenticator app) on your account.");

        addItem("Email");
        addItem(emailField = new TextFieldItem("Email", "", 100, TextField.EMAILADDR));

        addItem("Password");
        addItem(passwordField = new TextFieldItem("Password", "", 100, TextField.PASSWORD));

        addItem("Token login is unsafe and only intended for advanced users.");

        addItem("Token");
        addItem(tokenField = new TextFieldItem("Token", "", 80, 0));

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
    }
}