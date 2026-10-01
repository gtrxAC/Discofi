package fi.gtrxac.discord;

/**
 * Small X25519 implementation intended for CLDC 1.0.
 *
 * No:
 *   BigInteger
 *   java.security
 *   floating point
 *   generics
 *
 * Field elements use 16 little-endian 16-bit limbs.
 *
 * This implementation is intended specifically for X25519.
 */
public final class X25519 {

    private static final long MASK = 0xFFFFL;

    /*
     * Working storage.
     *
     * Keeping these as instance fields means scalarMult()
     * does not allocate temporary field-element arrays.
     */
    private final long[] x1 = new long[16];
    private final long[] x2 = new long[16];
    private final long[] z2 = new long[16];
    private final long[] x3 = new long[16];
    private final long[] z3 = new long[16];

    private final long[] a = new long[16];
    private final long[] b = new long[16];
    private final long[] c = new long[16];
    private final long[] d = new long[16];

    private final long[] aa = new long[16];
    private final long[] bb = new long[16];
    private final long[] e = new long[16];

    private final long[] da = new long[16];
    private final long[] cb = new long[16];

    private final long[] t0 = new long[16];
    private final long[] t1 = new long[16];

    private final byte[] scalar = new byte[32];

    public X25519() {
    }

    /**
     * out = X25519(privateKey, publicKey)
     *
     * All three arrays must contain at least 32 bytes from
     * their respective offsets.
     */
    public void scalarMult(
            byte[] out,
            int outOff,
            byte[] privateKey,
            int privateOff,
            byte[] publicKey,
            int publicOff) {

        int i;
        int pos;
        int swap;
        int bit;

        /*
         * Copy scalar because clamping must not modify the
         * caller's private key.
         */
        for (i = 0; i < 32; i++) {
            scalar[i] = privateKey[privateOff + i];
        }

        /*
         * RFC 7748 scalar clamping.
         */
        scalar[0] =
                (byte) (scalar[0] & 248);

        scalar[31] =
                (byte) (scalar[31] & 127);

        scalar[31] =
                (byte) (scalar[31] | 64);

        /*
         * Decode server u-coordinate.
         */
        unpack(x1, publicKey, publicOff);

        /*
         * RFC 7748 requires the high bit of the input
         * u-coordinate to be ignored.
         */
        x1[15] &= 0x7FFF;

        /*
         * x2 = 1
         * z2 = 0
         * x3 = x1
         * z3 = 1
         */

        for (i = 0; i < 16; i++) {
            x2[i] = 0;
            z2[i] = 0;
            x3[i] = x1[i];
            z3[i] = 0;
        }

        x2[0] = 1;
        z3[0] = 1;

        swap = 0;

        /*
         * Montgomery ladder.
         *
         * Process bits 254 down through 0.
         */
        for (pos = 254; pos >= 0; pos--) {

            bit =
                    (scalar[pos >>> 3]
                    >>> (pos & 7))
                    & 1;

            swap ^= bit;

            cswap(x2, x3, swap);
            cswap(z2, z3, swap);

            swap = bit;

            /*
             * A  = x2 + z2
             * AA = A^2
             * B  = x2 - z2
             * BB = B^2
             * E  = AA - BB
             */

            add(a, x2, z2);
            square(aa, a);

            sub(b, x2, z2);
            square(bb, b);

            sub(e, aa, bb);

            /*
             * C = x3 + z3
             * D = x3 - z3
             */

            add(c, x3, z3);
            sub(d, x3, z3);

            /*
             * DA = D * A
             * CB = C * B
             */

            mul(da, d, a);
            mul(cb, c, b);

            /*
             * x3 = (DA + CB)^2
             */

            add(t0, da, cb);
            square(x3, t0);

            /*
             * z3 = x1 * (DA - CB)^2
             */

            sub(t0, da, cb);
            square(t1, t0);
            mul(z3, x1, t1);

            /*
             * x2 = AA * BB
             */

            mul(x2, aa, bb);

            /*
             * z2 = E * (AA + 121665 * E)
             *
             * RFC 7748 a24 = 121665.
             */

            mulSmall(t0, e, 121665);
            add(t0, aa, t0);
            mul(z2, e, t0);
        }

        cswap(x2, x3, swap);
        cswap(z2, z3, swap);

        /*
         * Convert projective x2/z2 into affine:
         *
         * x = x2 * inverse(z2)
         */
        invert(t0, z2);
        mul(t1, x2, t0);

        pack(out, outOff, t1);
    }

    /**
     * Calculate an X25519 public key:
     *
     * public = X25519(private, 9)
     */
    public void publicFromPrivate(
            byte[] out,
            int outOff,
            byte[] privateKey,
            int privateOff) {

        /*
         * Avoid allocating a 32-byte basepoint array.
         *
         * t0 is field-element storage, but we need bytes here.
         * Use scalar as temporary byte storage. scalarMult()
         * immediately copies the private key into scalar before
         * decoding the public input, so using scalar here would
         * conflict.
         *
         * Therefore generate the base point directly using this
         * tiny local array.
         */
        byte[] base = new byte[32];

        base[0] = 9;

        scalarMult(
                out,
                outOff,
                privateKey,
                privateOff,
                base,
                0
        );
    }

    /*
     * ------------------------------------------------------------
     * Field operations
     * ------------------------------------------------------------
     */

    private static void add(
            long[] out,
            long[] x,
            long[] y) {

        int i;

        for (i = 0; i < 16; i++) {
            out[i] = x[i] + y[i];
        }
    }

