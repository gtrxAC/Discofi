package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class ChannelCategoryItem extends StringItem {
    private static int padding;

    public ChannelCategoryItem(String text) {
        super(text, Fonts.bold);
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        g.setColor(0xE8E8E8);
        g.fillRect(0, -screen.itemPadding, width, height + screen.itemPadding*2);

        g.translate(padding, padding);
        super.draw(g, screen, width - padding*2, highlighted);
        g.translate(-padding, -padding);
    }

    public void recalc(int width) {
        padding = Fonts.height/7;
        super.recalc(width - padding*2);
        height += padding*2;
    }
}