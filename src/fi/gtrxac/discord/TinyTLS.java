package fi.gtrxac.discord;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public final class TinyTLS {

    private static final int TYPE_ALERT = 21;
    private static final int TYPE_HANDSHAKE = 22;

    private static final int HS_CLIENT_HELLO = 1;
    private static final int HS_SERVER_HELLO = 2;
    private static final int HS_CERTIFICATE = 11;
    private static final int HS_SERVER_KEY_EXCHANGE = 12;
    private static final int HS_SERVER_HELLO_DONE = 14;
    private static final int HS_CLIENT_KEY_EXCHANGE = 16;

    private static final int CIPHER =
        0xC02B;

    private static final int GROUP_X25519 = 29;
    private static final int SIG_ECDSA_SHA256 = 0x0403;

    private SocketConnection socket;
    private InputStream in;
    private OutputStream out;

    private final StringBuffer log;

    /*
     * Running TLS handshake transcript.
     */
    private final SHA256 transcript =
        new SHA256();

    private final X25519 x25519 =
        new X25519();

    private final byte[] clientRandom =
        new byte[32];

    private final byte[] serverRandom =
        new byte[32];

    private final byte[] serverKey =
        new byte[32];

    private final byte[] clientPrivateKey =
        new byte[32];

    private final byte[] clientPublicKey =
        new byte[32];

    private final byte[] sharedSecret =
        new byte[32];

    private final byte[] masterSecret =
        new byte[48];

    /*
     * AES-128-GCM key block:
     *
     * client_write_key = 16
     * server_write_key = 16
     * client_write_IV  = 4
     * server_write_IV  = 4
     *
     * Total = 40.
     */
    private final byte[] keyBlock =
        new byte[40];

    /*
     * Small scratch buffer for streaming skips/hashing.
     */
    private final byte[] scratch =
        new byte[128];

    /*
     * State of the current incoming TLS record.
     */
    private int recordRemaining;

    public TinyTLS(StringBuffer log) {
        this.log = log;
    }

    public void connect(
            String host,
            int port) throws IOException {

        String url =
            "socket://" + host + ":" + port;

        log("Connecting");

        socket =
            (SocketConnection)
            Connector.open(
                url,
                Connector.READ_WRITE
            );

        in = socket.openInputStream();
        out = socket.openOutputStream();

        log("TCP connected");

        transcript.reset();

        sendClientHello(host);

        readServerHandshake();

        generateClientKey();

        sendClientKeyExchange();

        deriveSecrets();

        log("Phase 3A complete");
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

    private void log(String s) {
        TLSUtil.log(log, s);
    }

    /*
     * ------------------------------------------------------------
     * ClientHello
     * ------------------------------------------------------------
     */

    private void sendClientHello(String host)
            throws IOException {

        byte[] buf = new byte[512];

        int p = 0;

        /*
         * TLS record header.
         */
        buf[p++] = 22;
        buf[p++] = 3;
        buf[p++] = 1;

        int recordLengthPos = p;
        p += 2;

        /*
         * Handshake begins here.
         *
         * Everything from this point is part of transcript.
         */
        int handshakeStart = p;

        buf[p++] = HS_CLIENT_HELLO;

        int handshakeLengthPos = p;
        p += 3;

        int bodyStart = p;

        /*
         * TLS 1.2
         */
        buf[p++] = 3;
        buf[p++] = 3;

        TLSUtil.temporaryRandom(
            clientRandom,
            0,
            32
        );

        System.arraycopy(
            clientRandom,
            0,
            buf,
            p,
            32
        );

        p += 32;

        /*
         * Empty session ID.
         */
        buf[p++] = 0;

        /*
         * One cipher.
         */
        TLSUtil.writeU16(buf, p, 2);
        p += 2;

        TLSUtil.writeU16(
            buf,
            p,
            CIPHER
        );

        p += 2;

        /*
         * Null compression.
         */
        buf[p++] = 1;
        buf[p++] = 0;

        /*
         * Extensions.
         */
        int extensionsLengthPos = p;
        p += 2;

        int extensionsStart = p;

        /*
         * SNI.
         */
        byte[] hostBytes =
            TLSUtil.ascii(host);

        TLSUtil.writeU16(buf, p, 0);
        p += 2;

        int sniLengthPos = p;
        p += 2;

        int sniStart = p;

        TLSUtil.writeU16(
            buf,
            p,
            3 + hostBytes.length
        );

        p += 2;

        buf[p++] = 0;

        TLSUtil.writeU16(
            buf,
            p,
            hostBytes.length
        );

        p += 2;

        System.arraycopy(
            hostBytes,
            0,
            buf,
            p,
            hostBytes.length
        );

        p += hostBytes.length;

        TLSUtil.writeU16(
            buf,
            sniLengthPos,
            p - sniStart
        );

        /*
         * supported_groups:
         * X25519 only.
         */
        TLSUtil.writeU16(
            buf,
            p,
            0x000A
        );

        p += 2;

        TLSUtil.writeU16(buf, p, 4);
        p += 2;

        TLSUtil.writeU16(buf, p, 2);
        p += 2;

        TLSUtil.writeU16(
            buf,
            p,
            GROUP_X25519
        );

        p += 2;

        /*
         * signature_algorithms:
         * ECDSA/SHA256 only.
         */
        TLSUtil.writeU16(
            buf,
            p,
            0x000D
        );

        p += 2;

        TLSUtil.writeU16(buf, p, 4);
        p += 2;

        TLSUtil.writeU16(buf, p, 2);
        p += 2;

        TLSUtil.writeU16(
            buf,
            p,
            SIG_ECDSA_SHA256
        );

        p += 2;

        /*
         * ec_point_formats:
         * uncompressed.
         */
        TLSUtil.writeU16(
            buf,
            p,
            0x000B
        );

        p += 2;

        TLSUtil.writeU16(buf, p, 2);
        p += 2;

        buf[p++] = 1;
        buf[p++] = 0;

        TLSUtil.writeU16(
            buf,
            extensionsLengthPos,
            p - extensionsStart
        );

        TLSUtil.writeU24(
            buf,
            handshakeLengthPos,
            p - bodyStart
        );

        TLSUtil.writeU16(
            buf,
            recordLengthPos,
            p - 5
        );

        /*
         * Hash ClientHello handshake structure,
         * NOT the TLS record header.
         */
        transcript.update(
            buf,
            handshakeStart,
            p - handshakeStart
        );

        out.write(buf, 0, p);
        out.flush();

        log("ClientHello sent");
    }

    /*
     * ------------------------------------------------------------
     * Streaming incoming handshake
     * ------------------------------------------------------------
     */

    private void readServerHandshake()
            throws IOException {

        boolean hello = false;
        boolean cert = false;
        boolean keyExchange = false;

        while (true) {

            byte[] header = new byte[4];

            readHandshakeBytes(
                header,
                0,
                4
            );

            /*
             * Handshake header is part of transcript.
             */
            transcript.update(
                header,
                0,
                4
            );

            int type =
                header[0] & 0xFF;

            int len =
                ((header[1] & 0xFF) << 16)
                | ((header[2] & 0xFF) << 8)
                | (header[3] & 0xFF);

            log(
                "Handshake "
                + type
                + " len "
                + len
            );

            if (type == HS_SERVER_HELLO) {

                parseServerHello(len);
                hello = true;

            } else if (type == HS_CERTIFICATE) {

                /*
                 * Hash and discard entire body.
                 */
                hashAndSkip(len);

                cert = true;

                log("Certificate skipped");

            } else if (
                    type ==
                    HS_SERVER_KEY_EXCHANGE) {

                parseServerKeyExchange(len);

                keyExchange = true;

            } else if (
                    type ==
                    HS_SERVER_HELLO_DONE) {

                /*
                 * Usually zero length, but consume
                 * anything the server actually sends.
                 */
                hashAndSkip(len);

                if (!hello) {
                    throw new IOException(
                        "Missing ServerHello"
                    );
                }

                if (!cert) {
                    throw new IOException(
                        "Missing Certificate"
                    );
                }

                if (!keyExchange) {
                    throw new IOException(
                        "Missing ServerKeyExchange"
                    );
                }

                log("ServerHelloDone");

                return;

            } else {

                /*
                 * Unknown handshake messages are still
                 * part of the transcript.
                 */
                hashAndSkip(len);
            }
        }
    }

    /*
     * Reads bytes from the logical handshake stream.
     *
     * TLS record boundaries disappear here.
     */
    private void readHandshakeBytes(
            byte[] b,
            int off,
            int len) throws IOException {

        while (len > 0) {

            if (recordRemaining == 0) {
                beginHandshakeRecord();
            }

            int n = len;

            if (n > recordRemaining) {
                n = recordRemaining;
            }

            TLSUtil.readFully(
                in,
                b,
                off,
                n
            );

            off += n;
            len -= n;

            recordRemaining -= n;
        }
    }

    private void beginHandshakeRecord()
            throws IOException {

        int type = TLSUtil.readU8(in);
        int major = TLSUtil.readU8(in);
        int minor = TLSUtil.readU8(in);
        int len = TLSUtil.readU16(in);

        if (type == TYPE_ALERT) {

            if (len >= 2) {
                int level =
                    TLSUtil.readU8(in);

                int desc =
                    TLSUtil.readU8(in);

                /*
                 * Consume any extra alert bytes.
                 */
                int remaining = len - 2;

                while (remaining > 0) {
                    int n = remaining;

                    if (n > scratch.length) {
                        n = scratch.length;
                    }

                    TLSUtil.readFully(
                        in,
                        scratch,
                        0,
                        n
                    );

                    remaining -= n;
                }

                throw new IOException(
                    "TLS alert "
                    + level
                    + "/"
                    + desc
                );
            }

            throw new IOException(
                "Malformed TLS alert"
            );
        }

        if (type != TYPE_HANDSHAKE) {
            throw new IOException(
                "Expected handshake record, got "
                + type
            );
        }

        if (major != 3) {
            throw new IOException(
                "Bad TLS record version"
            );
        }

        recordRemaining = len;
    }

    /*
     * Read data, hash it, then discard it.
     */
    private void hashAndSkip(int len)
            throws IOException {

        while (len > 0) {

            int n = len;

            if (n > scratch.length) {
                n = scratch.length;
            }

            readHandshakeBytes(
                scratch,
                0,
                n
            );

            transcript.update(
                scratch,
                0,
                n
            );

            len -= n;
        }
    }

    /*
     * Read body data and simultaneously add it
     * to the handshake transcript.
     */
    private void readHashed(
            byte[] b,
            int off,
            int len) throws IOException {

        readHandshakeBytes(
            b,
            off,
            len
        );

        transcript.update(
            b,
            off,
            len
        );
    }

    /*
     * ------------------------------------------------------------
     * ServerHello
     * ------------------------------------------------------------
     */

    private void parseServerHello(int len)
            throws IOException {

        /*
         * ServerHello is small enough to keep.
         */
        if (len > 512) {
            throw new IOException(
                "ServerHello too large"
            );
        }

        byte[] b = new byte[len];

        readHashed(b, 0, len);

        int p = 0;

        if (len < 38) {
            throw new IOException(
                "ServerHello too short"
            );
        }

        int major = b[p++] & 0xFF;
        int minor = b[p++] & 0xFF;

        if (major != 3 || minor != 3) {
            throw new IOException(
                "Not TLS 1.2"
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

        int sessionLen =
            b[p++] & 0xFF;

        if (p + sessionLen + 3 > len) {
            throw new IOException(
                "Bad ServerHello"
            );
        }

        p += sessionLen;

        int cipher =
            ((b[p] & 0xFF) << 8)
            | (b[p + 1] & 0xFF);

        p += 2;

        if (cipher != CIPHER) {
            throw new IOException(
                "Cipher 0x"
                + TLSUtil.hex16(cipher)
            );
        }

        int compression =
            b[p++] & 0xFF;

        if (compression != 0) {
            throw new IOException(
                "Compression enabled"
            );
        }

        log("ServerHello C02B");
    }

    /*
     * ------------------------------------------------------------
     * ServerKeyExchange
     * ------------------------------------------------------------
     */

    private void parseServerKeyExchange(
            int len) throws IOException {

        if (len < 36) {
            throw new IOException(
                "Short ServerKeyExchange"
            );
        }

        /*
         * We only need:
         *
         * curve_type       1
         * named_curve      2
         * public key len   1
         * public key      32
         *
         * = 36 bytes
         */
        byte[] head = new byte[36];

        readHashed(
            head,
            0,
            36
        );

        int p = 0;

        int curveType =
            head[p++] & 0xFF;

        if (curveType != 3) {
            throw new IOException(
                "Unsupported curve type"
            );
        }

        int group =
            ((head[p] & 0xFF) << 8)
            | (head[p + 1] & 0xFF);

        p += 2;

        if (group != GROUP_X25519) {
            throw new IOException(
                "Not X25519"
            );
        }

        int keyLen =
            head[p++] & 0xFF;

        if (keyLen != 32) {
            throw new IOException(
                "Bad X25519 key"
            );
        }

        System.arraycopy(
            head,
            p,
            serverKey,
            0,
            32
        );

        /*
         * Signature follows.
         *
         * We deliberately do not verify it, but it MUST
         * still be included in the transcript hash.
         */
        hashAndSkip(len - 36);

        log("Server X25519 received");
    }

    /*
     * ------------------------------------------------------------
     * X25519
     * ------------------------------------------------------------
     */

    private void generateClientKey()
            throws IOException {

        TLSUtil.temporaryRandom(
            clientPrivateKey,
            0,
            32
        );

        /*
         * Clamp.
         */
        clientPrivateKey[0] =
            (byte)
            (clientPrivateKey[0] & 248);

        clientPrivateKey[31] =
            (byte)
            (clientPrivateKey[31] & 127);

        clientPrivateKey[31] =
            (byte)
            (clientPrivateKey[31] | 64);

        x25519.publicFromPrivate(
            clientPublicKey,
            0,
            clientPrivateKey,
            0
        );

        x25519.scalarMult(
            sharedSecret,
            0,
            clientPrivateKey,
            0,
            serverKey,
            0
        );

        if (TLSUtil.isAllZero(
                sharedSecret,
                0,
                32)) {

            throw new IOException(
                "Zero X25519 secret"
            );
        }

        log("X25519 complete");

        /*
         * Don't normally log the secret.
         *
         * Uncomment only while debugging:
         *
         * TLSUtil.logHex(
         *     log,
         *     "Shared",
         *     sharedSecret,
         *     0,
         *     32
         * );
         */
    }

    /*
     * ------------------------------------------------------------
     * ClientKeyExchange
     * ------------------------------------------------------------
     */

    private void sendClientKeyExchange()
            throws IOException {

        byte[] b = new byte[42];

        int p = 0;

        /*
         * Record header.
         */
        b[p++] = TYPE_HANDSHAKE;
        b[p++] = 3;
        b[p++] = 3;

        TLSUtil.writeU16(
            b,
            p,
            37
        );

        p += 2;

        int handshakeStart = p;

        /*
         * Handshake header.
         */
        b[p++] =
            HS_CLIENT_KEY_EXCHANGE;

        TLSUtil.writeU24(
            b,
            p,
            33
        );

        p += 3;

        /*
         * X25519 public key.
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

        /*
         * ClientKeyExchange IS part of the
         * Finished transcript.
         */
        transcript.update(
            b,
            handshakeStart,
            p - handshakeStart
        );

        out.write(b, 0, p);
        out.flush();

        log("ClientKeyExchange sent");
    }

    /*
     * ------------------------------------------------------------
     * TLS 1.2 secrets
     * ------------------------------------------------------------
     */

    private void deriveSecrets() {

        byte[] seed = new byte[64];

        /*
         * master_secret:
         *
         * client_random || server_random
         */
        System.arraycopy(
            clientRandom,
            0,
            seed,
            0,
            32
        );

        System.arraycopy(
            serverRandom,
            0,
            seed,
            32,
            32
        );

        TLSCrypto.prfSha256(
            sharedSecret,
            0,
            32,
            "master secret",
            seed,
            0,
            64,
            masterSecret,
            0,
            48
        );

        log("Master secret derived");

        /*
         * key expansion reverses random order:
         *
         * server_random || client_random
         */
        System.arraycopy(
            serverRandom,
            0,
            seed,
            0,
            32
        );

        System.arraycopy(
            clientRandom,
            0,
            seed,
            32,
            32
        );

        TLSCrypto.prfSha256(
            masterSecret,
            0,
            48,
            "key expansion",
            seed,
            0,
            64,
            keyBlock,
            0,
            40
        );

        log("Key block derived");

        /*
         * Snapshot the transcript hash.
         *
         * digest() destroys/finalizes its SHA object,
         * so copy the running transcript first.
         */
        SHA256 copy =
            new SHA256(transcript);

        byte[] handshakeHash =
            new byte[32];

        copy.digest(
            handshakeHash,
            0
        );

        TLSUtil.logHex(
            log,
            "Handshake hash",
            handshakeHash,
            0,
            32
        );

        /*
         * This is exactly the hash Phase 3B will use
         * for the client's Finished verify_data.
         */
    }

    /*
     * Accessors useful while developing Phase 3B.
     */

    public byte[] getMasterSecret() {
        return masterSecret;
    }

    public byte[] getKeyBlock() {
        return keyBlock;
    }
}