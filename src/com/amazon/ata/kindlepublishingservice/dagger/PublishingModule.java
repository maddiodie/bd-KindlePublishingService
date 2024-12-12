package com.amazon.ata.kindlepublishingservice.dagger;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishTask;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublisher;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Module;
import dagger.Provides;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;

@Module
public class PublishingModule {

    @Provides
    @Singleton
    public BookPublisher provideBookPublisher(ScheduledExecutorService scheduledExecutorService) {
        return new BookPublisher(scheduledExecutorService,
                new BookPublishTask(providesBookPublishRequestManager(), providePublishingStatusDao(),
                provideCatalogDao()));
    }

    @Provides
    @Singleton
    public ScheduledExecutorService provideBookPublisherScheduler() {
        return Executors.newScheduledThreadPool(1);
    }

    @Provides
    @Singleton
    public BookPublishRequestManager providesBookPublishRequestManager() {
        return new BookPublishRequestManager();
    }

    @Provides
    @Singleton
    public PublishingStatusDao providePublishingStatusDao() {
        return new PublishingStatusDao(provideDynamoDBMapper());
    }

    @Provides
    @Singleton
    public CatalogDao provideCatalogDao() {
        return new CatalogDao(provideDynamoDBMapper());
    }

    @Provides
    @Singleton
    public DynamoDBMapper provideDynamoDBMapper() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();

        return new DynamoDBMapper(client);
    }

}
