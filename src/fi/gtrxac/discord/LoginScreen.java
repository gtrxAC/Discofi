package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Font;

public class LoginScreen extends ListScreen implements CommandListener {
    public static final int CMD_SELECT = 0;
    public static final int CMD_BACK = 1;

    private TextFieldItem emailField;
    private TextFieldItem passwordField;
    private TextFieldItem tokenField;
    private ButtonItem loginButton;
    private ButtonItem tokenLoginButton;

    public LoginScreen() {
        super();
        setTitle("Log in");

        addItem("To log in with email and password, enable multi-factor authentication (authenticator app) on your account.");

        addItem(new LabelItem("Email"));
        addItem(emailField = new TextFieldItem("Email", "", 100, TextField.EMAILADDR));

        addItem(new LabelItem("Password"));
        addItem(passwordField = new TextFieldItem("Password", "", 100, TextField.PASSWORD));

        addItem(new BlankItem(0));

        addItem(loginButton = new ButtonItem("Log in"));

        addItem(new BlankItem(0));

        addItem("Token login is unsafe and only intended for advanced users.");

        addItem(new LabelItem("Token"));
        addItem(tokenField = new TextFieldItem("Token", "", 80, 0));

        addItem(new BlankItem(0));

        addItem(tokenLoginButton = new ButtonItem("Use token"));

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