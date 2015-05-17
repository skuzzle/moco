package de.uni.bremen.monty.moco.codegeneration;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A No-Op Outputstream which discards every byte written.
 *
 * @author Simon Taddiken
 */
class NopOutputStream extends OutputStream {

    @Override
    public void write(int b) throws IOException {}

}
