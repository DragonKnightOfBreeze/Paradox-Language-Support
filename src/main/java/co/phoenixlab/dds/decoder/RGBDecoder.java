package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.*;

import static java.lang.Integer.numberOfTrailingZeros;

public class RGBDecoder extends AbstractBasicDecoder {

    private final int rShift;
    private final int rMask;
    private final int gShift;
    private final int gMask;
    private final int bShift;
    private final int bMask;

    private final boolean nativeRGB;

    private final int width;

    public RGBDecoder(Dds dds) {
        super(dds);
        DdsPixelFormat pixelFormat = dds.getHeader().getDdspf();
        rMask = pixelFormat.getDwRBitMask();
        gMask = pixelFormat.getDwGBitMask();
        bMask = pixelFormat.getDwBBitMask();
        rShift = numberOfTrailingZeros(rMask);
        gShift = numberOfTrailingZeros(gMask);
        bShift = numberOfTrailingZeros(bMask);
        width = pixelFormat.getDwRGBBitCount();
        //  Optimize if the DDS is already in ARGB form
        nativeRGB = (rMask == 0x00FF0000) &&
            (gMask == 0x0000FF00) &&
            (bMask == 0x000000FF);
    }

    @Override
    public int[] decodeLine() {
        if (currLine >= numLines) {
            return null;
        }
        loadNextLineIntoCache();
        int[] ret = new int[lineWidth];
        if (width == 24) {
            byte[] scratch = new byte[3];
            if (nativeRGB) {
                for (int i = 0; i < lineWidth; i++) {
                    ret[i] = readThreeByteInt(scratch);
                }
            } else {
                int l;
                for (int i = 0; i < lineWidth; i++) {
                    l = readThreeByteInt(scratch);
                    ret[i] = ((l & rMask) >> rShift & 0xFF) << 16 |
                        ((l & gMask) >> gShift & 0xFF) << 8 |
                        ((l & bMask) >> bShift & 0xFF);
                }
            }
            ++currLine;
            return ret;
        } else {
            throw new UnsupportedOperationException("Unsupported bit width " + width);
        }
    }

    private int readThreeByteInt(byte[] scratch) {
        rawLineCache.get(scratch);
        return 0xFF000000 | ((int) scratch[2] & 0xFF) << 16 | ((int) scratch[1] & 0xFF) << 8 | (int) scratch[0] & 0xFF;
    }
}
