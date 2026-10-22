package fi.gtrxac.discord.model;

import fi.gtrxac.discord.DiscordUtil;
import cc.nnproject.json.*;
import java.util.*;

public class Message {
    public static final int TYPE_ADDED = 1;  // user added another user to group DM
    public static final int TYPE_REMOVED = 2;  // user left (or removed someone) from group DM
    public static final int TYPE_CALL = 3;
    public static final int TYPE_CHANNEL_NAME_CHANGE = 4;  // changed name of group DM
    public static final int TYPE_CHANNEL_ICON_CHANGE = 5;
    public static final int TYPE_PINNED = 6;
    public static final int TYPE_JOINED = 7;  // user joined server
    public static final int TYPE_BOOSTED = 8;  // user boosted server
    public static final int TYPE_BOOSTED_LEVEL_1 = 9; 
    public static final int TYPE_BOOSTED_LEVEL_2 = 10;
    public static final int TYPE_BOOSTED_LEVEL_3 = 11;

    public String id;
    public User author;
    public String content;
    public boolean showAuthor;
    public String timestamp;
    public Vector attachments;

    public boolean isStatus;
    public boolean isForwarded;
    public boolean isReply;
    public User replyAuthor;
    public String replyContent;

    public Message(JSONObject data) {
        id = data.getString("id");
        author = new User(data.getObject("author"));
        timestamp = DiscordUtil.formatTimestamp(id, false);

        JSONObject contentObject = getContentObject(data);

        int type = contentObject.getInt("type", 0);
        isStatus = (type >= TYPE_ADDED && type <= TYPE_BOOSTED_LEVEL_3);

        if (isStatus) {
            content = parseStatusMessage(contentObject, type);
        }
        else {
            content = contentObject.getString("content", "");
            parseReply(contentObject);
            parseAttachments(contentObject);
        }
    }

    /**
     * Get JSON object to use for filling out message content and related fields.
     * For forwarded messages, this is an inner object inside the message object.
     */
    private JSONObject getContentObject(JSONObject data) {
        JSONObject result = data;
        isForwarded = false;

        try {
            result = data.getArray("message_snapshots").getObject(0).getObject("message");
            isForwarded = true;
        }
        catch (Exception e) {}

        return result;
    }

    private void parseReply(JSONObject data) {
        try {
            data.getObject("message_reference");
            isReply = true;
        }
        catch (Exception e) {}

        try {
            Message replyMessage = new Message(data.getObject("referenced_message"));
            replyAuthor = replyMessage.author;
            replyContent = replyMessage.content;

            if (replyContent.length() == 0) {
                replyContent = "(no content)";
            }
            else {
                replyContent = replyContent.replace('\n', ' ');
            }
        }
        catch (Exception e) {}
    }

    private void parseAttachments(JSONObject data) {
        JSONArray attachArray = data.getArray("attachments", null);
        if (attachArray == null) return;

        for (int i = 0; i < attachArray.size(); i++) {
            try {
                JSONObject attachJson = attachArray.getObject(i);
                Attachment attachment = new Attachment(attachJson);

                if (attachments == null) {
                    attachments = new Vector();
                }
                attachments.addElement(attachment);
            }
            catch (Exception e) {}
        }
    }

    private String parseStatusMessage(JSONObject data, int type) {
        // Status message -> determine content by message type
        String target = "(unknown)";
        String targetID = null;

        try {
            JSONObject targetData = data.getArray("mentions").getObject(0);
            User targetUser = new User(targetData);
            target = targetUser.name;
            targetID = targetUser.id;
        }
        catch (Exception e) {}

        switch (type) {
            case TYPE_ADDED: {
                return "added " + target + " to the group";
            }
            case TYPE_REMOVED: {
                if (author.id.equals(targetID)) {
                    return "left the group";
                }
                return "removed " + target + " from the group";
            }
            case TYPE_CALL: {
                return "started a call";
            }
            case TYPE_CHANNEL_NAME_CHANGE: {
                return "changed the group name";
            }
            case TYPE_CHANNEL_ICON_CHANGE: {
                return "changed the group icon";
            }
            case TYPE_PINNED: {
                return "pinned a message";
            }
            case TYPE_JOINED: {
                return "joined the server";
            }
            case TYPE_BOOSTED: {
                return "boosted the server";
            }
            default: {
                return "boosted the server to level " + (type - TYPE_BOOSTED);
            }
        }
    }

    /**
     * Determine whether or not the author/timestamp row should be shown for
     * this message.
     *
     * @param above        The message shown above this message.
     * @param clusterStart The ID of the top-most message in this message cluster.
     * @return true if author should be shown, false if messages are "merged"
     */
    public boolean shouldShowAuthor(Message above, String clusterStart) {
        // Different authors -> true
        if (!above.author.id.equals(author.id)) return true;

        // This message is a reply -> true
        if (replyAuthor != null) return true;

        // This message only consists of an attachment -> true
        if (attachments != null && content.length() == 0) return true;

        // This message or above message is a status message -> true
        // if (isStatus || above.isStatus) return true;

        // Finally, check if message was sent more than 7 minutes after the first message of the cluster
        long thisMsgTime = Long.parseLong(id) >> 22;
        long firstMsgTime = Long.parseLong(clusterStart) >> 22;
        return (thisMsgTime - firstMsgTime > 7*60*1000);
    }
}