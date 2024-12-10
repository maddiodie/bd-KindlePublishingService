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

    @Override
    public void run() {
        BookPublishRequest bookPublishRequest =
                bookPublishRequestManager.getBookPublishRequestToProcess();

        if (bookPublishRequestManager == null) {
            logger.info("No book publish requests to process.");
            return;
        }
        // if there are no requests to process, return immediately ...

        try {
            logger.info("Updating status to IN_PROGRESS for publishing record ID.");
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.IN_PROGRESS, bookPublishRequest.getBookId());
            // updates status to IN_PROGRESS

            processBookRequest(bookPublishRequest);

            logger.info("Updating status to SUCCESSFUL for publishing record ID.");
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL, bookPublishRequest.getBookId());
            // updated status to SUCCESSFUL
        } catch (Exception e) {
            logger.error("Failed to process book publish request for publishing record ID.");
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED, bookPublishRequest.getBookId(), e.getMessage());
            // update status to FAILED in case of an exception
            e.printStackTrace();
        }

    }

    private void processBookRequest(BookPublishRequest request) {
        addEntryToPublishingStatusTable(request);
        addBookToCatalogTable(bookFormattingAndConversion(request));
    }

    private void addEntryToPublishingStatusTable(BookPublishRequest request) {
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS, request.getBookId());
    }

    private KindleFormattedBook bookFormattingAndConversion(BookPublishRequest request) {
        return KindleFormatConverter.format(request);
    }

    private void addBookToCatalogTable(KindleFormattedBook kindleFormattedBook) {
        catalogDao.addBookToCatalog(kindleFormattedBook);
    }

}
