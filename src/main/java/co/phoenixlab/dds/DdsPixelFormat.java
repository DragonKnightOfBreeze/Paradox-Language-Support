package co.phoenixlab.dds;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.function.IntSupplier;

import static co.phoenixlab.dds.InternalUtils.*;
import static java.lang.Integer.reverseBytes;
import static java.lang.Integer.toHexString;

/**
 * Represents a DdsPixelFormat<br/>
 * See the <a href=https://msdn.microsoft.com/en-us/library/windows/desktop/bb943984%28v=vs.85%29.aspx>MSDN</a>
 * reference
 */
public class DdsPixelFormat implements DdsReadable {

    /**
     * Size of the pixelformat ({@value} bytes).
     */
    public static final int DW_SIZE = 32;
    /**
     * Size of the dwFourCC field ({@value} bytes).
     */
    public static final int DW_FOUR_CC_SIZE = 4;
    /**
     * DX10 value for dwFourCC as bytes ('D' 'X' '1' '0')
     */
    public static final byte[] DXGI_DX10_DW_FOUR_CC = {0x44, 0x58, 0x31, 0x30}; //  'D' 'X' '1' '0'
    /**
     * DX10 value for dwFourCC as an int ('D' 'X' '1' '0' -> 0x44583130)
     */
    public static final int DXGI_DX10_DW_FOUR_CC_INT = 0x44583130;

    /**
     * Structure size
     */
    private int dwSize;
    /**
     * Values which indicate what type of data is in the surface
     */
    private Set<Flags> dwFlags;
    /**
     * Four-character codes for specifiying compressed or custom formats. Possible values include: DXT1, DXT2, DXT3,
     * DXT4, or DXT5. A FourCC of DX10 indicates the presence of the {@link DdsHeaderDxt10} extended header.
     */
    private byte[] dwFourCC;
    /**
     * Number of bits in an RGB (possibly alpha) format. Valid when dwFlags includes
     * {@link Flags#DDPF_RGB},
     * {@link Flags#DDPF_LUMINANCE}, or
     * {@link Flags#DDPF_YUV}.
     */
    private int dwRGBBitCount;
    /**
     * Red (or luminance or Y) mask for reading color data
     */
    private int dwRBitMask;
    /**
     * Green (or U) mask for reading color data
     */
    private int dwGBitMask;
    /**
     * Blue (or V) mask for reading color data
     */
    private int dwBBitMask;
    /**
     * Alpha mask for reading alpha data. Valid when dwFlags includes
     * {@link Flags#DDPF_ALPHAPIXELS} or
     * {@link Flags#DDPF_ALPHA}.
     */
    private int dwABitMask;

    public DdsPixelFormat() {}

    /**
     * @return {@link #dwSize}
     */
    public int getDwSize() {
        return dwSize;
    }

    /**
     * @return {@link #dwFlags}
     */
    public Set<Flags> getDwFlags() {
        return dwFlags;
    }

    /**
     * @return {@link #dwFourCC}
     */
    public byte[] getDwFourCC() {
        return dwFourCC;
    }

    /**
     * @return {@link #dwFourCC} as an integer, first byte = highest byte, last byte = lowest byte
     */
    public int getDwFourCCAsInt() {
        return ((dwFourCC[0] << 24) & 0xFF000000) |
                ((dwFourCC[1] << 16) & 0xFF0000) |
                ((dwFourCC[2] << 8) & 0xFF00) |
                ((dwFourCC[3]) & 0xFF);
    }

    /**
     * @return {@link #dwFourCC} as a String
     */
    public String getDwFourCCAsString() {
        return new String(dwFourCC);
    }

    /**
     * @return {@link #dwRGBBitCount}
     */
    public int getDwRGBBitCount() {
        return dwRGBBitCount;
    }

    /**
     * @return {@link #dwRBitMask}
     */
    public int getDwRBitMask() {
        return dwRBitMask;
    }

    /**
     * @return {@link #dwGBitMask}
     */
    public int getDwGBitMask() {
        return dwGBitMask;
    }

    /**
     * @return {@link #dwBBitMask}
     */
    public int getDwBBitMask() {
        return dwBBitMask;
    }

    /**
     * @return {@link #dwABitMask}
     */
    public int getDwABitMask() {
        return dwABitMask;
    }

    /**
     * @return Whether or not the DX10 extended header is present in the DDS
     */
    public boolean isDx10HeaderPresent() {
        return dwFlags.contains(Flags.DDPF_FOURCC) && Arrays.equals(dwFourCC, DXGI_DX10_DW_FOUR_CC);
    }

