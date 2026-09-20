package fi.gtrxac.discord;

import fi.gtrxac.bluewap.Util;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.Sprite;
import tube42.lib.imagelib.*;

/**
 * Item that shows a loading animation.
 * This is meant to be the only item shown on the screen at a time, and it draws on the center of the screen.
 */
public class LoadingItem extends Item implements Runnable {
    private int curFrame;
    private int animDirection;
    private int drawCount;

    private static int scale;
    private static int phoneOffset;
    private static int bubbleOffset;
    private static Image[] phoneFrames;
    private static Image[] bubbleFrames;

    public LoadingItem() {
        super(false);
        curFrame = 0;
        animDirection = 1;

        // Start loading icon animation
        new Thread(this).start();
    }

    private static void loadFrames() {
        Image sheet;
        phoneFrames = new Image[4];
        bubbleFrames = new Image[4];

        try {
            sheet = Image.createImage("/l.png");
        }
        catch (Exception e) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            try {
                phoneFrames[i] = Image.createImage(sheet, i*32, 0, 32, 48, Sprite.TRANS_NONE);
                bubbleFrames[i] = Image.createImage(sheet, 128 + i*31, 0, 31, 24, Sprite.TRANS_NONE);

                if (scale > 1) {
                    phoneFrames[i] = ImageUtils.resize(phoneFrames[i], 32*scale, 48*scale, false, false);
                    bubbleFrames[i] = ImageUtils.resize(bubbleFrames[i], 31*scale, 24*scale, false, false);
                }
            }
            catch (Exception e) {}
        }
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        // Draw current animation frame
        int halfContainerHeight = 24*scale + Fonts.height*3/4;
        int halfScreenHeight = screen.getHeight()/2;

        int curPhoneFrame = Math.min(curFrame, 3);

        g.drawImage(
            phoneFrames[curPhoneFrame],
            width/2 + phoneOffset,
            halfScreenHeight - halfContainerHeight,
            Graphics.LEFT | Graphics.TOP);

        int curBubbleFrame = curFrame - 4;

        if (curBubbleFrame >= 0) {
            g.drawImage(
                bubbleFrames[curBubbleFrame],
                width/2 + bubbleOffset,
                halfScreenHeight - halfContainerHeight,
                Graphics.LEFT | Graphics.TOP);
        }

        int y = screen.getHeight()/2;
        g.setColor(0x000000);
        g.setFont(Fonts.plain);
        g.drawString("Loading...", 0, y, 0);
    }

    public void recalc(int width) {
        height = 0;

        int newScale = width/270 + 1;

        if (phoneFrames == null || scale != newScale) {
            scale = newScale;
            loadFrames();
        }

        // -24 (half of full frame width to center it on screen)
        // -3 (offset to make it look more centered since the phone is more towards the right)
        // 14 (phone offset to make it align with the bubble)
        phoneOffset = (-3 + 14 - 24)*scale;
        bubbleOffset = (-3 - 24)*scale;
    }

    // Icon animation thread
    public void run() {
        while (true) {
            long paintTime = System.currentTimeMillis();
            AppCanvas.instance.repaint();
            AppCanvas.instance.serviceRepaints();
            paintTime = System.currentTimeMillis() - paintTime;

            // Sleep based on the frame number that was just drawn (first frame = 167 ms, last frame = 500 ms)
            int sleepTime;
            switch (curFrame) {
                case 0: sleepTime = 167; break;
                case 7: sleepTime = 500; break;
                default: sleepTime = 83; break;
            }
            Util.sleep(sleepTime - (int) paintTime);

            // Go to next animation frame.
            // If end reached, start going through frames in descending order.
            // If beginning reached, start going through frames in ascending order.
            curFrame += animDirection;
            if (curFrame == 7) animDirection = -1;
            if (curFrame == 0) animDirection = 1;
        }
    }
}