package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.models.Book;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class BookPublishRequestManager {

    // todo: mt2
    //  - double check the design document to ensure the specifics of the implementation are being followed
    //  - immediate dagger integration is not necessary until unit tests have been completed ... however,
    //    we need to prepare his manager for future use with dagger even if it's not currently required
    //  - as you implement the class, make sure to include constructor parameters for any dependencies and
    //    annotate the class appropriately for dagger as you go

    /**
     * Static instance of the Singleton.
     */
    private static BookPublishRequestManager instance;

    /**
     * Queue to hold the book publish requests.
     */
    private Queue<BookPublishRequest> bookPublishRequestsQueue;

    /**
     * Private constructor to prevent instantiation.
     */
    private BookPublishRequestManager() {
        this.bookPublishRequestsQueue = new LinkedList<>();
    }

    /**
     * Public method to provide access to the Singleton instance.
     * @return the Singleton instance of this class
     */
    public static BookPublishRequestManager getInstance() {
        if (instance == null) {
            instance = new BookPublishRequestManager();
        }
        return instance;
    }

    /**
     * Adds the given book publish request to the queue on a first-come-first-serve basis.
     * @param bookPublishRequest book publish request being made
     */
    public void addBookPublishRequest(BookPublishRequest bookPublishRequest) {
        bookPublishRequestsQueue.add(bookPublishRequest);
    }

    /**
     * Retrieves the next book publish request in line for publishing and returns it. If there are no requests to
     * publish in the book publish request queue, it returns null.
     * @return the next book publish request in the queue
     */
    public BookPublishRequest getBookPublishRequestToProcess() {
        if (bookPublishRequestsQueue.isEmpty()) {
            return null;
        }

        return bookPublishRequestsQueue.poll();
    }

}
