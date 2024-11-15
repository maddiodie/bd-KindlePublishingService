package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
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


    /** Soft deletes a Book from the catalog by marking it as inactive so as not to lose data.
     *
     * @param request Request object containing the book ID associated with the book to remove from
     *                the catalog.
     * @return Returns the Book that was either soft deleted/marked as inactive or the Book that does
     * not exist in the catalog ... just returns the Book we're trying to do tings to.
     */
    public RemoveBookFromCatalogResponse execute(final RemoveBookFromCatalogRequest request) {
        Book book = Book.builder()
                .withBookId(request.getBookId())
                .build();

        try {
            catalogDao.removeBookFromCatalog(request.getBookId());
            return RemoveBookFromCatalogResponse.builder()
                    .withBook(book)
                    .withMessage("Book successfully removed.")
                    .build();
        } catch (BookNotFoundException e) {
            System.err.println("Error removing book: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
