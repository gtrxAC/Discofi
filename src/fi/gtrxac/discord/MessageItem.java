package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import tube42.lib.imagelib.ImageUtils;

public class MessageItem extends Item {
    public Message message;
    private String[] textLines;
    private String displayedAuthor;
    private int timestampX;
    private int timestampY;

    private int replyAuthorWidth;
    private String displayedReplyContent;
    private boolean replyImageHighlighted;
    private Image replyImage;

    private static Font timestampFont;
    private static int leftMargin;
    private static int avatarSize;
    private static int contentX;
    private static int contentWidth;
    private static int headerHeight;
    private static int replyHeight;

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
            // Lines connecting reply to message
            int linesBeginX = contentX/2;
            int linesBeginY = replyHeight/2;
            int horizontalLineEndX = contentX*4/5;
            int verticalLineEndY = replyHeight*4/5;

            g.setColor(0x888888);
            g.drawLine(linesBeginX, linesBeginY, horizontalLineEndX, linesBeginY);
            g.drawLine(linesBeginX, linesBeginY, linesBeginX, verticalLineEndY);

            // Reply
            if (message.replyAuthor != null) {
                updateReplyImage(highlighted);
                g.drawImage(replyImage, contentX, Fonts.height/5, 0);
                textY += replyHeight;
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

    private void drawReply(Graphics g, int x, int y) {
        // Avatar placeholder
        g.setColor(0x666666);
        g.fillArc(x, y, Fonts.height, Fonts.height, 0, 360);

        x += Fonts.height + Fonts.height/3;
        
        // Username
        g.setColor(0x000000);
        g.setFont(Fonts.bold);
        g.drawString(message.replyAuthor.name, x, y, 0);

        x += replyAuthorWidth;

        // Message content
        g.setColor(0x666666);
        g.setFont(Fonts.plain);
        g.drawString(displayedReplyContent, x, y, 0);
    }

    private void updateReplyImage(boolean highlighted) {
        if (replyImage != null && replyImageHighlighted == highlighted) return;

        replyImage = null;
        replyImage = Image.createImage(contentWidth*4/3, Fonts.height);

        Graphics g = replyImage.getGraphics();

        if (highlighted) {
            g.setColor(0xAADDFF);
            g.fillRect(0, 0, contentWidth*4/3, Fonts.height);
        }

        drawReply(g, 0, 0);

        replyImage = ImageUtils.resize(replyImage, contentWidth, Fonts.height*3/4, true, true);
        replyImageHighlighted = highlighted;
    }

    public void recalc(int width) {
        leftMargin = Fonts.height/3;
        avatarSize = Fonts.height*5/4;
        int avatarRightMargin = Fonts.height/3;

        contentX = leftMargin + avatarSize + avatarRightMargin;
        contentWidth = width - contentX;

        textLines = Util.wordWrap(message.content, contentWidth, Fonts.plain);
        height = Fonts.height*textLines.length;

        if (!message.showAuthor) return;

        calcAuthorHeight();
        calcTimestampAndAuthor();

        if (message.replyAuthor == null) return;

        calcReplyHeight();
        calcReplyContent();
    }

    private void calcAuthorHeight() {
        headerHeight = Fonts.boldHeight + Fonts.boldHeight/7;
        height += headerHeight;
    }

    private void calcTimestampAndAuthor() {
        timestampFont = Fonts.get(-1, false, false, false);
        timestampY = Fonts.bold.getBaselinePosition() - timestampFont.getBaselinePosition();

        int timestampPadding = Fonts.height/2;
        int timestampWidth = timestampPadding + timestampFont.stringWidth(message.timestamp);
        int authorAvailableWidth = contentWidth - timestampWidth;
        
        displayedAuthor = Util.stringToWidth(
            message.author.name, Fonts.bold, authorAvailableWidth);

        int authorWidth = Fonts.bold.stringWidth(displayedAuthor);
        
        timestampX = contentX + authorWidth + timestampPadding;
    }

    private void calcReplyHeight() {
        int replyPadding = Fonts.height/5;
        int replyImageHeight = Fonts.height*3/4;

        replyHeight = replyImageHeight + replyPadding*2;
        height += replyHeight;
        timestampY += replyHeight;
    }

    private void calcReplyContent() {
        int replyAuthorRightMargin = Fonts.height/3;

        replyAuthorWidth = Fonts.bold.stringWidth(message.replyAuthor.name)
            + replyAuthorRightMargin;
    
        // for when we add reply direct drawing:

        // int replyContentAvailableWidth =
        //     contentWidth - Fonts.height - Fonts.height/3 - replyAuthorWidth;

        // displayedReplyContent = Util.stringToWidth(
        //     message.replyContent, Fonts.plain, replyContentAvailableWidth);

        displayedReplyContent = message.replyContent;
    }
}