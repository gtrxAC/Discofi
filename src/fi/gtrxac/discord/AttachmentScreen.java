package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import fi.gtrxac.bluewap.Util;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class AttachmentScreen extends Screen implements CommandListener {
    public static final int CMD_BACK = 0;
    public static final int CMD_ZOOM_IN = 1;
    public static final int CMD_ZOOM_OUT = 2;

    private static final Command zoomInCommand =
        new Command("Zoom in", Command.SCREEN, CMD_ZOOM_IN);

    private static final Command zoomOutCommand =
        new Command("Zoom out", Command.SCREEN, CMD_ZOOM_OUT);

    private Attachment attachment;
    private Image attachmentImage;
    private int offsetX;
    private int offsetY;
    private boolean isLoading;
    private int zoomLevel;

    public AttachmentScreen(Attachment att) {
        super(0);
        setTitle(att.filename);

        setCommandListener(this);
        addCommand(new Command("Back", Command.BACK, CMD_BACK));
        addCommand(zoomInCommand);

        loadImage();
    }

    private void loadImage() {
        isLoading = true;

        // HTTPThread h = new HTTPThread(HTTPThread.ACTION_FETCH_ATTACHMENT);
        // ...
        // h.start();

        App.repaint();
    }

    // Callback
    public void imageLoaded(Image image) {
        attachmentImage = image;
        isLoading = false;
        App.repaint();
    }

    public void draw(Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (attachmentImage != null) {
            g.drawImage(
                attachmentImage,
                getWidth()/2 + offsetX,
                getHeight()/2 + offsetY,
                Graphics.HCENTER | Graphics.VCENTER);
        }

        if (isLoading) {
            int textWidth = Fonts.plain.stringWidth("Loading...");
            int textX = getWidth()/2 - textWidth/2;
            int textY = getHeight()/2 - Fonts.height/2;

            g.fillRect(textX, textY, textWidth, Fonts.height);

            g.setColor(0xFFFFFF);
            g.drawString("Loading...", textX, textY, 0);
        }
    }

    public void recalc() {
        clampViewOffset();
    }

    public void keyEvent(int keyCode, int gameAction) {
        if (attachmentImage == null) return;

        int increment = Math.min(getWidth(), getHeight())/10;

        switch (gameAction) {
            case Canvas.UP: {
                offsetY -= increment;
                break;
            }
            case Canvas.DOWN: {
                offsetY += increment;
                break;
            }
            case Canvas.LEFT: {
                offsetX -= increment;
                break;
            }
            case Canvas.RIGHT: {
                offsetX += increment;
                break;
            }
            case Canvas.FIRE: {
                zoomIn();
                break;
            }
        }

        clampViewOffset();
    }

    private void clampViewOffset() {
        // ...
    }

    private void zoomIn() {
        zoomLevel++;
        addCommand(zoomOutCommand);

        if (zoomLevel >= 5) {
            zoomLevel = 5;
            removeCommand(zoomInCommand);
        }
        else {
            addCommand(zoomInCommand);
        }
        
        loadImage();
    }

    private void zoomOut() {
        zoomLevel--;
        addCommand(zoomInCommand);
        
        if (zoomLevel <= 1) {
            zoomLevel = 1;
            removeCommand(zoomOutCommand);
        }
        else {
            addCommand(zoomOutCommand);
        }

        loadImage();
    }

    public void commandAction(Command c, Displayable d) {
        switch (c.getPriority()) {
            case CMD_BACK: {
                App.popScreen();
                break;
            }
            case CMD_ZOOM_IN: {
                zoomIn();
                break;
            }
            case CMD_ZOOM_OUT: {
                zoomOut();
                break;
            }
        }
    }
}