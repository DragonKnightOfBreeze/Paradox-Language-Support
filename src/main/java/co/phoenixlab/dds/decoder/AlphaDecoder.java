package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.Dds;

public class AlphaDecoder extends AbstractBasicDecoder {
    
    public AlphaDecoder(Dds dds) {
        super(dds);
    }

    @Override
    public int[] decodeLine() {
        throw new UnsupportedOperationException();
    }
}