    private static void sub(
            long[] out,
            long[] x,
            long[] y) {

        int i;

        /*
         * Add enough multiples of p to keep every limb
         * positive.
         *
         * p = 2^255 - 19.
         */
        out[0] =
                x[0] + 0x1FFDA - y[0];

        for (i = 1; i < 15; i++) {
            out[i] =
                    x[i] + 0x1FFFE - y[i];
        }

        out[15] =
                x[15] + 0x0FFFE - y[15];
    }

    private static void square(
            long[] out,
            long[] x) {

        mul(out, x, x);
    }

    /**
     * Field multiplication modulo 2^255 - 19.
     */
    private static void mul(
            long[] out,
            long[] x,
            long[] y) {

        long[] t = new long[31];

        int i;
        int j;

        for (i = 0; i < 31; i++) {
            t[i] = 0;
        }

        for (i = 0; i < 16; i++) {

            for (j = 0; j < 16; j++) {

                t[i + j] +=
                        x[i] * y[j];
            }
        }

        /*
         * Since 2^256 == 38 mod p:
         *
         * fold limbs 16..30 back into 0..14.
         */
        for (i = 30; i >= 16; i--) {

            t[i - 16] +=
                    38L * t[i];
        }

        for (i = 0; i < 16; i++) {
            out[i] = t[i];
        }

        carry(out);
        carry(out);
    }

    private static void mulSmall(
            long[] out,
            long[] x,
            long n) {

        int i;

        for (i = 0; i < 16; i++) {
            out[i] = x[i] * n;
        }

        carry(out);
        carry(out);
    }

    /**
     * Normalize radix-2^16 limbs.
     */
    private static void carry(long[] x) {

        int i;
        long c;

        for (i = 0; i < 15; i++) {

            c = x[i] >> 16;

            x[i] &= MASK;

            x[i + 1] += c;
        }

        /*
         * Limb 15 only has 15 significant bits because
         * the field is 255 bits.
         */
        c = x[15] >> 15;

        x[15] &= 0x7FFF;

        /*
         * 2^255 == 19 mod p.
         */
        x[0] += c * 19;
    }

    /*
     * ------------------------------------------------------------
     * Conditional swap
     * ------------------------------------------------------------
     */

    private static void cswap(
            long[] x,
            long[] y,
            int swap) {

        long mask;
        long v;

        int i;

        /*
         * swap = 0 -> mask = 0
         * swap = 1 -> mask = -1
         */
        mask = -((long) swap);

        for (i = 0; i < 16; i++) {

            v = mask & (x[i] ^ y[i]);

            x[i] ^= v;
            y[i] ^= v;
        }
    }

    /*
     * ------------------------------------------------------------
     * Inversion
     * ------------------------------------------------------------
     *
     * x^(p-2)
     *
     * p - 2 = 2^255 - 21
     *
     * This uses the straightforward square-and-multiply
     * exponentiation pattern. It is not the fastest possible
     * addition chain, but keeps Phase 2 easy to verify.
     */

    private void invert(
            long[] out,
            long[] z) {

        int i;

        copy(t0, z);

        /*
         * Compute z^(2^255 - 21).
         *
         * Standard Curve25519 inversion sequence:
         *
         * starting from z, repeatedly square from bit 253
         * down to 0, multiplying by z except at exponent
         * bit positions 2 and 4.
         */

        for (i = 253; i >= 0; i--) {

            square(t1, t0);

            if (i != 2 && i != 4) {
                mul(t0, t1, z);
            } else {
                copy(t0, t1);
            }
        }

        copy(out, t0);
    }

    private static void copy(
            long[] out,
            long[] in) {

        int i;

        for (i = 0; i < 16; i++) {
            out[i] = in[i];
        }
    }

    /*
     * ------------------------------------------------------------
     * Encoding
     * ------------------------------------------------------------
     */

    private static void unpack(
            long[] out,
            byte[] in,
            int off) {

        int i;

        for (i = 0; i < 16; i++) {

            out[i] =
                    (in[off + i * 2] & 0xFF)
                    |
                    ((long)
                    (in[off + i * 2 + 1] & 0xFF)
                    << 8);
        }

        out[15] &= 0x7FFF;
    }

    private static void pack(
            byte[] out,
            int off,
            long[] x) {

        long[] t = new long[16];
        long[] m = new long[16];

        int i;
        int j;
        long borrow;
        long v;

        for (i = 0; i < 16; i++) {
            t[i] = x[i];
        }

        carry(t);
        carry(t);
        carry(t);

        /*
         * Conditionally subtract p.
         *
         * Do this twice to ensure canonical reduction.
         */
        for (j = 0; j < 2; j++) {

            m[0] = t[0] - 0xFFED;

            borrow = m[0] >> 63;
            m[0] &= MASK;

            for (i = 1; i < 15; i++) {

                v =
                        t[i]
                        - 0xFFFF
                        + borrow;

                borrow = v >> 63;
                m[i] = v & MASK;
            }

            v =
                    t[15]
                    - 0x7FFF
                    + borrow;

            borrow = v >> 63;
            m[15] = v & MASK;

            /*
             * If subtraction did not underflow,
             * use m. Otherwise retain t.
             */
            int useM =
                    (int) (1 + borrow);

            conditionalMove(
                    t,
                    m,
                    useM
            );
        }

        for (i = 0; i < 16; i++) {

            out[off + i * 2] =
                    (byte) t[i];

            out[off + i * 2 + 1] =
                    (byte) (t[i] >>> 8);
        }
    }

    private static void conditionalMove(
            long[] x,
            long[] y,
            int move) {

        long mask =
                -((long) move);

        long v;

        int i;

        for (i = 0; i < 16; i++) {

            v =
                    mask
                    & (x[i] ^ y[i]);

            x[i] ^= v;
        }
    }
}