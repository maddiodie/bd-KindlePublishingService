package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.models.requests.RemoveBookFromCatalogRequest;
import com.amazon.ata.kindlepublishingservice.models.response.RemoveBookFromCatalogResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;

public class RemoveBookFromCatalogActivity {

    private CatalogDao catalogDao;

    /**
     * Instantiates a new RemoveBookFromCatalogActivity object.
     *
     * @param catalogDao to access the Catalog table in DynamoDB
     */
    @Inject
    public RemoveBookFromCatalogActivity(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }


    public RemoveBookFromCatalogResponse execute(final RemoveBookFromCatalogRequest request) {
        // todo: mt1
        // CatalogItemVersion catalogItem = catalogDao.removeBookFromCatalog(request.getBookId());

        Book book = null;
        // use the dao and request to get the deleted book for the response object below

        return RemoveBookFromCatalogResponse.builder()
                .withBook(book)
                .build();
    }
}
