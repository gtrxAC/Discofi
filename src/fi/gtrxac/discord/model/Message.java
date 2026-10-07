package fi.gtrxac.discord.model;

import fi.gtrxac.discord.DiscordUtil;
import cc.nnproject.json.*;

public class Message {
    public String id;
    public User author;
    public String content;
    public boolean showAuthor;
    public String timestamp;

    public User replyAuthor;
    public String replyContent;

    public Message(JSONObject data) {
        id = data.getString("id");
        author = new User(data.getObject("author"));
        content = data.getString("content", "");
        timestamp = DiscordUtil.formatTimestamp(id, false);

        try {
            JSONObject replyMessage = data.getObject("referenced_message");

            replyAuthor = new User(replyMessage.getObject("author"));
            replyContent = replyMessage.getString("content", null);

            if (replyContent == null || replyContent.length() == 0) {
                replyContent = "(no content)";
            }
        }
        catch (Exception e) {}
    }

    public boolean shouldShowAuthor(Message above, String clusterStart) {
        // Different authors -> true
        if (!above.author.id.equals(author.id)) return true;

        // This message is a reply -> true
        if (replyAuthor != null) return true;

        // This message only consists of an attachment -> true
        // if ((imageAttachments != null || fileAttachments != null) && content.length() == 0) return true;

        // This message or above message is a status message -> true
        // if (isStatus || above.isStatus) return true;

        // Finally, check if message was sent more than 7 minutes after the first message of the cluster
        long thisMsgTime = Long.parseLong(id) >> 22;
        long firstMsgTime = Long.parseLong(clusterStart) >> 22;
        return (thisMsgTime - firstMsgTime > 7*60*1000);
    }
}