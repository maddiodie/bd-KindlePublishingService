package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;

import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishTask;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.validation.ValidationException;

/**
 * Implementation of the SubmitBookForPublishingActivity for ATACurriculumKindlePublishingService's
 * SubmitBookForPublishing API.
 *
 * This API allows the client to submit a new book to be published in the catalog or update an existing
 * book.
 */
public class SubmitBookForPublishingActivity {

    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;
    private BookPublishRequestManager bookPublishRequestManager;
    private BookPublishTask bookPublishTask;

    /**
     * Instantiates a new SubmitBookForPublishingActivity object.
     * @param publishingStatusDao PublishingStatusDao to access the publishing status table.
     */
    @Inject
    public SubmitBookForPublishingActivity(PublishingStatusDao publishingStatusDao, CatalogDao
            catalogDao, BookPublishRequestManager bookPublishRequestManager, BookPublishTask
            bookPublishTask) {
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.bookPublishTask = bookPublishTask;
    }

    /**
     * Submits the book in the request for publishing.
     * @param request Request object containing the book data to be published. If the request is
     *                updating an existing book, then the corresponding book id should be provided.
     *                Otherwise, the request will be treated as a new book.
     * @return SubmitBookForPublishingResponse Response object that includes the publishing status id,
     *         which can be used to check the publishing state of the book.
     */
    public SubmitBookForPublishingResponse execute(SubmitBookForPublishingRequest request) {
        final BookPublishRequest bookPublishRequest = BookPublishRequestConverter
                .toBookPublishRequest(request);

        String bookId = request.getBookId();

        if (bookId != null) {
            catalogDao.validateBookExists(bookId);
            // 1a
        }

        if (request.getTitle() == null || request.getAuthor() == null || request.getGenre() == null
                || request.getText() == null) {
            throw new ValidationException("Any or all of the provided values do not exist or are " +
                    "'null'. All values must exist.\n"
                    + "Title: " + request.getTitle() + "\n"
                    + "Author: " + request.getAuthor() + "\n"
                    + "Genre: " + request.getGenre() + "\n"
                    + "Text: " + request.getText() + "\n");
        }
        // 4

        // 1b ...
        bookPublishRequestManager.addBookPublishRequest(bookPublishRequest);

        new Thread(bookPublishTask).start();

        PublishingStatusItem item = publishingStatusDao.setPublishingStatus(bookPublishRequest
                        .getPublishingRecordId(),
                PublishingRecordStatus.QUEUED,
                bookPublishRequest.getBookId());
        // 3

        return SubmitBookForPublishingResponse.builder()
                .withPublishingRecordId(item.getPublishingRecordId())
                .build();
    }

    // PLAN
    // (1a) if request contains a bookId
    //      this is supposedly an existing book ... validate that it exists using <validateBookExists()>
    // (1b) if request does not contain then the submission will be considered a new book
    //      a new book id will be generated when the book is published (we don't have to worry about this)
    // (2) insert a book publishing request into the manager
    // (3) WHAT ALREADY EXISTS WHICH IS THE QUEUE SHIT
    // (4) if the book submission is missing the title, author, language, genre, or text ... a
    //     <ValidationException> will be thrown

}
