package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;

public class HTTPThread extends Thread {
    public static final int ACTION_PASSWORD_LOGIN = 0;
    public static final int ACTION_SUBMIT_TOTP = 1;

    private int action;

    public String email;
    public String password;
    public String totp;

    public HTTPThread(int action) {
        this.action = action;
    }

    public void run() {
        switch (action) {
            case ACTION_PASSWORD_LOGIN: runPasswordLogin(); break;
            case ACTION_SUBMIT_TOTP: runSubmitTotp(); break;
        }
    }

    private void runPasswordLogin() {
        ListScreen l = new ListScreen();
        l.addItem("Loading...");
        App.pushScreen(l);

        Util.sleep(5000);

        System.out.println("'"+email+"', '"+password+"'");
        App.replaceScreen(new AuthCodeScreen());
    }

    private void runSubmitTotp() {
        ListScreen l = new ListScreen();
        l.addItem("Loading...");
        App.pushScreen(l);

        Util.sleep(5000);

        App.loginCompleted("Mzk4ODc4NzI3OTM4ODk5OTc3.AAAAAA.AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }
}