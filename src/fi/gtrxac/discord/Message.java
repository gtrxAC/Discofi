package fi.gtrxac.discord;

import cc.nnproject.json.*;

public class Message {
    public String id;
    public User author;
    public String content;

    public Message(JSONObject data) {
        id = data.getString("id");
        author = new User(data.getObject("author"));
        content = data.getString("content", "");
    }
}