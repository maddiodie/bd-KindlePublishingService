package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.converters.PublishingStatusItemConverter;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * This API allows the client to retrieve a publishing status record.
 */

public class GetPublishingStatusActivity {

    private PublishingStatusDao publishingStatusDao;

    /**
     * Instantiates a new GetPublishingStatusActivity.
     *
     * @param publishingStatusDao PublishingStatusDao to access the PublishingRecord table.
     */
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    /**
     * Retrieves the publishing status record(s) with the given publishing status ID.
     *
     * @param publishingStatusRequest Request object containing the publishing status ID associated
     *                                with the list of publishing status items from the table.
     * @return GetPublishingStatusResponse Response object containing the requested publishing status
     *                                     item.
     */
    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        List<PublishingStatusItem> publishingStatusItemList = publishingStatusDao
                .getPublishingStatus(publishingStatusRequest.getPublishingRecordId());

        List<PublishingStatusRecord> publishingStatusRecordList = new ArrayList<>();

        for(PublishingStatusItem publishingStatusItem : publishingStatusItemList) {
            publishingStatusRecordList.add(PublishingStatusItemConverter.toPublishingStatusRecord(publishingStatusItem));
        }

        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(publishingStatusRecordList) // takes a List<PublishingStatusRecord>
                .build();
    }

}
