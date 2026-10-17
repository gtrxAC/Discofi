package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;

public class LabelItem extends StringItem {
    public LabelItem(String text) {
        super(text, Fonts.get(-1, false, false, false));
        color = Theme.labelItem;
    }
}