package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.*;

import java.nio.*;
import java.util.*;
import java.util.stream.*;

public abstract class AbstractBasicDecoder implements FormatDecoder {

    protected Dds dds;
    protected int currLine;
    protected final int numLines;
    protected final int rawLineByteWidth;
    protected final ByteBuffer rawLineCache;
    protected final IntBuffer intCacheView;
    protected final int lineWidth;
    protected int arrayPos;

    public AbstractBasicDecoder(Dds dds) {
        this.dds = dds;
        this.currLine = 0;
        DdsHeader header = dds.getHeader();
        this.numLines = header.getDwHeight();
        this.lineWidth = header.getDwWidth();
        this.rawLineByteWidth = header.getDdspf().getDwRGBBitCount() / 8 * header.getDwWidth();
        this.rawLineCache = ByteBuffer.allocate(rawLineByteWidth);
        this.rawLineCache.order(ByteOrder.LITTLE_ENDIAN);
        this.intCacheView = rawLineCache.asIntBuffer();
        this.arrayPos = 0;
    }

    protected void loadNextLineIntoCache() {
        rawLineCache.rewind();
        rawLineCache.put(dds.getBdata(), arrayPos, rawLineByteWidth);
        rawLineCache.flip();
        intCacheView.rewind();
        arrayPos += rawLineByteWidth;
    }

    public Stream<int[]> lineStream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Spliterator<int[]> spliterator() {
        return Spliterators.spliterator(lineIterator(), numLines,
                Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    public Iterator<int[]> lineIterator() {
        return new LineIterator();
    }

    class LineIterator implements Iterator<int[]> {
        public boolean hasNext() {
            return currLine < numLines;
        }

        public int[] next() {
            return decodeLine();
        }
    }
}
