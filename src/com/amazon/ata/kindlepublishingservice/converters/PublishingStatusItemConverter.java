package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

public class PublishingStatusItemConverter {

    private PublishingStatusItemConverter() {}

    public static PublishingStatusRecord toPublishingStatusRecord(
            PublishingStatusItem publishingStatusItem) {
        return PublishingStatusRecord.builder()
                .withBookId(publishingStatusItem.getBookId())
                .withStatus(publishingStatusItem.getStatus().name())
                .withStatusMessage(publishingStatusItem.getStatusMessage())
                .build();
    }

}
