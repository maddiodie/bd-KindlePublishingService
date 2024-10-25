package com.amazon.ata.kindlepublishingservice.exceptions;

/**
 * Exception to be thrown when a book is already marked as inactive.
 */
public class BookInactiveException extends RuntimeException {

    private static final long serialVersionUID = 7805358307628540964L;

    /**
     * Exception with a message, but no cause.
     * @param message A descriptive message for this exception.
     */
    public BookInactiveException(String message) {
        super(message);
    }

    /**
     * Exception with a message and cause.
     * @param message A descriptive message for this exception.
     * @param cause The original throwable resulting in this exception.
     */
    public BookInactiveException(String message, Throwable cause) {
        super(message, cause);
    }

}
