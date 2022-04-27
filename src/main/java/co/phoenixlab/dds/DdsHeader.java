package co.phoenixlab.dds;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntSupplier;

import static co.phoenixlab.dds.InternalUtils.bitsToUnmodifiableSet;
import static co.phoenixlab.dds.InternalUtils.verifyThat;
import static java.lang.Integer.reverseBytes;

/**
 * Represents a DdsHeader<br/>
 * See the <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/bb943982%28v=vs.85%29.aspx">MSDN</a>
 * reference
 */
public class DdsHeader implements DdsReadable {

    /**
     * Size of DDSHeader ({@value} bytes)
     */
    public static final int DW_SIZE = 124;
    /**
     * Number of elements for dwReserved1 ({@value} DWORDs)
     */
    public static final int DW_RESERVED_1_SIZE = 11;
    /**
     * Expected contents of dwReserved
     */
    public static final int[] DW_RESERVED_1 = new int[DW_RESERVED_1_SIZE];

    /**
     * Size of struct
     *
     * @see #DW_SIZE
     */
    private int dwSize;
    /**
     * Flags to indicate which members contain valid data
     *
     * @see Flags
     */
    private Set<Flags> dwFlags;
    /**
     * Surface height (in pixels)
     */
    private int dwHeight;
    /**
     * Surface width (in pixels)
     */
    private int dwWidth;
    /**
     * The pitch or number of bytes per scan line in an uncompressed texture; the total number of bytes in the top
     * level texture for a compressed texture
     */
    private int dwPitchOrLinearSize;
    /**
     * Depth of a volume texture (in pixels)
     */
    private int dwDepth;
    /**
     * Number of mipmap levels
     */
    private int dwMipMapCount;
    /**
     * Unused
     */
    private int[] dwReserved1;
    /**
     * The pixel format
     *
     * @see DdsPixelFormat
     */
    private DdsPixelFormat ddspf;
    /**
     * Specifies the complexity of the surfaces stored
     *
     * @see Caps
     */
    private Set<Caps> dwCaps;
    /**
     * Additional detail about the surfaces stored
     *
     * @see Caps2
     */
    private Set<Caps2> dwCaps2;
    /**
     * Unused
     *
     * @see Caps3
     */
    private Set<Caps3> dwCaps3;
    /**
     * Unused
     *
     * @see Caps4
     */
    private Set<Caps4> dwCaps4;
    /**
     * Unused
     */
    private int dwReserved2;

