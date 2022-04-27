package co.phoenixlab.dds;

import java.io.IOException;

public class InvalidDdsException extends IOException {

    public InvalidDdsException() {
    }

    public InvalidDdsException(String message) {
        super(message);
    }

    public InvalidDdsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDdsException(Throwable cause) {
        super(cause);
    }
}
