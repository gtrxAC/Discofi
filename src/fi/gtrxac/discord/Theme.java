package fi.gtrxac.discord;

import fi.gtrxac.bluewap.ui.*;

/**
 * Adds Microcord-specific color entries which are used
 * in addition to the BlueWAP UI framework's colors.
 */
public class Theme extends Colors {
    /**
     * ChannelCategoryItem
     */
    public static int channelCategoryText;
    public static int channelCategoryBackground;

    /**
     * LoadingItem
     */
    public static int loadingItemText;

    /**
     * MessageItem
     */
    public static int messageBackgroundSel;
    public static int username;
    public static int timestamp;
    public static int messageText;
    public static int messageTextSel;
    public static int replyConnectorLines;
    public static int replyUsername;
    public static int replyContent;
    public static int avatarPlaceholder;

    public static void loadLightTheme() {
        Colors.load();

        channelCategoryText = 0x111111;
        channelCategoryBackground = 0xE8E8E8;
        loadingItemText = 0x333333;
        messageBackgroundSel = 0xAADDFF;
        username = 0x000000;
        timestamp = 0x666666;
        messageText = 0x111111;
        messageTextSel = 0x000000;
        replyConnectorLines = 0x888888;
        replyUsername = 0x000000;
        replyContent = 0x666666;
        avatarPlaceholder = 0x666666;
    }

    public static void loadDarkTheme() {
        listScreenBackground = 0xFF0000;
        listScreenScrollbar = 0xCCCCCC;
        listScreenScrollbarSel = 0x888888;
        bannerBackground = 0xEEEEEE;
        bannerOutline = 0xAAAAAA;
        bannerText = 0x000000;
        tabBarBackground = 0xBBBBBB;
        tabBackground = 0xDDDDDD;
        tabOutlineColor = 0x9A9A9A;
        tabText = 0x111111;
        tabTextSel = 0x000000;
        stringItemText = 0x111111;
        titleBarBackground = 0xDDDDDD;
        titleBarOutline = 0x9B9B9B;
        titleBarText = 0x111111;
        softkeyBarBackground = 0xEEEEEE;
        softkeyBackground = 0xDDDDDD;
        softkeyBackgroundSel = 0xBBBBBB;
        softkeyOutline = 0x9B9B9B;
        softkeyOutlineSel = 0x838383;
        softkeyText = 0x111111;
        softkeyTextSel = 0x000000;
        highlightOutline = 0x5599FF;
        spacerItem = 0x888888;
        buttonBackground = 0xDDDDDD;
        buttonBackgroundSel = 0xAADDFF;
        buttonOutline = 0x9A9A9A;
        buttonText = 0x111111;
        buttonTextSel = 0x000000;
        linkBackgroundSel = 0xEEF8FF;
        linkText = 0x3355CC;
        linkTextSel = 0x2244AA;
        listItemBackgroundSel = 0xAADDFF;
        listItemText = 0x111111;
        listItemTextSel = 0x000000;
        listItemArrow = 0x444444;
        optionItemBackgroundSel = 0xAADDFF;
        optionItemText = 0x111111;
        optionItemTextSel = 0x000000;
        radioButtonItemBackgroundSel = 0xEEF8FF;
        radioButtonItemText = 0x111111;
        radioButtonItemTextSel = 0x000000;
        radioButtonOutline = 0x9A9A9A;
        radioButtonFill = 0xDDDDDD;
        radioButtonDot = 0x111111;
        textFieldBackground = 0x999999;
        textFieldBackgroundSel = 0xEEF8FF;
        textFieldText = 0x222222;
        textFieldTextSel = 0x000000;

        channelCategoryText = 0xFF00000;
        channelCategoryBackground = 0xE8E8E8;
        loadingItemText = 0x333333;
        messageBackgroundSel = 0xAADDFF;
        username = 0x000000;
        timestamp = 0x666666;
        messageText = 0x111111;
        messageTextSel = 0x000000;
        replyConnectorLines = 0x888888;
        replyUsername = 0x000000;
        replyContent = 0x666666;
        avatarPlaceholder = 0x666666;
    }

    public static void load() {
        loadDarkTheme();
    }
}