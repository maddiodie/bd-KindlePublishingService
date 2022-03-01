package com.amazon.ata.kindlepublishingservice.mastery.two.submit;

import com.amazon.ata.kindlepublishingservice.KindlePublishingClientException;
import com.amazon.ata.kindlepublishingservice.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.helpers.IntegrationTestBase;
import com.amazon.ata.kindlepublishingservice.helpers.KindlePublishingServiceTctTestDao.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.helpers.KindlePublishingServiceTctTestDao.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.helpers.KindlePublishingServiceTctTestDao.PublishingStatusItem;

import com.amazon.ata.recommendationsservice.types.BookGenre;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

public class MasteryTaskTwoSubmitBookForPublishingTests extends IntegrationTestBase {

    /**
     * Ensure the test infra is ready for test run, including creating the client.
     */
    @BeforeClass
    public void setup() {
        super.setup();
    }

    @Test
    public void submitBookForPublishing_noBookId_submitsBook() {

        // GIVEN
        SubmitBookForPublishingRequest submitBookForPublishingRequest = SubmitBookForPublishingRequest.builder()
            .withAuthor("author")
            .withGenre(String.valueOf(BookGenre.ACTION))
            .withText("text")
            .withTitle("title")
            .build();

        // WHEN
        SubmitBookForPublishingResponse response = super.kindlePublishingServiceClient
            .newSubmitBookForPublishingCall()
            .call(submitBookForPublishingRequest);

        // THEN
        PublishingStatusItem key = new PublishingStatusItem();
        key.setPublishingRecordId(response.getPublishingRecordId());
        key.setStatus(PublishingRecordStatus.QUEUED);

        PublishingStatusItem publishingStatusRecord = getTestDao().load(key);

        assertNotNull(publishingStatusRecord, String.format("Expected a publishing status record with " +
            "[id: %s, status: %s] to be saved in DynamoDB",
            key.getPublishingRecordId(), PublishingRecordStatus.QUEUED));
        assertNotNull(publishingStatusRecord.getStatusMessage(), String.format("Expected the saved " +
            "publishing status record %s to have a non null status message.", publishingStatusRecord));
        assertNull(publishingStatusRecord.getBookId(), String.format("Expected the saved " +
            "publishing status record %s to have a null book ID", publishingStatusRecord));
    }

    @Test
    public void submitBookForPublishing_existingBookId_submitsBook() {
        // GIVEN
        CatalogItemVersion catalogItemVersion = saveNewCatalogItemVersion(false);

        SubmitBookForPublishingRequest submitBookForPublishingRequest = SubmitBookForPublishingRequest.builder()
            .withAuthor(catalogItemVersion.getAuthor())
            .withBookId(catalogItemVersion.getBookId())
            .withGenre(catalogItemVersion.getGenre().name())
            .withText(catalogItemVersion.getText())
            .withTitle(catalogItemVersion.getTitle())
            .build();

        // WHEN
        SubmitBookForPublishingResponse response = super.kindlePublishingServiceClient
            .newSubmitBookForPublishingCall()
            .call(submitBookForPublishingRequest);

        // THEN
        PublishingStatusItem key = new PublishingStatusItem();
        key.setPublishingRecordId(response.getPublishingRecordId());
        key.setStatus(PublishingRecordStatus.QUEUED);

        PublishingStatusItem publishingStatusRecord = getTestDao().load(key);

        assertNotNull(publishingStatusRecord, String.format("Expected a publishing record status item with " +
            "[id: %s, status: %s] to be saved in DynamoDB", key, PublishingRecordStatus.QUEUED));
        assertEquals(catalogItemVersion.getBookId(), publishingStatusRecord.getBookId(), "Expected " +
            "the saved publishing status record to have the same book ID as the request.");
        assertNotNull(publishingStatusRecord.getStatusMessage(), String.format("Expected the saved " +
            "publishing status record %s to have a non null status message.", publishingStatusRecord));    }

    @Test
    public void submitBookForPublishing_existingInactiveBookId_submitsBook() {
        // GIVEN
        CatalogItemVersion catalogItemVersion = saveNewCatalogItemVersion(true);

        SubmitBookForPublishingRequest submitBookForPublishingRequest = SubmitBookForPublishingRequest.builder()
            .withAuthor(catalogItemVersion.getAuthor())
            .withBookId(catalogItemVersion.getBookId())
            .withGenre(catalogItemVersion.getGenre().name())
            .withText(catalogItemVersion.getText())
            .withTitle(catalogItemVersion.getTitle())
            .build();

        // WHEN
        SubmitBookForPublishingResponse response = super.kindlePublishingServiceClient
            .newSubmitBookForPublishingCall()
            .call(submitBookForPublishingRequest);

      // THEN
        PublishingStatusItem key = new PublishingStatusItem();
        key.setPublishingRecordId(response.getPublishingRecordId());
        key.setStatus(PublishingRecordStatus.QUEUED);

        PublishingStatusItem publishingStatusRecord = getTestDao().load(key);

        assertNotNull(publishingStatusRecord, String.format("Expected a publishing record status item with " +
            "[id: %s, status: %s] to be saved in DynamoDB", key, PublishingRecordStatus.QUEUED));
        assertEquals(catalogItemVersion.getBookId(), publishingStatusRecord.getBookId(), "Expected " +
            "the saved publishing status record to have the same book ID as the request.");
        assertNotNull(publishingStatusRecord.getStatusMessage(), String.format("Expected the saved " +
            "publishing status record %s to have a non null status message.", publishingStatusRecord));    }

    @Test
    public void submitBookForPublishing_bookIdThatDoesNotExist_throwsKindlePublishingClientException() {
        // GIVEN
        SubmitBookForPublishingRequest submitBookForPublishingRequest = SubmitBookForPublishingRequest.builder()
            .withAuthor("author")
            .withBookId(UUID.randomUUID().toString())
            .withGenre(String.valueOf(BookGenre.ACTION))
            .withText("text")
            .withTitle("title")
            .build();

        // WHEN + THEN
        assertThrows(KindlePublishingClientException.class, () ->
            super.kindlePublishingServiceClient.newSubmitBookForPublishingCall()
                .call(submitBookForPublishingRequest));
    }

    private CatalogItemVersion saveNewCatalogItemVersion(boolean inactive) {
        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();
        catalogItemVersion.setBookId("MT02_SubmitBookForPublishing_" + UUID.randomUUID().toString());
        catalogItemVersion.setVersion(1);
        catalogItemVersion.setAuthor("author");
        catalogItemVersion.setGenre(BookGenre.ACTION);
        catalogItemVersion.setText("text");
        catalogItemVersion.setTitle("title");
        catalogItemVersion.setInactive(inactive);

        super.getTestDao().save(catalogItemVersion);

        return catalogItemVersion;
    }
}
