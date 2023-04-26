package co.phoenixlab.dds;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import co.phoenixlab.dds.decoder.Decoders;
import co.phoenixlab.dds.decoder.FormatDecoder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class DdsImageDecoder {

    private static final Map<String, IntUnaryOperator> SWIZZLER_CACHE = new HashMap<>();

    private static final IntUnaryOperator GET_RED = i -> i >> 16 & 255;
    private static final IntUnaryOperator GET_GREEN = i -> i >> 8 & 255;
    private static final IntUnaryOperator GET_BLUE = i -> i & 255;
    private static final IntUnaryOperator GET_ALPHA = i -> i >> 24 & 255;
    private static final IntUnaryOperator GET_MIN = i -> 0;
    private static final IntUnaryOperator GET_MAX = i -> 255;
    private static final IntUnaryOperator NEGATE = i -> 255 - i;

    private static final IntUnaryOperator SET_RED = i -> (i & 255) << 16;
    private static final IntUnaryOperator SET_GREEN = i -> (i & 255) << 8;
    private static final IntUnaryOperator SET_BLUE = i -> i & 255;
    private static final IntUnaryOperator SET_ALPHA = i -> (i & 255) << 24;

    private static final IntUnaryOperator[] SETTERS = new IntUnaryOperator[]{
        SET_RED,
        SET_GREEN,
        SET_BLUE,
        SET_ALPHA
    };

    public DdsImageDecoder() {

    }

    private static IntUnaryOperator buildSwizzler(String swizzle) {
        if (swizzle.length() != 4) {
            if (swizzle.length() == 3) {
                //  Assume alpha of 1 if not specified
                swizzle = swizzle + "1";
            } else {
                throw new IllegalArgumentException("Invalid swizzle string (bad length)");
            }
        }
        char[] chars = swizzle.toCharArray();

        IntUnaryOperator[] ops = new IntUnaryOperator[4];

        for (int i = 0; i < 4; i++) {
            ops[i] = getSourceOp(chars[i]).andThen(SETTERS[i]);
        }

        return i -> {
            int ret = 0;
            ret |= ops[0].applyAsInt(i);
            ret |= ops[1].applyAsInt(i);
            ret |= ops[2].applyAsInt(i);
            ret |= ops[3].applyAsInt(i);
            return ret;
        };
    }

    private static IntUnaryOperator getSourceOp(char c) {
        switch (c) {
            case 'r':
            case 'x':
                return GET_RED;
            case 'R':
            case 'X':
                return GET_RED.andThen(NEGATE);
            case 'g':
            case 'y':
                return GET_GREEN;
            case 'G':
            case 'Y':
                return GET_GREEN.andThen(NEGATE);
            case 'b':
            case 'z':
                return GET_BLUE;
            case 'B':
            case 'Z':
                return GET_BLUE.andThen(NEGATE);
            case 'a':
            case 'w':
                return GET_ALPHA;
            case 'A':
            case 'W':
                return GET_ALPHA.andThen(NEGATE);
            case '0':
                return GET_MIN;
            case '1':
                return GET_MAX;
            default:
                throw new IllegalArgumentException("Invalid swizzle value");
        }
    }

    public byte[] convertToRawARGB8(Dds dds) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            convertToRawARGB8(dds, byteArrayOutputStream);
        } catch (IOException e) {
            //  Impossible
        }

        return byteArrayOutputStream.toByteArray();
    }

    public void convertToRawARGB8(Dds dds, OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream;
        if (outputStream instanceof DataOutputStream) {
            dataOutputStream = (DataOutputStream) outputStream;
        } else {
            dataOutputStream = new DataOutputStream(outputStream);
        }

        FormatDecoder decoder = Decoders.getDecoder(dds);
        for (int[] ints : decoder) {
            for (int pixel : ints) {
                dataOutputStream.writeInt(pixel);
            }
        }

        outputStream.flush();
    }

    public byte[] convertToPNG(Dds dds) {
        return convertToPNG(dds, "");
    }

    public byte[] convertToPNG(Dds dds, String swizzle) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            convertToPNG(dds, byteArrayOutputStream, swizzle);
        } catch (IOException e) {
            //  Impossible
        }

        return byteArrayOutputStream.toByteArray();
    }

    public byte[] convertToPNG(Dds dds, int frame) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            convertToPNG(dds, byteArrayOutputStream, frame);
        } catch (IOException e) {
            //  Impossible
        }

        return byteArrayOutputStream.toByteArray();
    }

    public void convertToPNG(Dds dds, OutputStream outputStream) throws IOException {
        convertToPNG(dds, outputStream, "");
    }

    public void convertToPNG(Dds dds, OutputStream outputStream, String swizzle) throws IOException {
        DdsHeader header = dds.getHeader();
        FormatDecoder decoder = Decoders.getDecoder(dds);
        ImageInfo imageInfo = new ImageInfo(header.getDwWidth(), header.getDwHeight(), 8, true);
        PngWriter pngWriter = new PngWriter(outputStream, imageInfo);
        ImageLineInt imageLine = new ImageLineInt(imageInfo);
        for (int[] ints : decoder) {
            swizzle(ints, swizzle);
            ImageLineHelper.setPixelsRGBA8(imageLine, ints);
            pngWriter.writeRow(imageLine);
        }

        pngWriter.end();
    }

    public void convertToPNG(Dds dds, OutputStream outputStream, int frame) throws IOException {
        convertToPNG(dds, outputStream, frame, "");
    }

    public void convertToPNG(Dds dds, OutputStream outputStream, int frame, String swizzle) throws IOException {
        if(frame <= 0) throw new IllegalArgumentException();
        DdsHeader header = dds.getHeader();
        FormatDecoder decoder = Decoders.getDecoder(dds);
        int width = header.getDwWidth();
        int height = header.getDwHeight();
        int frames = width / height;
        ImageInfo imageInfo = new ImageInfo(height, height, 8, true);
        PngWriter pngWriter = new PngWriter(outputStream, imageInfo);
        ImageLineInt imageLine = new ImageLineInt(imageInfo);
        for (int[] ints : decoder) {
            int finalLength = ints.length / frames;
            int[] finalInts = new int[finalLength];
            System.arraycopy(ints,finalLength * (frame -1),finalInts, 0, finalLength);
            swizzle(finalInts, swizzle);
            ImageLineHelper.setPixelsRGBA8(imageLine, finalInts);
            pngWriter.writeRow(imageLine);
        }

        pngWriter.end();
    }

    private void swizzle(int[] ints, String swizzle) {
        if (swizzle.isEmpty()) {
            return;
        }

        //  Build swizzler
        IntUnaryOperator swizzler = SWIZZLER_CACHE.computeIfAbsent(swizzle, DdsImageDecoder::buildSwizzler);

        //  Apply
        for (int i = 0; i < ints.length; i++) {
            ints[i] = swizzler.applyAsInt(ints[i]);
        }
    }

}
