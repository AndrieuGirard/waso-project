package fr.insalyon.waso.util.exception;

import java.io.IOException;

public class ServiceIOException extends IOException {

    public ServiceIOException(String message) {
        super(message);
    }

    public ServiceIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceIOException(Throwable cause) {
        super(cause);
    }
    
}
