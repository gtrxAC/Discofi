package fi.gtrxac.discord;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 * Experimental TLS 1.2 client for CLDC 1.0.
 *
 * PHASE 1 ONLY:
 *   - Sends ClientHello
 *   - Parses ServerHello
 *   - Skips Certificate
 *   - Parses ECDHE ServerKeyExchange
 *   - Extracts server X25519 public key
 *   - Stops at ServerHelloDone
 *
 * No cryptography is performed yet.
 */
public final class TinyTLS {
        public static String debugtext = "";

        private static void debuglog(String s) {
                debugtext += s + "\n";
        }

    private static final int TLS_HANDSHAKE = 22;

    private static final int HS_SERVER_HELLO = 2;
    private static final int HS_CERTIFICATE = 11;
    private static final int HS_SERVER_KEY_EXCHANGE = 12;
    private static final int HS_SERVER_HELLO_DONE = 14;

    private static final int TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 =
            0xC02B;

    /*
     * RFC 8422 / RFC 8446 ID for X25519.
     */
    private static final int GROUP_X25519 = 29;

    /*
     * TLS 1.2 signature_algorithms:
     *
     * hash = SHA256 = 4
     * signature = ECDSA = 3
     */
    private static final int SIG_ECDSA_SHA256 = 0x0403;

    private SocketConnection socket;
    private InputStream in;
    private OutputStream out;

    /*
     * Reusable TLS record buffer.
     *
     * Phase 1 uses a fairly large buffer for simplicity.
     * We can shrink this substantially once everything works.
     */
    private final byte[] recordBuffer = new byte[16384];

    /*
     * Server random obtained from ServerHello.
     */
    private final byte[] serverRandom = new byte[32];

    /*
     * X25519 server public key from ServerKeyExchange.
     */
    private final byte[] serverKey = new byte[32];

    private final byte[] clientPrivateKey = new byte[32];
    private final byte[] clientPublicKey = new byte[32];
    private final byte[] sharedSecret = new byte[32];

    private final X25519 x25519 = new X25519();

    public TinyTLS() {
    }

    public void connect(String host, int port) throws IOException {

        String url = "socket://" + host + ":" + port;

        debuglog("TLS: connecting to " + url);

        socket = (SocketConnection) Connector.open(
                url,
                Connector.READ_WRITE
        );

        in = socket.openInputStream();
        out = socket.openOutputStream();

        debuglog("TLS: TCP connected");

        sendClientHello(host);

        debuglog("TLS: ClientHello sent");

        readInitialHandshake();

        debuglog("TLS: Phase 1 complete");
    }

    public void close() {

        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }

    /*
     * ------------------------------------------------------------
     * ClientHello
     * ------------------------------------------------------------
     */

