package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Item that shows a loading animation.
 * This is meant to be the only item shown on the screen at a time.
 */
public class LoadingItem extends Item {
    public LoadingItem() {
        super(false);
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        int y = screen.getHeight();
        g.setColor(0x000000);
        g.setFont(Fonts.plain);
        g.drawString("Loading...", 0, y/2, 0);
    }

    public void recalc(int width) {
        height = 0;
    }
}