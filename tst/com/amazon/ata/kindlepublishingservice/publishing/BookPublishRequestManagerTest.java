package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.recommendationsservice.types.BookGenre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BookPublishRequestManagerTest {

    @Mock
    private Queue<BookPublishRequest> bookPublishRequestQueue;

    @InjectMocks
    private BookPublishRequestManager bookPublishRequestManager;

    @BeforeEach
    public void setup() {
        initMocks(this);
    }

    @Test
    public void addBookPublishRequest_goodBookPublishRequest_publishesRequest() {
        // given
        BookPublishRequest bookPublishRequest = BookPublishRequest.builder()
                .withBookId("1")
                .withPublishingRecordId("1")
                .withAuthor("Muhammad\'s Companions")
                .withGenre(BookGenre.HISTORY)
                .withTitle("Quran")
                .withText("In the Name of Allah, the Most Gracious, the Most Merciful.")
                .build();

        // when
        bookPublishRequestManager.addBookPublishRequest(bookPublishRequest);

        // then
        verify(bookPublishRequestQueue).add(bookPublishRequest);
    }

    @Test
    public void getBookPublishRequestToProcess_queueIsNotEmpty_returnsFirstBookPublishRequest() {
        // given
        BookPublishRequest bookPublishRequest = BookPublishRequest.builder()
                .withBookId("2")
                .withPublishingRecordId("2")
                .withAuthor("Kristen R. Ghodsee")
                .withGenre(BookGenre.ROMANCE)
                .withTitle("Why Women Have Better Sex Under Socialism And Other Arguments for " +
                        "Economic Independence")
                .withText("With acumen and wit, [Ghodsee] lsys bare the inequities women face " +
                        "under capitalism.")
                .build();

        when(bookPublishRequestQueue.isEmpty()).thenReturn(false);
        when(bookPublishRequestQueue.poll()).thenReturn(bookPublishRequest);

        // when
        BookPublishRequest result = bookPublishRequestManager.getBookPublishRequestToProcess();

        // then
        assertNotNull(result);
        assertEquals(bookPublishRequest, result);
        verify(bookPublishRequestQueue).poll();
    }

    @Test
    public void getBookPublishRequestToProcess_queueIsEmpty_returnsNull() {
        // given
        when(bookPublishRequestQueue.isEmpty()).thenReturn(true);

        // when
        BookPublishRequest result = bookPublishRequestManager.getBookPublishRequestToProcess();

        // then
        assertNull(result);
        verify(bookPublishRequestQueue).isEmpty();
        verifyNoMoreInteractions(bookPublishRequestQueue);
    }

}