    public DdsHeader() {
    }

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
     * @return {@link #dwHeight}
     */
    public int getDwHeight() {
        return dwHeight;
    }

    /**
     * @return {@link #dwWidth}
     */
    public int getDwWidth() {
        return dwWidth;
    }

    /**
     * @return {@link #dwPitchOrLinearSize}
     */
    public int getDwPitchOrLinearSize() {
        return dwPitchOrLinearSize;
    }

    /**
     * @return {@link #dwDepth}
     */
    public int getDwDepth() {
        return dwDepth;
    }

    /**
     * @return {@link #dwMipMapCount}
     */
    public int getDwMipMapCount() {
        return dwMipMapCount;
    }

    /**
     * @return {@link #dwReserved1}
     */
    public int[] getDwReserved1() {
        return dwReserved1;
    }

    /**
     * @return {@link #ddspf}
     */
    public DdsPixelFormat getDdspf() {
        return ddspf;
    }

    /**
     * @return {@link #dwCaps}
     */
    public Set<Caps> getDwCaps() {
        return dwCaps;
    }

    /**
     * @return {@link #dwCaps2}
     */
    public Set<Caps2> getDwCaps2() {
        return dwCaps2;
    }

    /**
     * @return {@link #dwCaps3}
     */
    public Set<Caps3> getDwCaps3() {
        return dwCaps3;
    }

    /**
     * @return {@link #dwCaps4}
     */
    public Set<Caps4> getDwCaps4() {
        return dwCaps4;
    }

    /**
     * @return {@link #dwReserved2}
     */
    public int getDwReserved2() {
        return dwReserved2;
    }

    /**
     * Verifies that:
     * <ul>
     * <li>dwSize is {@value #DW_SIZE} bytes</li>
     * <li>dwFlags contains all the required flags ({@link Flags#REQUIRED})</li>
     * <li>dwReserved1 is an array of zeros</li>
     * <li>ddspf is valid ({@link DdsPixelFormat#validate()})</li>
     * <li>dwCaps contains all the required caps ({@link Caps#REQUIRED})</li>
     * <li>dwCaps3 is empty</li>
     * <li>dwCaps4 is empty</li>
     * <li>dwReserved2 is zero</li>
     * </ul>
     *
     * @see DdsReadable#validate()
     */
    public void validate() throws InvalidDdsException {
        verifyThat(dwSize, "Invalid DDSHeader: dwSize not " + DW_SIZE, i -> i == DW_SIZE);
        verifyThat(dwFlags, "Invalid DDSHeader: dwFlags missing required flags", s -> s.containsAll(Flags.REQUIRED));
//        verifyThat(dwReserved1, i -> Arrays.equals(i, DW_RESERVED_1), "Invalid DDSHeader: dwReserved1 not empty");
        ddspf.validate();
        verifyThat(dwCaps, "Invalid DDSHeader: dwCaps missing required caps", s -> s.containsAll(Caps.REQUIRED));
        verifyThat(dwCaps3, "Invalid DDSHeader: dwCaps3 is not empty", Set::isEmpty);
        verifyThat(dwCaps4, "Invalid DDSHeader: dwCaps4 is not empty", Set::isEmpty);
        verifyThat(dwReserved2, "Invalid DDSHeader, dwReserved2 not zero", i -> i == 0);
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException {
        dwSize = reverseBytes(inputStream.readInt());
        dwFlags = bitsToUnmodifiableSet(reverseBytes(inputStream.readInt()), Flags.class);
        dwHeight = reverseBytes(inputStream.readInt());
        dwWidth = reverseBytes(inputStream.readInt());
        dwPitchOrLinearSize = reverseBytes(inputStream.readInt());
        dwDepth = reverseBytes(inputStream.readInt());
        dwMipMapCount = reverseBytes(inputStream.readInt());
        dwReserved1 = new int[DW_RESERVED_1_SIZE];
        for (int i = 0; i < DW_RESERVED_1_SIZE; i++) {
            dwReserved1[i] = reverseBytes(inputStream.readInt());
        }
        ddspf = new DdsPixelFormat();
        ddspf.read(inputStream);
        dwCaps = bitsToUnmodifiableSet(reverseBytes(inputStream.readInt()), Caps.class);
        dwCaps2 = bitsToUnmodifiableSet(reverseBytes(inputStream.readInt()), Caps2.class);
        dwCaps3 = bitsToUnmodifiableSet(reverseBytes(inputStream.readInt()), Caps3.class);
        dwCaps4 = bitsToUnmodifiableSet(reverseBytes(inputStream.readInt()), Caps4.class);
        dwReserved2 = reverseBytes(inputStream.readInt());
        validate();
    }

    @Override
    public void read(ReadableByteChannel byteChannel) throws IOException {
        read(byteChannel, DW_SIZE);
    }

    @Override
    public void read(ByteBuffer buf) throws InvalidDdsException {
        dwSize = buf.getInt();
        dwFlags = bitsToUnmodifiableSet(buf.getInt(), Flags.class);
        dwHeight = buf.getInt();
        dwWidth = buf.getInt();
        dwPitchOrLinearSize = buf.getInt();
        dwDepth = buf.getInt();
        dwMipMapCount = buf.getInt();
        dwReserved1 = new int[DW_RESERVED_1_SIZE];
        for (int i = 0; i < DW_RESERVED_1_SIZE; i++) {
            dwReserved1[i] = buf.getInt();
        }
        ddspf = new DdsPixelFormat();
        ddspf.read(buf);
        dwCaps = bitsToUnmodifiableSet(buf.getInt(), Caps.class);
        dwCaps2 = bitsToUnmodifiableSet(buf.getInt(), Caps2.class);
        dwCaps3 = bitsToUnmodifiableSet(buf.getInt(), Caps3.class);
        dwCaps4 = bitsToUnmodifiableSet(buf.getInt(), Caps4.class);
        dwReserved2 = buf.getInt();
        validate();
    }

    @Override
    public String toString() {
        return "DdsHeader{" +
                "dwSize=" + dwSize +
                ", dwFlags=" + dwFlags.toString() +
                ", dwHeight=" + dwHeight +
                ", dwWidth=" + dwWidth +
                ", dwPitchOrLinearSize=" + dwPitchOrLinearSize +
                ", dwDepth=" + (dwFlags.contains(Flags.DDSD_DEPTH) ? dwDepth : "N/A") +
                ", dwMipMapCount=" + (dwFlags.contains(Flags.DDSD_MIPMAPCOUNT) ? dwMipMapCount : "N/A") +
                ", dwReserved1=" + Arrays.toString(dwReserved1) +
                ", ddspf=" + ddspf.toString() +
                ", dwCaps=" + dwCaps.toString() +
                ", dwCaps2=" + dwCaps2.toString() +
                ", dwCaps3=" + dwCaps3.toString() +
                ", dwCaps4=" + dwCaps4.toString() +
                ", dwReserved2=" + dwReserved2 +
                '}';
    }

    /**
     * Flags to indicate which members contain valid data
     */
    public enum Flags implements IntSupplier {
        /**
         * Required
         * <p>
         * {@code bitmask = 0x1}
         */
        DDSD_CAPS(0x1),
        /**
         * Required
         * <p>
         * {@code bitmask = 0x2}
         */
        DDSD_HEIGHT(0x2),
        /**
         * Required
         * <p>
         * {@code bitmask = 0x4}
         */
        DDSD_WIDTH(0x4),
        /**
         * Required when pitch is provided for an uncompressed texture
         * <p>
         * {@code bitmask = 0x8}
         */
        DDSD_PITCH(0x8),
        /**
         * Required
         * <p>
         * {@code bitmask = 0x1000}
         */
        DDSD_PIXELFORMAT(0x1000),
        /**
         * Required in a mipmapped texture
         * <p>
         * {@code bitmask = 0x2000}
         */
        DDSD_MIPMAPCOUNT(0x20000),
        /**
         * Required whe pitch is provided for a compressed texture
         * <p>
         * {@code bitmask = 0x80000}
         */
        DDSD_LINEARSIZE(0x80000),
        /**
         * Required in a depth texture
         * <p>
         * {@code bitmask = 0x800000}
         */
        DDSD_DEPTH(0x800000);

        final int bits;

        Flags(int bits) {
            this.bits = bits;
        }

        @Override
        public int getAsInt() {
            return bits;
        }

        public static Set<Flags> REQUIRED = EnumSet.of(DDSD_CAPS, DDSD_HEIGHT, DDSD_WIDTH, DDSD_PIXELFORMAT);
    }

    /**
     * Specifies the complexity of the surfaces stored
     */
    public enum Caps implements IntSupplier {
        /**
         * Indicates the presence of more than one surface (a mipmap, cubic environment map, mipmapped volume texture
         * <p>
         * {@code bitmask = 0x8}
         */
        DDSCAPS_COMPLEX(0x8),
        /**
         * Indicates the presence of a mipmap
         * <p>
         * {@code bitmask = 0x400000}
         */
        DDSCAPS_MIPMAP(0x400000),
        /**
         * Indicates the presence of a texture - required
         * <p>
         * {@code bitmask = 0x1000}
         */
        DDSCAPS_TEXTURE(0x1000);

        final int bits;

        Caps(int bits) {
            this.bits = bits;
        }

        @Override
        public int getAsInt() {
            return bits;
        }

        public static Set<Caps> REQUIRED = EnumSet.of(DDSCAPS_TEXTURE);
    }

    /**
     * Additional detail about the surface stored
     */
    public enum Caps2 implements IntSupplier {
        /**
         * Required for a cube map
         * <p>
         * {@code bitmask = 0x200}
         */
        DDSCAPS2_CUBEMAP(0x200),
        /**
         * Required when these surfaces are stored in a cube map
         * <p>
         * {@code bitmask = 0x400}
         */
        DDSCAPS2_CUBEMAP_POSITIVEX(0x400),
        /**
         * Required when these surfaces are stored in a cube map
         * <p>
         * {@code bitmask = 0x800}
         */
        DDSCAPS2_CUBEMAP_NEGATIVEX(0x800),
        /**
         * Required when these surfaces are stored in a cube map
         * <p>
         * {@code bitmask = 0x1000}
         */
        DDSCAPS2_CUBEMAP_POSITIVEY(0x1000),
        /**
         * Required when these surfaces are stored in a cube map
         * <p>
         * {@code bitmask = 0x2000}
         */
        DDSCAPS2_CUBEMAP_NEGATIVEY(0x2000),
        /**
         * Required when these surfaces are stored in a cube map
         * <p>
         * {@code bitmask = 0x4000}
         */
        DDSCAPS2_CUBEMAP_POSITIVEZ(0x4000),
        /**
         * Required when these surfaces are stored in a cube map
         * <p>
         * {@code bitmask = 0x8000}
         */
        DDSCAPS2_CUBEMAP_NEGATIVEZ(0x8000),
        /**
         * Required for a volume texture
         * <p>
         * {@code bitmask = 0x200000}
         */
        DDSCAPS2_VOLUME(0x200000);

        final int bits;

        Caps2(int bits) {
            this.bits = bits;
        }

        @Override
        public int getAsInt() {
            return bits;
        }
    }

    /**
     * Unused
     */
    public enum Caps3 implements IntSupplier {
        ;   //  None

        final int bits;

        Caps3(int bits) {
            this.bits = bits;
        }

        @Override
        public int getAsInt() {
            return bits;
        }
    }

    /**
     * Unused
     */
    public enum Caps4 implements IntSupplier {
        ;   //  None

        final int bits;

        Caps4(int bits) {
            this.bits = bits;
        }

        @Override
        public int getAsInt() {
            return bits;
        }
    }
}
