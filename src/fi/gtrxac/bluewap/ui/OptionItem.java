package fi.gtrxac.bluewap.ui;

import fi.gtrxac.bluewap.*;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;

/**
 * Item shown in OptionsScreen.
 */
public class OptionItem extends Item {
    public String text;
    private String displayText;
    public Command command;

    public OptionItem(Command command) {
        super(true);
        this.command = command;
        this.text = command.getLabel();
    }

    public void draw(Graphics g, ListScreen screen, int width, boolean highlighted) {
        if (highlighted) {
            g.setColor(Colors.selOptionItemBackground);
            g.fillRect(0, 0, width, height);

            drawHighlight(g, 0, 0, width, height);

            g.setColor(Colors.selOptionItemText);
        } else {
            g.setColor(Colors.optionItemText);
        }

        g.setFont(Fonts.plain);
        g.drawString(displayText, Fonts.height/3, Fonts.height/3, 0);
    }

    public void recalc(int width) {
        height = Fonts.height + Fonts.height/3*2;

        int textWidth = width - Fonts.height/3 - Fonts.height - Fonts.height/3;
        displayText = Util.stringToWidth(text, Fonts.plain, textWidth);
    }
}