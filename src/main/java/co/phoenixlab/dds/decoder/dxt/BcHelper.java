package co.phoenixlab.dds.decoder.dxt;

final class BcHelper {

    public static final int RGB565_RED_MASK = 0b11111_000000_00000;
    public static final int RGB565_GREEN_MASK = 0b00000_111111_00000;
    public static final int RGB565_BLUE_MASK = 0b00000_000000_11111;
    public static final int RET_SIZE = 16;
    public static final int BC1_DXT1_BLOCK_SIZE_BYTES = 8;
    public static final int BC2_DXT3_BLOCK_SIZE_BYTES = 16;
    public static final int BC3_DXT5_BLOCK_SIZE_BYTES = 16;
    public static final int BC4_BLOCK_SIZE_BYTES = 8;


    private static final BcHelper instance = new BcHelper();

    private BcHelper() {
    }

    static BcHelper helper() {
        return instance;
    }

    /*
     * DXT/BC Decompression methods
     * Based on https://bitbucket.org/Anteru/dxt-decompress/overview and
     * https://msdn.microsoft.com/en-us/library/windows/desktop/bb694531%28v=vs.85%29.aspx
     *
     * RGB 565 to RGB8
     * https://msdn.microsoft.com/en-us/library/windows/desktop/dd390989%28v=vs.85%29.aspx
     */

    private int getInt(byte[] data, int offset) {
        return getShort(data, offset) << 16 & 0xFFFF0000 |
                getShort(data, offset + 2) & 0x0000FFFF;
    }

    private int getShort(byte[] data, int offset) {
        return data[offset + 1] << 8 & 0x0000FF00 |
                data[offset] & 0x000000FF;
    }

    int[] decompressBc1Block(byte[] data) {
        return decompressBc1Block(data, 0);
    }

    int[] decompressBc1Block(byte[] data, int offset) {
        int[] ret = new int[RET_SIZE];
        decompressBc1Block(data, offset, ret);
        return ret;
    }

    void decompressBc1Block(byte[] data, int[] ret) {
        decompressBc1Block(data, 0, ret);
    }

    void decompressBc1Block(byte[] data, int offset, int[] ret) {
        decompressBc1Block(data, offset, ret, 0);
    }

    void decompressBc1Block(byte[] data, int[] ret, int retOffset) {
        decompressBc1Block(data, 0, ret, retOffset);
    }

    void decompressBc1Block(byte[] data, int offset, int[] ret, int retOffset) {
        //  DXT1/BC1 blocks are 8 bytes long
        //  Decompressed block is 4x4 pixels

        //  Read the endpoint colors
        int i = offset;
        int rawColor0 = getShort(data, i);
        i += 2;
        int rawColor1 = getShort(data, i);
        i += 2;
        int r0, g0, b0, r1, g1, b1;
        //  Extract each component
        r0 = (rawColor0 & RGB565_RED_MASK) >> 11;
        g0 = (rawColor0 & RGB565_GREEN_MASK) >> 5;
        b0 = (rawColor0 & RGB565_BLUE_MASK);
        r1 = (rawColor1 & RGB565_RED_MASK) >> 11;
        g1 = (rawColor1 & RGB565_GREEN_MASK) >> 5;
        b1 = (rawColor1 & RGB565_BLUE_MASK);
        //  Rescale from 5/6 bit to 8 bit
        r0 = (r0 * 0x100 / 0b100000) & 0xFF;
        r1 = (r1 * 0x100 / 0b100000) & 0xFF;
        g0 = (g0 * 0x100 / 0b1000000) & 0xFF;
        g1 = (g1 * 0x100 / 0b1000000) & 0xFF;
        b0 = (b0 * 0x100 / 0b100000) & 0xFF;
        b1 = (b1 * 0x100 / 0b100000) & 0xFF;
        //  Construct ARGB colors
        int[] colors = new int[4];
        //  Don't need to mask because xn do not have any high bits set
        colors[0] = 0xFF000000 | r0 << 16 | g0 << 8 | b0;
        colors[1] = 0xFF000000 | r1 << 16 | g1 << 8 | b1;
        if (rawColor0 < rawColor1) {
            //  c2 = (c0+c1)/2
            colors[2] = 0xFF000000 | ((r0 + r1) / 2) << 16 | ((g0 + g1) / 2) << 8 | ((b0 + b1) / 2);
            //  c3 = transparent
            colors[3] = 0;
        } else {
            //  c2 = (2c0+c1)/3
            colors[2] = 0xFF000000 | ((2 * r0 + r1) / 3) << 16 | ((2 * g0 + g1) / 3) << 8 | ((2 * b0 + b1) / 3);
            //  c3 = (c0+2c1)/3
            colors[3] = 0xFF000000 | ((r0 + 2 * r1) / 3) << 16 | ((g0 + 2 * g1) / 3) << 8 | ((b0 + 2 * b1) / 3);
        }
        for (int row = 0; row < 4; row++, i++) {
            int rowByte = data[i];
            //  Pixels are laid out with MSBit on left, so the top left pixel is the highest 2 bits in the first byte
            for (int col = 3; col >= 0; col--) {
                ret[retOffset + col + row * 4] = colors[rowByte >> 2 * col & 0b11];
            }
        }
    }

