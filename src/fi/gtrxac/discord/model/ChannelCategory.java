package fi.gtrxac.discord.model;

import java.util.*;
import cc.nnproject.json.*;

public class ChannelCategory {
    public String id;
    public String name;
    public Vector children;

    public ChannelCategory(JSONObject data) {
        this(data.getString("name"));
        this.id = data.getString("id");
    }

    public ChannelCategory(String name) {
        this.name = name;
        this.children = new Vector();
    }
}