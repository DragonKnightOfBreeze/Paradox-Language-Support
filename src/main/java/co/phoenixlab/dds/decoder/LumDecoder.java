package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.*;

public class LumDecoder extends AbstractBasicDecoder {

    public LumDecoder(Dds dds) {
        super(dds);
    }

    @Override
    public int[] decodeLine() {
        throw new UnsupportedOperationException();
    }
}
