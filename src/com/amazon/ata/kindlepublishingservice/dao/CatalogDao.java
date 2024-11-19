package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog
     *                       table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the
     * specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is
     * found.
     * @param bookId ID associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String
                    .format("No book found for id: %s", bookId));
        }

        return book;
    }


    public void removeBookFromCatalog(String bookId) throws BookNotFoundException {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book != null && !book.isInactive()) {
            book.setInactive(true);
            dynamoDbMapper.save(book);
        } else {
            throw new BookNotFoundException("Book with ID " + bookId
                    + " not found or is already marked as inactive.");
        }
    }


    // returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression =
                new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class,
                queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }


    // todo: mt2
    //  - add new void method <validateBookExists()> which the <SubmitBookForPublishingActivity can call
    //  - this checks if the provided <bookId> exists in the catalog and throws a <BookNotFoundException> if it doesn't
    //  - the system should not restrict user from submitting a new version just because the current version is not
    //    active

}
