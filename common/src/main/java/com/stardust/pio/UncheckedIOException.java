package com.stardust.pio;

import java.io.IOException;

/**
 *
 */

public class UncheckedIOException extends RuntimeException {

    public UncheckedIOException(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }
}
