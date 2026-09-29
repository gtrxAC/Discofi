package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class MessageItem extends Item {
    public Message message;
    private String[] textLines;

    private static int headerHeight;

    public MessageItem(Message m) {
        super(true);
        this.message = m;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        if (highlighted) {
            g.setColor(0xDDDDDD);

            int pad = screen.itemPadding;
            g.fillRect(
                -pad,
                -pad/2,
                width + pad*2,
                height + pad/2*2);
        }

        g.setFont(Fonts.bold);
        g.setColor(0x000000);
        g.drawString(message.author.name, 0, 0, 0);

        g.setFont(Fonts.plain);
        g.setColor(highlighted ? 0x000000 : 0x111111);
        int y = headerHeight;

        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], 0, y, 0);
            y += Fonts.height;
        }
    }

    public void recalc(int width) {
        textLines = Util.wordWrap(message.content, width, Fonts.plain);
        headerHeight = Fonts.boldHeight + Fonts.boldHeight/7;

        height = Fonts.height*textLines.length + headerHeight;
    }
}