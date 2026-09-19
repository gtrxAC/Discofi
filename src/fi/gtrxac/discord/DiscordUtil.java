package fi.gtrxac.discord;

import java.util.*;
import util.*;

public class DiscordUtil {
    private static Vector getInvalidTokenTips() {
        Vector result = new Vector();
        result.addElement("use password login instead of your token");
        result.addElement("if you want to use your token, make sure you have retrieved it correctly");
        result.addElement("to avoid typos, import your token from a text file or insert it into the JAR (see login screen for details)");
        return result;
    }

    public static String getUserIDFromToken(String token) throws DiscordException {
        // StringBuffer id = new StringBuffer();
        int b64idLength = token.indexOf('.');

        if (b64idLength < 20) {
            throw new DiscordException(
                "Token is written incorrectly",
                "Token's ID segment is missing or too short",
                getInvalidTokenTips());
        }

        String b64id = token.substring(0, b64idLength);
        byte[] idBytes = Base64.decodeFast(b64id);
        String id = new String(idBytes);

        try {
            return Long.toString(Long.parseLong(id));
        }
        catch (Exception e) {
            throw new DiscordException(
                "Token is written incorrectly",
                "Failed to decode ID: " + e.toString(),
                getInvalidTokenTips());
        }
    }
}