    int[] decompressBc2Block(byte[] data) {
        return decompressBc2Block(data, 0);
    }

    int[] decompressBc2Block(byte[] data, int offset) {
        int[] ret = new int[RET_SIZE];
        decompressBc2Block(data, offset, ret);
        return ret;
    }

    void decompressBc2Block(byte[] data, int[] ret) {
        decompressBc2Block(data, 0, ret);
    }

    void decompressBc2Block(byte[] data, int offset, int[] ret) {
        decompressBc2Block(data, offset, ret, 0);
    }

    void decompressBc2Block(byte[] data, int[] ret, int retOffset) {
        decompressBc2Block(data, 0, ret, retOffset);
    }

    void decompressBc2Block(byte[] data, int offset, int[] ret, int retOffset) {
        //  Decompress the BC1 block that occurs after the 8 byte BC2 alpha section
        decompressBc1Block(data, offset + 8, ret, retOffset);
        //  BC2 alpha block uses 4 bit literal alpha values stored pixel-by-pixel
        for (int row = 0; row < 4; row++) {
            int rowShort = getShort(data, offset + row * 2);
            for (int col = 3; col >= 0; col--) {
                //  Wizardry
                //  Sets the pixel at (row, col) to have the correct alpha value extracted from the BC2 alpha block
                ret[retOffset + col + row * 4] = ret[retOffset + col + row * 4] & 0x00FFFFFF |
                        (rowShort >> 4 * col & 0xF) << 28;
            }
        }
    }

    int[] decompressBc3Block(byte[] data) {
        return decompressBc3Block(data, 0);
    }

    int[] decompressBc3Block(byte[] data, int offset) {
        int[] ret = new int[RET_SIZE];
        decompressBc3Block(data, offset, ret);
        return ret;
    }

    void decompressBc3Block(byte[] data, int[] ret) {
        decompressBc3Block(data, 0, ret);
    }

    void decompressBc3Block(byte[] data, int offset, int[] ret) {
        decompressBc3Block(data, offset, ret, 0);
    }

    void decompressBc3Block(byte[] data, int[] ret, int retOffset) {
        decompressBc3Block(data, 0, ret, retOffset);
    }

    void decompressBc3Block(byte[] data, int offset, int[] ret, int retOffset) {
        //  Decompress the BC1 block that occurs after the 8 byte BC2 alpha section
        decompressBc1Block(data, offset + 8, ret, retOffset);
        //  BC3 alpha block uses interpolated alpha values between alpha0 and alpha1
        int[] alpha = new int[8];
        int i = offset;
        int a0 = alpha[0] = data[i++] & 0xFF;
        int a1 = alpha[1] = data[i++] & 0xFF;
        if (a0 > a1) {
            alpha[2] = 6 * a0 / 7 + a1 / 7;
            alpha[3] = 5 * a0 / 7 + 2 * a1 / 7;
            alpha[4] = 4 * a0 / 7 + 3 * a1 / 7;
            alpha[5] = 3 * a0 / 7 + 4 * a1 / 7;
            alpha[6] = 2 * a0 / 7 + 5 * a1 / 7;
            alpha[7] = a0 / 7 + 6 * a1 / 7;
        } else {
            alpha[2] = 4 * a0 / 5 + a1 / 5;
            alpha[3] = 3 * a0 / 5 + 2 * a1 / 5;
            alpha[4] = 2 * a0 / 5 + 3 * a1 / 5;
            alpha[5] = a0 / 5 + 4 * a1 / 5;
            alpha[6] = 0;
            alpha[7] = 0xFF;
        }
        int[] bits = convert16x3(data, i);
        for (int j = 0; j < RET_SIZE; j++) {
            ret[retOffset + j] = ret[retOffset + j] & 0x00FFFFFF | alpha[bits[j]] << 24 & 0xFF000000;
        }
    }


    private int[] convert16x3(byte[] data, int offset) {
        int row1 = (data[offset + 2] << 16) & 0xFF0000 |
                (data[offset + 1] << 8) & 0x00FF00 |
                data[offset] & 0x0000FF;
        int row2 = (data[offset + 5] << 16) & 0xFF0000 |
                (data[offset + 4] << 8) & 0x00FF00 |
                data[offset + 3] & 0x0000FF;
        int[] ret = new int[RET_SIZE];
        for (int i = 7; i >= 0; i--) {
            ret[i] = row1 >> 3 * i & 0b111;
            ret[i + 8] = row2 >> 3 * i & 0b111;
        }
        return ret;
    }

}
