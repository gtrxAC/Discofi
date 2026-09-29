package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class MessageItem extends Item {
    public Message message;
    private String[] textLines;
    private String displayedAuthor;

    private static int avatarSize;
    private static int contentX;
    private static int headerHeight;

    public MessageItem(Message m) {
        super(true);
        this.message = m;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        int pad = screen.itemPadding;

        if (highlighted) {
            g.setColor(0xAADDFF);
            g.fillRect(
                -pad,
                -pad/2,
                width + pad,
                height + pad/2*2);
        }

        int textY = 0;

        if (message.showAuthor) {
            g.setColor(0x666666);
            g.fillArc(0, pad/2, avatarSize, avatarSize, 0, 360);

            g.setFont(Fonts.bold);
            g.setColor(0x000000);
            g.drawString(displayedAuthor, contentX, 0, 0);

            textY = headerHeight;
        }

        g.setFont(Fonts.plain);
        g.setColor(highlighted ? 0x000000 : 0x111111);

        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], contentX, textY, 0);
            textY += Fonts.height;
        }
    }

    public void recalc(int width) {
        avatarSize = Fonts.height*5/4;
        contentX = avatarSize + Fonts.height/3;
        int contentWidth = width - contentX;

        textLines = Util.wordWrap(message.content, contentWidth, Fonts.plain);
        height = Fonts.height*textLines.length;

        if (message.showAuthor) {
            headerHeight = Fonts.boldHeight + Fonts.boldHeight/7;
            height += headerHeight;
        }

        displayedAuthor = Util.stringToWidth(message.author.name, Fonts.bold, contentWidth);
    }
}