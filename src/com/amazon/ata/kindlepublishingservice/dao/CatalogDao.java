package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormatConverter;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;
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

    /**
     * Soft deletes (or removes) the given book from the catalog.
     * @param bookId given bookId of book to be removed form catalog
     * @throws BookNotFoundException thrown if given bookId not found in catalog
     */
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

    /**
     * Private method that gets the latest version of the book with the given bookId.
     * @param bookId given bookId to retrieve the latest version of from the catalog
     * @return latest version of the given bookId from the catalog
     */
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

    /**
     * Validates whether a book exists by using the <getLatestVersionOfBook()> method and only throwing a
     * BookNotFoundException if it doesn't exist.
     * @param bookId used to check which book we are validating
     * @throws BookNotFoundException thrown if the book with the given bookId does not exist
     */
    public void validateBookExists(String bookId) throws BookNotFoundException {
        if (getLatestVersionOfBook(bookId) == null) {
            throw new BookNotFoundException("The given book was not found with the given bookId: "
                    + bookId + ".");
        }
    }

    /**
     * Adds a book to the catalog. If the book already exists, it updates the book by incrementing
     * the version number and marking the previous version as inactive. If the book does not exist, it
     * creates a new entry with a new bookId and sets the version to 1.
     * @param book
     */
    public void addBookToCatalog(KindleFormattedBook book) {
        CatalogItemVersion catalogItemVersion = new CatalogItemVersion();
        catalogItemVersion.setTitle(book.getTitle());
        catalogItemVersion.setAuthor(book.getAuthor());
        catalogItemVersion.setText(book.getText());
        catalogItemVersion.setGenre(book.getGenre());
        catalogItemVersion.setInactive(false);

        try {
            CatalogItemVersion latestVersion = getBookFromCatalog(book.getBookId());
            // checking to see if we need to update or add a new entry
            // grabs the latest entry of the book if it exists
            catalogItemVersion.setBookId(latestVersion.getBookId());
            // set the bookId of the new CatalogItemVersion to the same as the latest version
            catalogItemVersion.setVersion(latestVersion.getVersion() + 1);
            // increment the version of the new CatalogItemVersion by 1
            latestVersion.setInactive(true);
            dynamoDbMapper.save(latestVersion);
            // save the latest version of the CatalogItemVersion
            //  (now marked as inactive to the catalog)
        } catch (BookNotFoundException e) {
            catalogItemVersion.setBookId(KindlePublishingUtils.generateBookId());
            catalogItemVersion.setVersion(1);
        }

        dynamoDbMapper.save(catalogItemVersion);
    }

}
