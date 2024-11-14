package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.inject.Inject;

import static org.mockito.MockitoAnnotations.initMocks;

public class RemoveBookFromCatalogActivityTest {

    @Mock
    private CatalogDao catalogDao;

    @InjectMocks
    private RemoveBookFromCatalogActivity removeBookFromCatalogActivity;

    @BeforeEach
    public void setup() {
        initMocks(this);
    }

    @Test
    public void execute_invalidBookIdInRequest_throwsException() throws BookNotFoundException {

    }

}