    private void sendClientHello(String host) throws IOException {

        /*
         * For Phase 1 we're constructing the entire ClientHello
         * in one small buffer.
         */
        byte[] buf = new byte[512];

        int p = 0;

        /*
         * TLS record header
         *
         * ContentType = handshake (22)
         * Version = TLS 1.0 (0x0301)
         *
         * Using 0x0301 in the record-layer version is intentional
         * for compatibility with TLS servers.
         */
        buf[p++] = 22;
        buf[p++] = 3;
        buf[p++] = 1;

        int recordLengthPosition = p;
        p += 2;

        /*
         * Handshake header
         *
         * HandshakeType = ClientHello (1)
         */
        buf[p++] = 1;

        int handshakeLengthPosition = p;
        p += 3;

        int helloStart = p;

        /*
         * ClientHello.client_version = TLS 1.2
         */
        buf[p++] = 3;
        buf[p++] = 3;

        /*
         * Client random.
         *
         * TEMPORARY FOR PHASE 1.
         *
         * This is NOT cryptographically secure.
         * We only need enough variation to test the handshake parser.
         */
        fillTemporaryRandom(buf, p, 32);
        p += 32;

        /*
         * Session ID:
         *
         * empty
         */
        buf[p++] = 0;

        /*
         * Cipher suites length = 2 bytes
         *
         * Exactly one suite.
         */
        writeU16(buf, p, 2);
        p += 2;

        writeU16(
                buf,
                p,
                TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
        );
        p += 2;

        /*
         * Compression methods.
         *
         * One method:
         *   null compression
         */
        buf[p++] = 1;
        buf[p++] = 0;

        /*
         * Extensions.
         *
         * We'll fill the total length later.
         */
        int extensionsLengthPosition = p;
        p += 2;

        int extensionsStart = p;

        /*
         * --------------------------------------------------------
         * server_name / SNI
         * --------------------------------------------------------
         */

        byte[] hostBytes = ascii(host);

        writeU16(buf, p, 0x0000);
        p += 2;

        int sniExtensionLengthPosition = p;
        p += 2;

        int sniStart = p;

        /*
         * ServerNameList length
         */
        writeU16(buf, p, 1 + 2 + hostBytes.length);
        p += 2;

        /*
         * name_type = host_name
         */
        buf[p++] = 0;

        writeU16(buf, p, hostBytes.length);
        p += 2;

        System.arraycopy(
                hostBytes,
                0,
                buf,
                p,
                hostBytes.length
        );

        p += hostBytes.length;

        writeU16(
                buf,
                sniExtensionLengthPosition,
                p - sniStart
        );

        /*
         * --------------------------------------------------------
         * supported_groups
         * --------------------------------------------------------
         *
         * X25519 only.
         */

        writeU16(buf, p, 0x000A);
        p += 2;

        /*
         * Extension data length:
         *
         * 2 bytes vector length
         * 2 bytes X25519
         */
        writeU16(buf, p, 4);
        p += 2;

        /*
         * supported_groups vector length
         */
        writeU16(buf, p, 2);
        p += 2;

        writeU16(buf, p, GROUP_X25519);
        p += 2;

        /*
         * --------------------------------------------------------
         * signature_algorithms
         * --------------------------------------------------------
         *
         * ECDSA + SHA256 only.
         */

        writeU16(buf, p, 0x000D);
        p += 2;

        /*
         * Extension length
         */
        writeU16(buf, p, 4);
        p += 2;

        /*
         * SignatureAlgorithms vector length
         */
        writeU16(buf, p, 2);
        p += 2;

        writeU16(buf, p, SIG_ECDSA_SHA256);
        p += 2;

        /*
         * --------------------------------------------------------
         * ec_point_formats
         * --------------------------------------------------------
         *
         * This extension is primarily for the older Weierstrass
         * EC groups. Including uncompressed is cheap and improves
         * TLS 1.2 compatibility.
         */

        writeU16(buf, p, 0x000B);
        p += 2;

        writeU16(buf, p, 2);
        p += 2;

        buf[p++] = 1;
        buf[p++] = 0;

        /*
         * Fill extension length.
         */
        writeU16(
                buf,
                extensionsLengthPosition,
                p - extensionsStart
        );

        /*
         * Fill handshake length.
         */
        int handshakeLength = p - helloStart;

        writeU24(
                buf,
                handshakeLengthPosition,
                handshakeLength
        );

        /*
         * Fill TLS record length.
         *
         * Record payload begins after the 5-byte record header.
         */
        writeU16(
                buf,
                recordLengthPosition,
                p - 5
        );

        out.write(buf, 0, p);
        out.flush();

        debuglog(
                "TLS: ClientHello length = " + p
        );
    }

    /*
     * ------------------------------------------------------------
     * Initial server handshake
     * ------------------------------------------------------------
     */

