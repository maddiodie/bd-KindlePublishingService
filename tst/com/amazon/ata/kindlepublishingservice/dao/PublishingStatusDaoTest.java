package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class PublishingStatusDaoTest {

    @Mock
    private PaginatedQueryList<CatalogItemVersion> list;

    @Mock
    private DynamoDBMapper dynamoDbMapper;

    @InjectMocks
    private PublishingStatusDao publishingStatusDao;

    @BeforeEach
    public void setup(){
        initMocks(this);
    }

    @Test
    public void getPublishingStatuses_publishingRecordIdDoesNotExist_exceptionThrown() {
        // GIVEN
        String invalidId = "invalid";
        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId(invalidId);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(PublishingStatusItem.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(true);

        // WHEN && THEN
        assertThrows(PublishingStatusNotFoundException.class,
            () -> publishingStatusDao.getPublishingStatuses(invalidId),
            "Expected BookNotFoundException to be thrown for an invalid bookId.");
        verify(dynamoDbMapper).query(eq(PublishingStatusItem.class), requestCaptor.capture());
        PublishingStatusItem queriedItem = (PublishingStatusItem) requestCaptor.getValue().getHashKeyValues();
        assertEquals(invalidId, queriedItem.getPublishingRecordId());
    }

    @Test
    public void getPublishingStatuses_publishingRecordIdExists_statusesReturned() {
        // GIVEN
        String publishingId = "publishingstatus.123";
        PublishingStatusItem item = new PublishingStatusItem();
        item.setPublishingRecordId(publishingId);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(PublishingStatusItem.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);

        // WHEN
        List<PublishingStatusItem> statuses = publishingStatusDao.getPublishingStatuses(publishingId);

        // THEN
        assertEquals(list, statuses, "Expected method to return the publishing status list " +
            "from the datastore.");

        verify(dynamoDbMapper).query(eq(PublishingStatusItem.class), requestCaptor.capture());
        PublishingStatusItem queriedItem = (PublishingStatusItem) requestCaptor.getValue().getHashKeyValues();
        assertEquals(publishingId, queriedItem.getPublishingRecordId());
    }

    @Test
    public void setPublishingStatus_successful_bookIdPresent() {
        // GIVEN
        String publishingId = "publishingstatus.123";
        String bookId = "book.123";

        // WHEN
        PublishingStatusItem status = publishingStatusDao.setPublishingStatus(publishingId,
            PublishingRecordStatus.SUCCESSFUL, bookId);

        // THEN
        verify(dynamoDbMapper).save(any(PublishingStatusItem.class));
        assertEquals(publishingId, status.getPublishingRecordId(), "Expected saved status to have the " +
            "correct publishing status id.");
        assertEquals(PublishingRecordStatus.SUCCESSFUL, status.getStatus(), "Expected saved status to have" +
            " the correct publishing status.");
        assertNotNull(status.getBookId(), "BookId should be present for successfully published book.");
        assertNotNull(status.getStatusMessage() , "Each status record should have a message.");
    }

    @Test
    public void setPublishingStatus2_queued_statusSaved() {
        // GIVEN
        String publishingId = "publishingstatus.123";

        // WHEN
        PublishingStatusItem status = publishingStatusDao.setPublishingStatus(publishingId,
            PublishingRecordStatus.QUEUED, null);

        // THEN
        verify(dynamoDbMapper).save(any(PublishingStatusItem.class));
        assertEquals(publishingId, status.getPublishingRecordId(), "Expected saved status to have the " +
            "correct publishing status id.");
        assertEquals(PublishingRecordStatus.QUEUED, status.getStatus(), "Expected saved status to have" +
            " the correct publishing status.");
        assertNotNull(status.getStatusMessage() , "Each status record should have a message.");
        assertNull(status.getBookId(), "Expected bookId to be null in the status, when a bookId is not provided.");
    }

    @Test
    public void setPublishingStatus2_additionalMessage_statusSaved() {
        // GIVEN
        String publishingId = "publishingstatus.123";
        String bookId = "book.123";

        // WHEN
        PublishingStatusItem status = publishingStatusDao.setPublishingStatus(publishingId,
            PublishingRecordStatus.FAILED, bookId, "Failed due to...");

        // THEN
        verify(dynamoDbMapper).save(any(PublishingStatusItem.class));
        assertEquals(publishingId, status.getPublishingRecordId(), "Expected saved status to have the " +
            "correct publishing status id.");
        assertEquals(PublishingRecordStatus.FAILED, status.getStatus(), "Expected saved status to have" +
            " the correct publishing status.");
        assertNotNull(status.getStatusMessage() , "Each status record should have a message.");
        assertTrue(status.getStatusMessage().contains("Additional Notes"), "If a message is provided it should be" +
            "included in the status message as 'Additional Notes'");
        assertTrue(status.getStatusMessage().contains("Failed due to..."), "If a message is provided it should be" +
            "included in the status message.");
    }
}
