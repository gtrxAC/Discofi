package fi.gtrxac.discord.model;

import cc.nnproject.json.*;

public class Channel {
    // https://docs.discord.food/resources/channel#channel-type
    public static final int TYPE_GUILD_TEXT = 0;
    public static final int TYPE_GUILD_VOICE = 2;
    public static final int TYPE_GUILD_CATEGORY = 4;
    public static final int TYPE_GUILD_NEWS = 5;
    public static final int TYPE_GUILD_FORUM = 15;
    public static final int TYPE_GUILD_MEDIA = 16;

    public String id;
    public String name;
    public int type;

    public Channel(JSONObject data) {
        id = data.getString("id");
        type = data.getInt("type", 0);
        name = data.getString("name");

        if (type != TYPE_GUILD_VOICE) name = "#" + name;
    }
}