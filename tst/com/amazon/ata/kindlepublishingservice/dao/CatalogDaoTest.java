package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.recommendationsservice.types.BookGenre;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.xml.catalog.Catalog;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CatalogDaoTest {

    @Mock
    private PaginatedQueryList<CatalogItemVersion> list;

    @Mock
    private DynamoDBMapper dynamoDbMapper;

    @InjectMocks
    private CatalogDao catalogDao;

    @BeforeEach
    public void setup(){
        initMocks(this);
    }

    @Test
    public void getBookFromCatalog_bookDoesNotExist_throwsException() {
        // GIVEN
        String invalidBookId = "notABookID";
        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(true);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.getBookFromCatalog(invalidBookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void getBookFromCatalog_bookInactive_throwsException() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(true);
        item.setBookId(bookId);
        item.setVersion(1);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN && THEN
        assertThrows(BookNotFoundException.class, () -> catalogDao.getBookFromCatalog(bookId),
                "Expected BookNotFoundException to be thrown for an invalid bookId.");
    }

    @Test
    public void getBookFromCatalog_oneVersion_returnVersion1() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(1);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.getBookFromCatalog(bookId);

        // THEN
        assertEquals(bookId, book.getBookId());
        assertEquals(1, book.getVersion(), "Expected version 1 of book to be returned");
        assertFalse(book.isInactive(), "Expected book to be active.");

        verify(dynamoDbMapper).query(eq(CatalogItemVersion.class), requestCaptor.capture());
        CatalogItemVersion queriedItem = (CatalogItemVersion) requestCaptor.getValue().getHashKeyValues();
        assertEquals(bookId, queriedItem.getBookId(), "Expected query to look for provided bookId");
        assertEquals(1, requestCaptor.getValue().getLimit(), "Expected query to have a limit set");
    }

    @Test
    public void getBookFromCatalog_twoVersions_returnsVersion2() {
        // GIVEN
        String bookId = "book.123";
        CatalogItemVersion item = new CatalogItemVersion();
        item.setInactive(false);
        item.setBookId(bookId);
        item.setVersion(2);
        ArgumentCaptor<DynamoDBQueryExpression> requestCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class))).thenReturn(list);
        when(list.isEmpty()).thenReturn(false);
        when(list.get(0)).thenReturn(item);

        // WHEN
        CatalogItemVersion book = catalogDao.getBookFromCatalog(bookId);

        // THEN
        assertEquals(bookId, book.getBookId());
        assertEquals(2, book.getVersion(), "Expected version 2 of book to be returned");
        assertFalse(book.isInactive(), "Expected book to be active.");

        verify(dynamoDbMapper).query(eq(CatalogItemVersion.class), requestCaptor.capture());
        CatalogItemVersion queriedItem = (CatalogItemVersion) requestCaptor.getValue().getHashKeyValues();
        assertEquals(bookId, queriedItem.getBookId(), "Expected query to look for provided bookId");
        assertEquals(1, requestCaptor.getValue().getLimit(), "Expected query to have a limit set");
    }

    @Test
    public void removeBookFromCatalog_bookDoesNotExist_throwsException() throws BookNotFoundException {
        // arrange
        String invalidBookId = "invalidBookId";

        // mock
        PaginatedQueryList<CatalogItemVersion> paginatedQueryListMock = Mockito.mock(PaginatedQueryList.class);
        paginatedQueryListMock.clear();

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryListMock);

        // act & assert
        assertThrows(BookNotFoundException.class, () -> {
            catalogDao.removeBookFromCatalog(invalidBookId);
        });

        // verify
        verify(dynamoDbMapper, never()).save(any(CatalogItemVersion.class));

    }

    @Test
    public void removeBookFromCatalog_bookIsAlreadyInactive_throwsException() throws BookNotFoundException {
        // arrange
        String bookId = "bookId";
        CatalogItemVersion mockedVersion = new CatalogItemVersion();
        mockedVersion.setBookId(bookId);
        mockedVersion.setInactive(true);

        // mock
        PaginatedQueryList<CatalogItemVersion> paginatedQueryListMock = Mockito.mock(PaginatedQueryList.class);
        when(paginatedQueryListMock.size()).thenReturn(1);
        when(paginatedQueryListMock.get(0)).thenReturn(mockedVersion);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryListMock);

        // act & assert
        assertThrows(BookNotFoundException.class, () -> {
            catalogDao.removeBookFromCatalog(bookId);
        });

        // verify
        verify(dynamoDbMapper, never()).save(any(CatalogItemVersion.class));
    }

    @Test
    public void removeBookFromCatalog_bookIsActive_returnBookMarkedAsInactive() {
        // arrange
        String validBookId = "validBookId";
        CatalogItemVersion mockedVersion = new CatalogItemVersion();
        mockedVersion.setBookId(validBookId);
        mockedVersion.setInactive(false);

        // mock
        PaginatedQueryList<CatalogItemVersion> paginatedQueryListMock = Mockito.mock(PaginatedQueryList.class);
        when(paginatedQueryListMock.size()).thenReturn(1);
        when(paginatedQueryListMock.get(0)).thenReturn(mockedVersion);

        when(dynamoDbMapper.query(eq(CatalogItemVersion.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryListMock);

        // act
        catalogDao.removeBookFromCatalog(validBookId);

        // capture
        ArgumentCaptor<CatalogItemVersion> captor = ArgumentCaptor.forClass(CatalogItemVersion.class);
        verify(dynamoDbMapper).save(captor.capture());

        // assert capture
        CatalogItemVersion capturedVersion = captor.getValue();
        assertTrue(capturedVersion.isInactive());
    }

}