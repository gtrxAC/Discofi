package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import fi.gtrxac.bluewap.ui.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class MessageItem extends StringItem {
    public MessageItem(Message m) {
        super(m.author.name + ": " + m.content);
    }

    // public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
    // }

    // public void recalc(int width) {
    // }
}