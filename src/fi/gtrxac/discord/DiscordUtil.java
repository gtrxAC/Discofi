package fi.gtrxac.discord;

import java.util.*;
import util.*;

public class DiscordUtil {
    // private static final String BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/+";

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
        // int b64idChunkLength = (b64idLength + 3)/4*4;

        // for (int i = 0; i < b64idChunkLength; i += 4) {
        //     int first6 = BASE64_ALPHABET.indexOf(b64id.charAt(i));
        //     int second6 = BASE64_ALPHABET.indexOf(b64id.charAt(i + 1));
        //     int third6 = 0;
        //     int fourth6 = 0;

        //     int first8 = (first6 << 2) | (second6 >> 4);
        //     id.append(first8 - 48);  // <- note! not a general-purpose base64 decoder! this only works for b64-encoded numeric IDs

        //     if (i + 2 < b64idLength) {
        //         third6 = BASE64_ALPHABET.indexOf(b64id.charAt(i + 2));
        //         int second8 = ((second6 & 0xF) << 4) | (third6 >> 2);
        //         id.append(second8 - 48);
        //     }
        //     if (i + 3 < b64idLength) {
        //         fourth6 = BASE64_ALPHABET.indexOf(b64id.charAt(i + 3));
        //         int third8 = ((third6 & 0x3) << 6) | fourth6;
        //         id.append(third8 - 48);
        //     }
        // }

        byte[] id = Base64.decodeFast(b64id);

        try {
            return Long.toString(Long.parseLong(id.toString()));
        }
        catch (Exception e) {
            throw new DiscordException(
                "Token is written incorrectly",
                "Failed to decode ID: " + e.toString(),
                getInvalidTokenTips());
        }
    }
}