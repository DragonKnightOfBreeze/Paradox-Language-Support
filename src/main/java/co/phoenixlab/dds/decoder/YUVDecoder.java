package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.Dds;

public class YUVDecoder extends AbstractBasicDecoder {

    public YUVDecoder(Dds dds) {
        super(dds);
    }

    @Override
    public int[] decodeLine() {
        throw new UnsupportedOperationException();
    }
}
