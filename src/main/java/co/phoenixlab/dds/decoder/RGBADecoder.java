package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.Dds;
import co.phoenixlab.dds.DdsPixelFormat;

import static java.lang.Integer.numberOfTrailingZeros;

public class RGBADecoder extends AbstractBasicDecoder {

    private final int rShift;
    private final int rMask;
    private final int gShift;
    private final int gMask;
    private final int bShift;
    private final int bMask;
    private final int aShift;
    private final int aMask;

    private final boolean nativeARGB;

    public RGBADecoder(Dds dds) {
        super(dds);
        DdsPixelFormat pixelFormat = dds.getHeader().getDdspf();
        rMask = pixelFormat.getDwRBitMask();
        gMask = pixelFormat.getDwGBitMask();
        bMask = pixelFormat.getDwBBitMask();
        aMask = pixelFormat.getDwABitMask();
        rShift = numberOfTrailingZeros(rMask);
        gShift = numberOfTrailingZeros(gMask);
        bShift = numberOfTrailingZeros(bMask);
        aShift = numberOfTrailingZeros(aMask);
        //  Optimize if the DDS is already in ARGB form
        nativeARGB = (rMask == 0x00FF0000) &&
                (gMask == 0x0000FF00) &&
                (bMask == 0x000000FF) &&
                (aMask == 0xFF000000);
    }

    public int[] decodeLine() {
        if (currLine >= numLines) {
            return null;
        }
        loadNextLineIntoCache();
        int[] ret = new int[lineWidth];
        if (nativeARGB) {
            intCacheView.get(ret);
        } else {
            int l;
            for (int i = 0; i < lineWidth; i++) {
                l = intCacheView.get();
                ret[i] = ((l & aMask) >> aShift & 0xFF) << 24 |
                        ((l & rMask) >> rShift & 0xFF) << 16 |
                        ((l & gMask) >> gShift & 0xFF) << 8 |
                        ((l & bMask) >> bShift & 0xFF);
            }
        }
        ++currLine;
        return ret;
    }
}
