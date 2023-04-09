package co.phoenixlab.dds;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.EnumSet;
import java.util.Set;

import static co.phoenixlab.dds.InternalUtils.verifyThat;
import static co.phoenixlab.dds.InternalUtils.verifyThatNot;
import static java.lang.Integer.reverseBytes;
import static java.lang.Integer.toHexString;

public class Dds implements DdsReadable {

    public static final int DW_MAGIC = reverseBytes(0x44445320);    //  'D' 'D' 'S' ' '

    private int dwMagic;
    private DdsHeader header;
    private DdsHeaderDxt10 header10;
    private byte[] bdata;
    private byte[] bdata2;

    public Dds() {
    }

    public int getDwMagic() {
        return dwMagic;
    }

    public DdsHeader getHeader() {
        return header;
    }

    public DdsHeaderDxt10 getHeader10() {
        return header10;
    }

    public byte[] getBdata() {
        return bdata;
    }

    public byte[] getBdata2() {
        return bdata2;
    }

    @Override
    public void validate() throws InvalidDdsException {
        verifyThat(dwMagic, "Invalid DDS: dwMagic not 'DDS '", i -> i == DW_MAGIC);
        verifyThatNot(header, "Invalid DDS: No header", h -> h == null);
        header.validate();
        verifyThat(header10, "Invalid DDS: " +
                "DDSPF indicates presence of DX10Header, but no DX10Header is present",
            h -> header.getDdspf().isDx10HeaderPresent() == (h != null));
        if (header10 != null) {
            header10.validate();
        }
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException {
        dwMagic = reverseBytes(inputStream.readInt());
        header = new DdsHeader();
        header.read(inputStream);
        if (header.getDdspf().isDx10HeaderPresent()) {
            header10 = new DdsHeaderDxt10();
            header10.read(inputStream);
        }
        if (header.getDwFlags().contains(DdsHeader.Flags.DDSD_LINEARSIZE)) {
            bdata = new byte[calculateCompressedDataSize()];
        } else {
            bdata = new byte[calculateDataSize()];
        }
        inputStream.readFully(bdata);
        validate();
    }

    @Override
    public void read(ReadableByteChannel byteChannel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        int read;
        //noinspection StatementWithEmptyBody
        while ((read = byteChannel.read(buf)) > 0);
        if (read < 0) {
            throw new EOFException();
        }
        buf.flip();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        dwMagic = buf.getInt();
        header = new DdsHeader();
        header.read(byteChannel);
        if (header.getDdspf().isDx10HeaderPresent()) {
            header10 = new DdsHeaderDxt10();
            header10.read(byteChannel);
        }
        int sz = Math.max(calculateCompressedDataSize(), calculateDataSize());
        buf = ByteBuffer.allocate(sz);
        bdata = new byte[sz];
        //noinspection StatementWithEmptyBody
        while ((read = byteChannel.read(buf)) > 0);
        if (read < 0) {
            throw new EOFException();
        }
        buf.flip();
        buf.get(bdata);
        validate();
    }

    @Override
    public void read(ByteBuffer buf) throws InvalidDdsException {
        dwMagic = buf.getInt();
        header = new DdsHeader();
        header.read(buf);
        if (header.getDdspf().isDx10HeaderPresent()) {
            header10 = new DdsHeaderDxt10();
            header10.read(buf);
        }
        if (header.getDwFlags().contains(DdsHeader.Flags.DDSD_LINEARSIZE)) {
            bdata = new byte[calculateCompressedDataSize()];
        } else {
            bdata = new byte[calculateDataSize()];
        }
        buf.get(bdata);
        validate();
    }

    private int calculateCompressedDataSize() {
        //  Turns out the pitchOrLinearSize field is actually unreliable
        //  So we have to calculate out the size ourselves
        DdsPixelFormat pixelFormat = getHeader().getDdspf();
        Set<DdsPixelFormat.Flags> flags = pixelFormat.getDwFlags();
        if (flags.contains(DdsPixelFormat.Flags.DDPF_FOURCC)) {
            int blockSize = getBlockSize(pixelFormat.getDwFourCCAsString());
            //  Blocks are 4x4, so determine the dimension in blocks
            int w = getHeader().getDwWidth();
            int h = getHeader().getDwHeight();
            //  Since integer division floors, we must round up to the nearest multiple of 4
            int blockWidth = (w + 3) / 4;
            int blockHeight = (h + 3) / 4;
            return blockWidth * blockHeight * blockSize;
        }
        //  For now fall back on what they give us
        //  TODO handle the odd corner cases
        return getHeader().getDwPitchOrLinearSize();
    }

    private int getBlockSize(String dwFourCC) {
        switch (dwFourCC) {
            case "\0\0\0\0": {
                throw new IllegalArgumentException("The provided DDS file has DDPF_FOURCC flag set " +
                        "but no dwFourCC");
            }
            case "DXT1": {
                return 8;
            }
            default: {
                return 16;
            }
        }
    }


    private int calculateDataSize() {
        int numPixels = header.getDwWidth() * header.getDwHeight();
        Set<DdsPixelFormat.Flags> flags = header.getDdspf().getDwFlags();
        if (flags.contains(DdsPixelFormat.Flags.DDPF_FOURCC)) {
            return calculateCompressedDataSize();
        }
        int bytesPerPixel = 0;
        if (flags.contains(DdsPixelFormat.Flags.DDPF_ALPHA)) {
            bytesPerPixel = 1;
        }
        boolean hasAlpha = flags.contains(DdsPixelFormat.Flags.DDPF_ALPHAPIXELS);
        Set<DdsPixelFormat.Flags> rgbBitCountValid = EnumSet.of(DdsPixelFormat.Flags.DDPF_LUMINANCE,
                DdsPixelFormat.Flags.DDPF_RGB, DdsPixelFormat.Flags.DDPF_YUV);
        rgbBitCountValid.retainAll(flags);
        if (!rgbBitCountValid.isEmpty()) {
            bytesPerPixel = header.getDdspf().getDwRGBBitCount() / 8;
        }
        return numPixels * bytesPerPixel;
    }

    @Override
    public String toString() {
        return "Dds{" +
                "dwMagic=" + toHexString(dwMagic) +
                ", header=" + header +
                ", header10=" + (header10 == null ? "N/A" : header10) +
                ", bdataSize=" + (bdata == null ? "N/A" : bdata.length) +
                ", bdata2Size=" + (bdata2 == null ? "N/A" : bdata2.length) +
                '}';
    }
}
