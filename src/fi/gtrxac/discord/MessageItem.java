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
    private int timestampX;
    private int timestampY;

    private static Font timestampFont;
    private static int leftMargin;
    private static int avatarSize;
    private static int contentX;
    private static int headerHeight;
    private static int replyAuthorWidth;

    public MessageItem(Message m) {
        super(true);
        this.message = m;
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        int pad = screen.itemPadding;

        // Highlight background
        if (highlighted) {
            g.setColor(0xAADDFF);
            g.fillRect(
                0,
                -pad/2,
                width,
                height + pad/2*2);
        }

        int textY = 0;

        // Author line
        if (message.showAuthor) {
            // Reply
            if (message.replyAuthor != null) {
                drawReply(g);
                textY += Fonts.height;
            }

            // Avatar placeholder
            g.setColor(0x666666);
            g.fillArc(leftMargin, textY + pad/2, avatarSize, avatarSize, 0, 360);

            // Username
            g.setFont(Fonts.bold);
            g.setColor(0x000000);
            g.drawString(displayedAuthor, contentX, textY, 0);

            // Timestamp
            g.setFont(timestampFont);
            g.setColor(0x666666);
            g.drawString(message.timestamp, timestampX, timestampY, 0);

            textY += headerHeight;
        }

        // Message content
        g.setFont(Fonts.plain);
        g.setColor(highlighted ? 0x000000 : 0x111111);

        for (int i = 0; i < textLines.length; i++) {
            g.drawString(textLines[i], contentX, textY, 0);
            textY += Fonts.height;
        }
    }

    private void drawReply(Graphics g) {
        // Avatar placeholder
        g.setColor(0x666666);
        g.fillArc(leftMargin, 0, Fonts.height, Fonts.height, 0, 360);

        int x = Fonts.height + Fonts.height/2;
        
        // Username
        g.setColor(0x333333);
        g.setFont(Fonts.bold);
        g.drawString(message.replyAuthor.name, x, 0, 0);

        x += replyAuthorWidth;

        // Message content
        g.setFont(Fonts.plain);
        g.drawString(message.content, x, 0, 0);
    }

    public void recalc(int width) {
        timestampFont = Fonts.get(-1, false, false, false);
        timestampY = Fonts.bold.getBaselinePosition() - timestampFont.getBaselinePosition();

        leftMargin = Fonts.height/3;
        avatarSize = Fonts.height*5/4;
        int avatarRightMargin = Fonts.height/3;
        contentX = avatarSize + leftMargin + avatarRightMargin;
        int contentWidth = width - contentX;

        textLines = Util.wordWrap(message.content, contentWidth, Fonts.plain);
        height = Fonts.height*textLines.length;

        if (message.showAuthor) {
            headerHeight = Fonts.boldHeight + Fonts.boldHeight/7;
            height += headerHeight;

            if (message.replyAuthor != null) {
                height += Fonts.height;
                timestampY += Fonts.height;
            }
        }

        int timestampPadding = Fonts.height/2;
        int timestampWidth = timestampFont.stringWidth(message.timestamp) + timestampPadding;
        int authorAvailableWidth = contentWidth - timestampWidth;
        displayedAuthor = Util.stringToWidth(message.author.name, Fonts.bold, authorAvailableWidth);

        int authorWidth = Fonts.bold.stringWidth(displayedAuthor);
        timestampX = contentX + authorWidth + timestampPadding;

        replyAuthorWidth = Fonts.bold.stringWidth(message.replyAuthor.name) + Fonts.height/2;
    }
}