package co.phoenixlab.dds.decoder.dxt;

import co.phoenixlab.dds.Dds;
import co.phoenixlab.dds.DdsHeader;
import co.phoenixlab.dds.decoder.FormatDecoder;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractDxtDecoder implements FormatDecoder {

    protected Dds dds;
    protected final int blockByteSize;
    protected int currLine;
    protected final int numLines;
    protected final int lineWidth;
    protected int currBlockRow;
    protected final int numBlockRows;
    protected final int blockRowWidth;
    protected final int blockRowBytes;
    protected final int[] decodedBlocks;
    protected final byte[] rawBlockLineCache;
    protected int arrayPos;

    public AbstractDxtDecoder(Dds dds, int blockByteSize) {
        this.dds = dds;
        this.blockByteSize = blockByteSize;
        this.currLine = 0;
        DdsHeader header = dds.getHeader();
        this.numLines = header.getDwHeight();
        this.lineWidth = header.getDwWidth();
        this.currBlockRow = 0;
        this.numBlockRows = (this.numLines + 3) / 4;
        this.blockRowWidth = (this.lineWidth + 3) / 4;
        this.blockRowBytes = this.blockByteSize * this.blockRowWidth;
        this.decodedBlocks = new int[16 * this.blockRowWidth];
        this.rawBlockLineCache = new byte[this.blockRowBytes];
        this.arrayPos = 0;
    }

    protected void loadNextBlockRowIntoCache() {
        System.arraycopy(dds.getBdata(), arrayPos, rawBlockLineCache, 0, blockRowBytes);
        arrayPos += blockRowBytes;
        ++currBlockRow;
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