    private void readInitialHandshake() throws IOException {

        boolean gotServerHello = false;
        boolean gotCertificate = false;
        boolean gotKeyExchange = false;

        while (true) {

            int type = readU8(in);
            int major = readU8(in);
            int minor = readU8(in);
            int length = readU16(in);

            debuglog(
                    "TLS: record type=" + type
                    + " version="
                    + major + "." + minor
                    + " length=" + length
            );

            if (length > recordBuffer.length) {
                throw new IOException(
                        "TLS record too large: " + length
                );
            }

            readFully(
                    in,
                    recordBuffer,
                    0,
                    length
            );

            if (type == 21) {
                parseAlert(recordBuffer, length);
                throw new IOException("TLS alert");
            }

            if (type != TLS_HANDSHAKE) {
                debuglog(
                        "TLS: ignoring record type " + type
                );
                continue;
            }

            /*
             * NOTE:
             *
             * This Phase 1 parser assumes complete handshake
             * messages are contained in the record.
             *
             * We'll add proper cross-record handshake
             * reassembly after confirming Discord's behavior.
             */

            int p = 0;

            while (p < length) {

                if (length - p < 4) {
                    throw new IOException(
                            "Incomplete handshake header"
                    );
                }

                int hsType =
                        recordBuffer[p] & 0xFF;

                int hsLength =
                        ((recordBuffer[p + 1] & 0xFF) << 16)
                        | ((recordBuffer[p + 2] & 0xFF) << 8)
                        | (recordBuffer[p + 3] & 0xFF);

                p += 4;

                if (hsLength > length - p) {
                    throw new IOException(
                            "Handshake spans TLS records"
                    );
                }

                debuglog(
                        "TLS: handshake type="
                        + hsType
                        + " length="
                        + hsLength
                );

                if (hsType == HS_SERVER_HELLO) {

                    parseServerHello(
                            recordBuffer,
                            p,
                            hsLength
                    );

                    gotServerHello = true;

                } else if (hsType == HS_CERTIFICATE) {

                    /*
                     * Deliberately do not parse X.509.
                     */
                    debuglog(
                            "TLS: skipping certificate message"
                    );

                    gotCertificate = true;

                } else if (hsType == HS_SERVER_KEY_EXCHANGE) {

                    parseServerKeyExchange(
                            recordBuffer,
                            p,
                            hsLength
                    );

                    gotKeyExchange = true;

                } else if (hsType == HS_SERVER_HELLO_DONE) {

                    debuglog(
                            "TLS: ServerHelloDone"
                    );

                    if (!gotServerHello) {
                        throw new IOException(
                                "Missing ServerHello"
                        );
                    }

                    if (!gotCertificate) {
                        throw new IOException(
                                "Missing Certificate"
                        );
                    }

                    if (!gotKeyExchange) {
                        throw new IOException(
                                "Missing ServerKeyExchange"
                        );
                    }

                    generateTemporaryPrivateKey();

                    x25519.publicFromPrivate(
                            clientPublicKey,
                            0,
                            clientPrivateKey,
                            0
                    );

                    debuglog(
                            "TLS: client X25519 public key:"
                    );

                    printHex(
                            clientPublicKey,
                            0,
                            32
                    );

                    x25519.scalarMult(
                            sharedSecret,
                            0,
                            clientPrivateKey,
                            0,
                            serverKey,
                            0
                    );

                    if (isAllZero(sharedSecret)) {
                        throw new IOException(
                            "Invalid X25519 shared secret"
                        );
                    }

                    debuglog(
                            "TLS: X25519 shared secret:"
                    );

                    printHex(
                            sharedSecret,
                            0,
                            32
                    );

                    sendClientKeyExchange();

                    return;

                } else {

                    debuglog(
                            "TLS: ignoring handshake type "
                            + hsType
                    );
                }

                p += hsLength;
            }
        }
    }

    private static boolean isAllZero(byte[] b) {

        int x = 0;
        int i;

        for (i = 0; i < 32; i++) {
            x |= b[i] & 0xFF;
        }

        return x == 0;
    }

    private void generateTemporaryPrivateKey() {

        java.util.Random r =
            new java.util.Random(
                System.currentTimeMillis()
                ^ Runtime.getRuntime().freeMemory()
            );

        int i;
        int x = 0;

        for (i = 0; i < 32; i++) {

            /*
            * CLDC 1.0 has nextInt(), so periodically
            * obtain another 32 bits.
            */
            if ((i & 3) == 0) {
                x = r.nextInt();
            }

            clientPrivateKey[i] =
                (byte) x;

            x >>>= 8;
        }

        /*
        * scalarMult() performs clamping itself.
        *
        * Do it here as well so the stored value corresponds
        * to what we're actually using.
        */
        clientPrivateKey[0] =
            (byte) (clientPrivateKey[0] & 248);

        clientPrivateKey[31] =
            (byte) (clientPrivateKey[31] & 127);

        clientPrivateKey[31] =
            (byte) (clientPrivateKey[31] | 64);
    }

