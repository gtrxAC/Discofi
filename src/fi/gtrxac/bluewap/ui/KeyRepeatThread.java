package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.*;

public class KeyRepeatThread extends Thread {
    public static volatile int activeKey;
    public static final KeyRepeatThread instance = new KeyRepeatThread();

    private KeyRepeatThread() {
        start();
    }
    
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    wait(0);
                }
            }
            catch (InterruptedException e) {}

            while (true) {
                long curr = System.currentTimeMillis();
                if (curr >= AppCanvas.instance.beginRepeatTime) break;
                Util.sleep((int) (AppCanvas.instance.beginRepeatTime - curr));
            }

            while (activeKey != 0) {
                try {
                    AppCanvas.instance.keyEvent(activeKey);
                    AppBase.repaint();
                    Thread.sleep(50);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}