package com.amazon.ata.kindlepublishingservice.mastery.three;

import com.amazon.ata.kindlepublishingservice.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.GetPublishingStatusResponse;
import com.amazon.ata.kindlepublishingservice.KindlePublishingClientException;
import com.amazon.ata.kindlepublishingservice.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.helpers.IntegrationTestBase;
import com.amazon.ata.kindlepublishingservice.helpers.KindlePublishingServiceTctTestDao.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.helpers.KindlePublishingServiceTctTestDao.PublishingStatusItem;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

public class MasteryTaskThreeTests extends IntegrationTestBase {
    private static final String TCT_STATUS_MESSAGE = "TCT test data created by MasteryTaskThreeTests";
    private static final String SINGLE_STATUS_RECORD_ID = "publishingstatus.tct3_single_status";
    private static final String SINGLE_STATUS_BOOK_ID = "book.tct3_single_status";
    private static final String MULTIPLE_STATUS_RECORD_ID = "publishingstatus.tct3_multiple_statuses";
    private static final String MULTIPLE_STATUS_BOOK_ID = "book.tct3.multiple_statuses";
    private static final String NONEXISTENT_STATUS_RECORD_ID = "publishingstatus.tct3_does_not_exist";

    /**
     * Ensure the test infra is ready for test run, including creating the client.
     */
    @BeforeClass
    public void setup() {
        super.setup();
        savePublishingStatuses();
    }

    @Test
    public void getPublishingStatus_singleStatus_returnsSingleRecord() {
        // GIVEN
        GetPublishingStatusRequest request = GetPublishingStatusRequest.builder()
            .withPublishingRecordId(SINGLE_STATUS_RECORD_ID)
            .build();

        // WHEN
        GetPublishingStatusResponse response = super.kindlePublishingServiceClient
            .callGetPublishingStatus(request);

        // THEN
        assertNotNull(response.getPublishingStatusHistory(), "Expected a non null response from GetPublishingStatus");
        assertEquals(response.getPublishingStatusHistory().size(), 1,
            String.format("Expected a single record with [status record id: %s, status: %s]",
                SINGLE_STATUS_RECORD_ID, PublishingRecordStatus.QUEUED));
        assertNotNull(response.getPublishingStatusHistory().get(0), "Expected a single non-null " +
            "status record");

        PublishingStatusRecord record = response.getPublishingStatusHistory().get(0);
        assertPublishingStatusRecord(record, PublishingRecordStatus.QUEUED, SINGLE_STATUS_BOOK_ID);
    }

    @Test
    public void getPublishingStatus_multipleStatuses_returnsAllRecord() {
        // GIVEN
        GetPublishingStatusRequest request = GetPublishingStatusRequest.builder()
            .withPublishingRecordId(MULTIPLE_STATUS_RECORD_ID)
            .build();

        // WHEN
        GetPublishingStatusResponse response = super.kindlePublishingServiceClient
            .callGetPublishingStatus(request);

        // THEN
        assertNotNull(response.getPublishingStatusHistory(), "Expected a non null response from GetPublishingStatus");
        assertEquals(response.getPublishingStatusHistory().size(), 4,
            "Incorrect number of records returned for status record ID " + MULTIPLE_STATUS_RECORD_ID);

        Map<String, List<PublishingStatusRecord>> statusRecordMap = response.getPublishingStatusHistory()
            .stream()
            .collect(Collectors.groupingBy(PublishingStatusRecord::getStatus));

        // we saved 1 record per status type (QUEUED, IN_PROGRESS, SUCCESSFUL, FAILED)
        for (PublishingRecordStatus status : PublishingRecordStatus.values()) {
            assertEquals(statusRecordMap.get(status.name()).size(), 1,
                String.format("Expected a single record with [status record id: %s, status: %s]",
                    MULTIPLE_STATUS_RECORD_ID, status));
            assertPublishingStatusRecord(statusRecordMap.get(status.name()).get(0),
                status, MULTIPLE_STATUS_BOOK_ID);
        }
    }

    @Test
    public void getPublishingStatus_noRecordForStatus_throwsKindlePublishingClientException() {
        // GIVEN
        GetPublishingStatusRequest request = GetPublishingStatusRequest.builder()
            .withPublishingRecordId(NONEXISTENT_STATUS_RECORD_ID)
            .build();

        // WHEN + THEN
        assertThrows(KindlePublishingClientException.class, () ->
            super.kindlePublishingServiceClient.newGetPublishingStatusCall()
                .call(request));
    }

    private void assertPublishingStatusRecord(PublishingStatusRecord actual,
                                              PublishingRecordStatus expectedStatus,
                                              String expectedBookId) {
        assertEquals(actual.getBookId(), expectedBookId, "GetPublishingStatus returned a status record " +
            "with an unexpected bookId.");
        assertEquals(actual.getStatusMessage(), TCT_STATUS_MESSAGE, "GetPublishingStatus returned a status record " +
            "with an unexpected status message ");
        assertEquals(actual.getStatus(), expectedStatus.name(), "GetPublishingStatus returned a status record " +
            "with an unexpected publishing status.");
    }

    private void savePublishingStatuses() {
        PublishingStatusItem queued = new PublishingStatusItem();
        queued.setPublishingRecordId(SINGLE_STATUS_RECORD_ID);
        queued.setStatus(PublishingRecordStatus.QUEUED);
        queued.setStatusMessage(TCT_STATUS_MESSAGE);
        queued.setBookId(SINGLE_STATUS_BOOK_ID);

        super.getTestDao().save(queued);

        for (PublishingRecordStatus status : PublishingRecordStatus.values()) {
            PublishingStatusItem multipleStatus = new PublishingStatusItem();
            multipleStatus.setPublishingRecordId(MULTIPLE_STATUS_RECORD_ID);
            multipleStatus.setBookId(MULTIPLE_STATUS_BOOK_ID);
            multipleStatus.setStatusMessage(TCT_STATUS_MESSAGE);
            multipleStatus.setStatus(status);

            super.getTestDao().save(multipleStatus);
        }
    }
}