    /**
     * Verifies that:
     * <ul>
     * <li>dwSize is {@value #DW_SIZE} bytes</li>
     * <li>dwFlags is not empty</li>
     * </ul>
     * @see DdsReadable#validate()
     */
    @Override
    public void validate() throws InvalidDdsException {
        verifyThat(dwSize, "Invalid DDSPixelFormat: dwSize not " + DW_SIZE, i -> i == DW_SIZE);
        verifyThatNot(dwFlags, "Invalid DDSPixelFormat: dwFlags cannot be empty", Set::isEmpty);
    }

    public void read(DataInputStream inputStream) throws IOException {
        dwSize = reverseBytes(inputStream.readInt());
        dwFlags = bitsToUnmodifiableSet(reverseBytes(inputStream.readInt()), Flags.class);
        dwFourCC = new byte[DW_FOUR_CC_SIZE];
        inputStream.readFully(dwFourCC);
        dwRGBBitCount = reverseBytes(inputStream.readInt());
        dwRBitMask = reverseBytes(inputStream.readInt());
        dwGBitMask = reverseBytes(inputStream.readInt());
        dwBBitMask = reverseBytes(inputStream.readInt());
        dwABitMask = reverseBytes(inputStream.readInt());
    }

    public void read (ReadableByteChannel byteChannel) throws IOException {
        read(byteChannel, DW_SIZE);
    }

    public void read(ByteBuffer buf) {
        dwSize = buf.getInt();
        dwFlags = bitsToSet(buf.getInt(), Flags.class);
        dwFourCC = new byte[DW_FOUR_CC_SIZE];
        buf.get(dwFourCC);
        dwRGBBitCount = buf.getInt();
        dwRBitMask = buf.getInt();
        dwGBitMask = buf.getInt();
        dwBBitMask = buf.getInt();
        dwABitMask = buf.getInt();
    }

    @Override
    public String toString() {
        return "DdsPixelFormat{" +
                "dwSize=" + dwSize +
                ", dwFlags=" + dwFlags +
                ", dwFourCC=\"" + new String(dwFourCC) + "\"" +
                ", dwRGBBitCount=" + dwRGBBitCount +
                ", dwRBitMask=" + toHexString(dwRBitMask) +
                ", dwGBitMask=" + toHexString(dwGBitMask) +
                ", dwBBitMask=" + toHexString(dwBBitMask) +
                ", dwABitMask=" + toHexString(dwABitMask) +
                '}';
    }

    /**
     * Values which indicate what type of data is in the surface
     */
    public enum Flags implements IntSupplier {
        /**
         * Texture contains alpha data; {@link #dwABitMask} contains valid data
         * <p>
         * {@code bitmask = 0x1}
         */
        DDPF_ALPHAPIXELS(0x1),
        /**
         * Used in some older DDS files for alpha channel only uncompressed data; {@link #dwABitMask} contains valid
         * data
         * <p>
         * {@code bitmask = 0x2}
         */
        DDPF_ALPHA(0x2),
        /**
         * Texture contains compressed RGB data; {@link #dwFourCC} contains valid data
         * <p>
         * {@code bitmask = 0x4}
         */
        DDPF_FOURCC(0x4),
        /**
         * Texture contains uncompressed RGB data; {@link #dwRGBBitCount} and the RGB masks (@link #dwRBitMask},
         * {@link #dwGBitMask}, {@link #dwBBitMask}) contain valid data
         * <p>
         * {@code bitmask = 0x40}
         */
        DDPF_RGB(0x40),
        /**
         * Used in some older DDS files for YUV uncompressed data ({@link #dwRGBBitCount} contains the YUV bit count,
         * {@link #dwRBitMask} is the Y mask, {@link #dwGBitMask} U, {@link #dwBBitMask} V
         * <p>
         * {@code bitmask = 0x200}
         */
        DDPF_YUV(0x200),
        /**
         * Used in some older DDS files for single channel color uncompressed data ({@link #dwRGBBitCount} contains the
         * luminance channel bit count; {@link #dwRBitMask} contains the channel mask). Can be combined with
         * {@link #DDPF_ALPHAPIXELS} for a two-channel DDS file
         * <p>
         * {@code bitmask = 0x20000}
         */
        DDPF_LUMINANCE(0x20000);

        public final int bits;

        Flags(int bits) {
            this.bits = bits;
        }

        public int getAsInt() {
            return bits;
        }
    }
}
