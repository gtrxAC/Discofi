package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class MessageItem extends StringItem {
    public Message message;

    private static int headerHeight;

    public MessageItem(Message m) {
        super(m.content);
        this.message = m;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        g.setFont(Fonts.bold);
        g.setColor(0x000000);
        g.drawString(message.author.name, 0, 0, 0);

        g.translate(0, headerHeight);

        super.draw(g, screen, width, highlighted);

        g.translate(0, -headerHeight);
    }

    public void recalc(int width) {
        super.recalc(width);
        headerHeight = Fonts.boldHeight + Fonts.boldHeight/5;
        height += headerHeight;
    }
}