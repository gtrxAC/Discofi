package fi.gtrxac.discord.model;

import cc.nnproject.json.*;

public class Channel {
    public String id;
    public String name;
    public boolean isForum;

    public Channel(JSONObject data) {
        id = data.getString("id");
        name = "#" + data.getString("name");

        int type = data.getInt("type", 0);
        isForum = (type == 15 || type == 16);
    }
}