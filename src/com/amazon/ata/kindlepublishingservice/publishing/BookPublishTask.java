package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;

public class BookPublishTask implements Runnable {

    private BookPublishRequestManager bookPublishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;

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
            return;
        }
        // if there are no requests to process, return immediately ...

        try {
            processBookRequest(bookPublishRequest);
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL, bookPublishRequest.getBookId());
        } catch (Exception e) {
            publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED, bookPublishRequest.getBookId(), e.getMessage());
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