    private void sendClientKeyExchange()
            throws IOException {

        /*
        * TLS record:
        *
        * 5 byte record header
        * 4 byte handshake header
        * 1 byte key length
        * 32 byte key
        *
        * Total = 42 bytes.
        */
        byte[] b = new byte[42];

        int p = 0;

        /*
        * TLS record header.
        */
        b[p++] = 22;       // handshake
        b[p++] = 3;
        b[p++] = 3;        // TLS 1.2

        /*
        * Record payload:
        *
        * 4 + 1 + 32 = 37
        */
        writeU16(b, p, 37);
        p += 2;

        /*
        * ClientKeyExchange handshake.
        */
        b[p++] = 16;

        /*
        * Handshake payload = 33.
        */
        writeU24(b, p, 33);
        p += 3;

        /*
        * X25519 public key vector.
        */
        b[p++] = 32;

        System.arraycopy(
            clientPublicKey,
            0,
            b,
            p,
            32
        );

        p += 32;

        out.write(b, 0, p);
        out.flush();

        debuglog(
            "TLS: ClientKeyExchange sent"
        );
    }

    /*
     * ------------------------------------------------------------
     * ServerHello
     * ------------------------------------------------------------
     */

    private void parseServerHello(
            byte[] b,
            int off,
            int len) throws IOException {

        int end = off + len;
        int p = off;

        if (len < 38) {
            throw new IOException(
                    "ServerHello too short"
            );
        }

        int major = b[p++] & 0xFF;
        int minor = b[p++] & 0xFF;

        debuglog(
                "TLS: server selected version "
                + major + "." + minor
        );

        if (major != 3 || minor != 3) {
            throw new IOException(
                    "Server did not select TLS 1.2"
            );
        }

        System.arraycopy(
                b,
                p,
                serverRandom,
                0,
                32
        );

        p += 32;

        int sessionLength = b[p++] & 0xFF;

        if (p + sessionLength + 3 > end) {
            throw new IOException(
                    "Bad ServerHello session ID"
            );
        }

        p += sessionLength;

        int cipher =
                ((b[p] & 0xFF) << 8)
                | (b[p + 1] & 0xFF);

        p += 2;

        debuglog(
                "TLS: selected cipher = 0x"
                + hex16(cipher)
        );

        if (cipher !=
                TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256) {

            throw new IOException(
                    "Unexpected cipher suite"
            );
        }

        int compression = b[p++] & 0xFF;

        if (compression != 0) {
            throw new IOException(
                    "Unexpected compression"
            );
        }

        debuglog(
                "TLS: ServerHello OK"
        );
    }

    /*
     * ------------------------------------------------------------
     * ServerKeyExchange
     * ------------------------------------------------------------
     */

    private void parseServerKeyExchange(
            byte[] b,
            int off,
            int len) throws IOException {

        int p = off;
        int end = off + len;

        /*
         * ECParameters for named_curve:
         *
         * curve_type       1 byte
         * named_curve      2 bytes
         *
         * For X25519:
         *
         * curve_type = 3 (named_curve)
         * named_curve = 29
         */

        if (p + 4 > end) {
            throw new IOException(
                    "ServerKeyExchange too short"
            );
        }

        int curveType = b[p++] & 0xFF;

        if (curveType != 3) {
            throw new IOException(
                    "Unsupported EC curve type: "
                    + curveType
            );
        }

        int group =
                ((b[p] & 0xFF) << 8)
                | (b[p + 1] & 0xFF);

        p += 2;

        debuglog(
                "TLS: ECDHE group = " + group
        );

        if (group != GROUP_X25519) {
            throw new IOException(
                    "Server did not select X25519"
            );
        }

        /*
         * ECPoint / public key is represented as:
         *
         * opaque point <1..2^8-1>
         *
         * X25519 key should therefore have length 32.
         */

        int keyLength = b[p++] & 0xFF;

        debuglog(
                "TLS: server public key length = "
                + keyLength
        );

        if (keyLength != 32) {
            throw new IOException(
                    "Unexpected X25519 key length"
            );
        }

        if (p + 32 > end) {
            throw new IOException(
                    "Truncated X25519 key"
            );
        }

        System.arraycopy(
                b,
                p,
                serverKey,
                0,
                32
        );

        p += 32;

        debuglog(
                "TLS: X25519 server key:"
        );

        printHex(serverKey, 0, 32);

        /*
         * Everything after the public key is the
         * TLS 1.2 signature structure.
         *
         * We're deliberately not authenticating the
         * server, so Phase 1 ignores it.
         */

        debuglog(
                "TLS: ignoring ServerKeyExchange signature"
        );
    }

