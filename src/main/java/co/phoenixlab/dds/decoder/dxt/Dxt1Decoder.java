package co.phoenixlab.dds.decoder.dxt;

import co.phoenixlab.dds.*;

public class Dxt1Decoder extends AbstractDxtDecoder {

    public Dxt1Decoder(Dds dds) {
        super(dds, BcHelper.BC1_DXT1_BLOCK_SIZE_BYTES);
    }

    @Override
    public int[] decodeLine() {
        if (currLine >= numLines) {
            return null;
        }
        int subRow = currLine % 4;
        if (subRow == 0) {
            BcHelper bc = BcHelper.helper();
            loadNextBlockRowIntoCache();
            for (int i = 0; i < blockRowWidth; i++) {
                bc.decompressBc1Block(rawBlockLineCache, i * blockByteSize, decodedBlocks, i * 16);
            }
        }
        int[] ret = new int[lineWidth];
        //  decodedBlocks is stored in 4x4 blocks, [r0, r1, r2, r3], [r0, r1, r2, r3], ...
        //  so to get the nth row from each block, we skip (row * 4) entries, read 4 entries, skip 16 entries,
        // read 4 entries... and so on until we've read enough
        int pos = subRow * 4;
        for (int i = 0; i < blockRowWidth; i++) {
            System.arraycopy(decodedBlocks, pos, ret, i * 4, 4);
            pos += 16;
        }
        ++currLine;
        return ret;
    }
}
