package fi.gtrxac.discord;

import fi.gtrxac.discord.model.*;
import cc.nnproject.json.*;

public class MessageParser {
    private JSONObject inputContentObject;
    private String inputContent;
    private int readOffset;
    private StringBuffer outputContent; 

    public MessageParser(JSONObject contentObject) {
        inputContentObject = contentObject;
        inputContent = contentObject.getString("content", null);
        outputContent = new StringBuffer(); 

        if (inputContent == null) {
            inputContent = "";
        }
    }

    public String parse() {
        while (readOffset < inputContent.length()) {
            char currentChar = inputContent.charAt(readOffset);

            if (currentChar == '<' && parseMentionAndMoveCursor()) {
                continue;
            }
            else {
                outputContent.append(currentChar);
                readOffset++;
            }
        }

        return outputContent.toString();
    }

    private boolean parseMentionAndMoveCursor() {
        if (parseMention()) {
            int tagEndIndex = inputContent.indexOf(">", readOffset);
            readOffset = tagEndIndex + 1;
            return true;
        }
        return false;
    }

    /**
     * Try to parse a mention tag for channels, users, etc. that is inside < >.
     * Return true on success.
     */
    private boolean parseMention() {
        int tagEndIndex = inputContent.indexOf(">", readOffset);
        if (tagEndIndex == -1) return false;

        // Everything that's inside the < >
        String tagContent = inputContent.substring(readOffset + 1, tagEndIndex);
        if (tagContent.length() == 0) return false;

        if (tagContent.charAt(0) == '@') {
            // User mention
            String userID = tagContent.substring(1);
            return parseUserMention(userID);
        }
        else if (tagContent.charAt(0) == '#') {
            String channelID = tagContent.substring(1);
            return parseChannelMention(channelID);
        }

        return false;
    }

    /**
     * Try to parse user mention.
     * On success, appends the human-readable username form of the mention to
     * the output, and returns true.
     * On failure, return false.
     */
    private boolean parseUserMention(String userID) {
        // From the mentions object, find the user that this ID belongs to
        try {
            JSONArray mentions = inputContentObject.getArray("mentions");

            for (int i = 0; i < mentions.size(); i++) {
                JSONObject mention = mentions.getObject(i);
                String mentionID = mention.getString("id", null);

                if (userID.equals(mentionID)) {
                    String mentionName = mention.getString("username", "unknown");

                    outputContent.append("@").append(mentionName);
                    return true;
                }
            }
        }
        catch (JSONException e) {}

        if (!isValidID(userID)) return false;
        
        outputContent.append("@unknown");
        return true;
    }

    private boolean parseChannelMention(String channelID) {
        Account acc = Accounts.getActive();
        Channel channel = acc.channelCache.getChannelByID(null, channelID);

        if (channel != null) {
            outputContent.append(channel.name);
            return true;
        }

        if (!isValidID(channelID)) return false;

        outputContent.append("#unknown");
        return true;
    }

    private boolean isValidID(String id) {
        if (id.length() < 17) return false;

        try {
            Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}