    /*
     * ------------------------------------------------------------
     * TLS alert
     * ------------------------------------------------------------
     */

    private void parseAlert(byte[] b, int len) {

        if (len >= 2) {

            int level = b[0] & 0xFF;
            int description = b[1] & 0xFF;

            debuglog(
                    "TLS ALERT: level="
                    + level
                    + " description="
                    + description
            );

        } else {

            debuglog(
                    "TLS ALERT: malformed"
            );
        }
    }

    /*
     * ------------------------------------------------------------
     * Temporary random
     * ------------------------------------------------------------
     *
     * NOT CRYPTOGRAPHICALLY SECURE.
     *
     * Phase 2 should replace this with our actual PRNG.
     */

    private static void fillTemporaryRandom(
            byte[] b,
            int off,
            int len) {

        long seed =
                System.currentTimeMillis()
                ^ Runtime.getRuntime().freeMemory();

        int x = (int) seed;

        int i;

        for (i = 0; i < len; i++) {

            /*
             * Simple xorshift-like generator.
             *
             * Fine for Phase 1 testing.
             * NOT suitable for X25519 private keys.
             */
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;

            b[off + i] = (byte) x;
        }
    }

    /*
     * ------------------------------------------------------------
     * Primitive encoding helpers
     * ------------------------------------------------------------
     */

    private static void writeU16(
            byte[] b,
            int off,
            int value) {

        b[off] =
                (byte) (value >>> 8);

        b[off + 1] =
                (byte) value;
    }

    private static void writeU24(
            byte[] b,
            int off,
            int value) {

        b[off] =
                (byte) (value >>> 16);

        b[off + 1] =
                (byte) (value >>> 8);

        b[off + 2] =
                (byte) value;
    }

    private static int readU8(
            InputStream in) throws IOException {

        int x = in.read();

        if (x < 0) {
            throw new IOException(
                    "Unexpected EOF"
            );
        }

        return x;
    }

    private static int readU16(
            InputStream in) throws IOException {

        int a = readU8(in);
        int b = readU8(in);

        return (a << 8) | b;
    }

    private static void readFully(
            InputStream in,
            byte[] b,
            int off,
            int len) throws IOException {

        while (len > 0) {

            int n = in.read(
                    b,
                    off,
                    len
            );

            if (n < 0) {
                throw new IOException(
                        "Unexpected EOF"
                );
            }

            if (n == 0) {
                continue;
            }

            off += n;
            len -= n;
        }
    }

    /*
     * ------------------------------------------------------------
     * CLDC-safe ASCII conversion
     * ------------------------------------------------------------
     */

    private static byte[] ascii(String s) {

        int len = s.length();

        byte[] b = new byte[len];

        int i;

        for (i = 0; i < len; i++) {
            b[i] = (byte) s.charAt(i);
        }

        return b;
    }

    /*
     * ------------------------------------------------------------
     * Debug helpers
     * ------------------------------------------------------------
     */

    private static String hex16(int x) {

        char[] c = new char[4];

        c[0] = hexDigit((x >>> 12) & 15);
        c[1] = hexDigit((x >>> 8) & 15);
        c[2] = hexDigit((x >>> 4) & 15);
        c[3] = hexDigit(x & 15);

        return new String(c);
    }

    private static char hexDigit(int x) {

        if (x < 10) {
            return (char) ('0' + x);
        }

        return (char) ('A' + x - 10);
    }

    private static void printHex(
            byte[] b,
            int off,
            int len) {

        StringBuffer s =
                new StringBuffer(len * 2);

        int i;

        for (i = 0; i < len; i++) {

            int x = b[off + i] & 0xFF;

            s.append(
                    hexDigit((x >>> 4) & 15)
            );

            s.append(
                    hexDigit(x & 15)
            );
        }

        debuglog(s.toString());
    }
}