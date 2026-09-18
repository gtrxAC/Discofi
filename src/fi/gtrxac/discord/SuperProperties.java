package fi.gtrxac.discord;

import cc.nnproject.json.JSONObject;
import util.Base64;
import java.util.*;

/**
 * Created by gtrx on 1.9.2026.
 */

public class SuperProperties {
    private static final Random random = new Random();
    private static final String clientLaunchID = generateRandomUUID();

    private static String heartbeatSessionID;
    private static long heartbeatSessionIDExpiry;

    private static String generateRandomUUID() {
        byte[] bytes = new byte[16];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (random.nextInt() & 0xff);
        }

        bytes[6] = (byte) ((bytes[6] & 0x0f) | 0x40);
        bytes[8] = (byte) ((bytes[8] & 0x3f) | 0x80);

        StringBuffer uuid = new StringBuffer(36);
        for (int i = 0; i < bytes.length; i++) {
            if (i == 4 || i == 6 || i == 8 || i == 10) {
                uuid.append('-');
            }

            int value = bytes[i] & 0xff;
            if (value < 0x10) {
                uuid.append('0');
            }
            uuid.append(Integer.toHexString(value));
        }
        return uuid.toString();
    }

    static {
        for (int i=0;i< 100;i++) System.out.println(generateRandomUUID());
    }

    private static String getHeartbeatSessionID() {
        if (heartbeatSessionID == null || System.currentTimeMillis() >= heartbeatSessionIDExpiry) {
            heartbeatSessionID = generateRandomUUID();
            heartbeatSessionIDExpiry = System.currentTimeMillis() + 30*60*1000;
        }
        return heartbeatSessionID;
    }

    public static String get() {
        JSONObject propsJson = new JSONObject();

        propsJson.put("os", "Android");
        propsJson.put("browser", "Discord Android");
        propsJson.put("device", "a20e");
        propsJson.put("system_locale", "en-US");
        propsJson.put("has_client_mods", false);
        propsJson.put("client_version", "262.5 - rn");  // should match the user agent in HTTPConfig
        propsJson.put("release_channel", "alpha");
        propsJson.put("device_vendor_id", "17503929-a4b8-4490-87bf-0222adfdadc8");
        propsJson.put("design_id", 2);
        propsJson.put("browser_user_agent", "");
        propsJson.put("browser_version", "");
        propsJson.put("os_version", "34");
        propsJson.put("client_build_number", 3463);
        propsJson.put("client_event_source", (Object) null);
        propsJson.put("client_launch_id", clientLaunchID);
        propsJson.put("launch_signature", Long.toString(System.currentTimeMillis() * 1000000));
        propsJson.put("client_heartbeat_session_id", getHeartbeatSessionID());

        byte[] propsBytes = propsJson.build().getBytes();
        return Base64.encodeToString(propsBytes, false);
    }
}
