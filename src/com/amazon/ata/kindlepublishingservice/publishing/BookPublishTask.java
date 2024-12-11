package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BookPublishTask.class);
    private BookPublishRequestManager bookPublishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                           PublishingStatusDao publishingStatusDao, CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    /**
     * Processes a BookPublishRequest from the BookPublishManager.
     */
    @Override
    public void run() {
        BookPublishRequest bookPublishRequest =
                bookPublishRequestManager.getBookPublishRequestToProcess();

        if (bookPublishRequest == null) {
            logger.info("No book publish requests to process.");
            return;
        }
        // if there are no requests to process, return immediately ...

        try {
            logger.info("Processing book request for publishing record ID: {}.",
                    bookPublishRequest.getPublishingRecordId());
            processBookRequest(bookPublishRequest);

            logger.info("Updating status to SUCCESSFUL for publishing record ID: {}.",
                    bookPublishRequest.getPublishingRecordId());
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL, bookPublishRequest.getBookId());
            // updated status to SUCCESSFUL
        } catch (Exception e) {
            logger.error("Failed to process book publish request for publishing record ID: {}.",
                    bookPublishRequest.getPublishingRecordId(), e);
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED, bookPublishRequest.getBookId(), e.getMessage());
            // update status to FAILED in case of an exception
            e.printStackTrace();
        }

    }

    private void processBookRequest(BookPublishRequest request) {
        logger.info("Adding entry to publishing status table for publishing record ID: {}.",
                request.getPublishingRecordId());
        addEntryToPublishingStatusTable(request);

        logger.info("Formatting and converting book for publishing record ID: {}.",
                request.getPublishingRecordId());
        KindleFormattedBook kindleFormattedBook = bookFormattingAndConversion(request);

        logger.info("Adding book to catalog table for publishing record ID: {}.",
                request.getPublishingRecordId());
        addBookToCatalogTable(kindleFormattedBook);
    }

    private void addEntryToPublishingStatusTable(BookPublishRequest request) {
        logger.info("Setting status to IN_PROGRESS for publishing record ID: {}.",
                request.getPublishingRecordId());
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS, request.getBookId());
    }

    private KindleFormattedBook bookFormattingAndConversion(BookPublishRequest request) {
        logger.info("Formatting book for publishing record ID: {}.",
                request.getPublishingRecordId());
        return KindleFormatConverter.format(request);
    }

    private void addBookToCatalogTable(KindleFormattedBook kindleFormattedBook) {
        logger.info("Adding book to catalog for book ID: {}.", kindleFormattedBook.getBookId());
        catalogDao.addBookToCatalog(kindleFormattedBook);
    }

}
