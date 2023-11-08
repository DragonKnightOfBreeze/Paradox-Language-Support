package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.*;
import co.phoenixlab.dds.decoder.dxt.*;

import java.util.*;

public class Decoders {

    public static FormatDecoder getDecoder(Dds dds) {
        DdsPixelFormat pixelFormat = dds.getHeader().getDdspf();
        Set<DdsPixelFormat.Flags> flags = pixelFormat.getDwFlags();
        if (flags.contains(DdsPixelFormat.Flags.DDPF_FOURCC)) {
            return fourCCDecoder(dds);
        }
        if (flags.contains(DdsPixelFormat.Flags.DDPF_RGB)) {
            if (flags.contains(DdsPixelFormat.Flags.DDPF_ALPHAPIXELS)) {
                return new RGBADecoder(dds);
            } else {
                return new RGBDecoder(dds);
            }
        }
        if (flags.contains(DdsPixelFormat.Flags.DDPF_ALPHA)) {
            return new AlphaDecoder(dds);
        }
        if (flags.contains(DdsPixelFormat.Flags.DDPF_LUMINANCE)) {
            return new LumDecoder(dds);
        }
        if (flags.contains(DdsPixelFormat.Flags.DDPF_YUV)) {
            return new YUVDecoder(dds);
        }
        throw new IllegalArgumentException("The provided DDS file has invalid Flags: " + flags.toString());
    }

    private static FormatDecoder fourCCDecoder(Dds dds) {
        DdsPixelFormat pixelFormat = dds.getHeader().getDdspf();
        String dwFourCC = pixelFormat.getDwFourCCAsString();
        switch (dwFourCC) {
            case "\0\0\0\0": {
                throw new IllegalArgumentException("The provided DDS file has DDPF_FOURCC flag set but no dwFourCC");
            }
            case "DXT1": {
                return new Dxt1Decoder(dds);
            }
            case "DXT3": {
                return new Dxt3Decoder(dds);
            }
            case "DXT5": {
                return new Dxt5Decoder(dds);
            }
            default: {
                throw new UnsupportedOperationException("No decoder found for " + dwFourCC +
                        " (0x" + Integer.toHexString(pixelFormat.getDwFourCCAsInt()).toUpperCase() + ")");
            }
        }
    }

}
