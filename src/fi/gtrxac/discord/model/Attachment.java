package fi.gtrxac.discord.model;

import cc.nnproject.json.JSONObject;

/**
 * Created by Leap on 4.10.2026.
 */

public class Attachment {
    private static final String[] VIDEO_EXTENSIONS = {
        ".mp4", ".3gp", ".webm", ".mov", ".m4v", ".mkv", ".avi", ".gifv"
    };

    // requesting a proxy-scaled preview wider than this is a waste of data
    private static final int MAX_PREVIEW_WIDTH = 512;

    public String id;
    public String filename;
    public String contentType;
    public int size;

    public int width;
    public int height;

    public String url;
    public String proxyUrl;

    // images and videos can be previewed (videos as their first frame)
    public boolean previewable;
    public boolean isVideo;

    public Attachment(JSONObject data) {
        this(data, false);
    }

    public Attachment(JSONObject data, boolean isEmbed) {
        width = data.getInt("width", 0);
        height = data.getInt("height", 0);
        contentType = data.getString("content_type", null);

        url = data.getString("url", null);
        proxyUrl = data.getString("proxy_url", null);

        if (!isEmbed) {
            id = data.getString("id", null);
            filename = data.getString("filename", "attachment");
            size = data.getInt("size", 0);
        }

        isVideo = (isContentType("video/"))
                || (filename != null && endsWithAny(filename.toLowerCase(), VIDEO_EXTENSIONS));

        // Anything with dimensions can be rendered by the media proxy; images
        // without dimensions (very rare) can still be fetched as-is.
        previewable = (proxyUrl != null) && (width > 0 || isContentType("image/"));
    }

    private boolean isContentType(String check) {
        return contentType != null && contentType.startsWith(check);
    }

    public String getPreviewUrl(int maxWidth) {
        if (!previewable) return null;

        int width = maxWidth > 0 ? maxWidth : 512;
        if (width > MAX_PREVIEW_WIDTH) width = MAX_PREVIEW_WIDTH;
        return appendQuery(proxyUrl, "format=jpeg&width=" + width);
    }

    private static String appendQuery(String url, String query) {
        if (url == null) return null;
        return url + (url.indexOf('?') == -1 ? "?" : "&") + query;
    }

    private static boolean endsWithAny(String name, String[] extensions) {
        for (int i = 0; i < extensions.length; i++) {
            if (name.endsWith(extensions[i])) return true;
        }
        return false;
    }

    public static String formatSize(int size) {
        if (size >= 1000000) return (size/1000000) + " MB";
        if (size >= 1000) return (size/1000) + " kB";
        return size + " bytes";
    }
}
