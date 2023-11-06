package co.phoenixlab.dds.decoder;

import co.phoenixlab.dds.*;

public class AlphaDecoder extends AbstractBasicDecoder {
    
    public AlphaDecoder(Dds dds) {
        super(dds);
    }

    @Override
    public int[] decodeLine() {
        throw new UnsupportedOperationException();
    }
}
