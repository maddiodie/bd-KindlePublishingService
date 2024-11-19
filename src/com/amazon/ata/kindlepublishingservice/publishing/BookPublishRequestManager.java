package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages a collection of book publish requests ensuring that requests are processed in teh same order that they
 * are submitted.
 */
@Singleton
public class BookPublishRequestManager {

    // todo: mt2
    //  - immediate dagger integration is not necessary until unit tests have been completed ... however,
    //    we need to prepare his manager for future use with dagger even if it's not currently required
    //  - as you implement the class, make sure to include constructor parameters for any dependencies and
    //    annotate the class appropriately for dagger as you go

    /**
     * Queue to hold the book publish requests on a first-come-first-serve basis.
     */
    private Queue<BookPublishRequest> bookPublishRequestsQueue;

    /**
     * Public constructor to instantiate class.
     */
    public BookPublishRequestManager() {
        this.bookPublishRequestsQueue = new LinkedList<>();
    }

    /**
     * Adds the given book publish request to the queue.
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
