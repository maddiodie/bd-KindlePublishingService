package com.amazon.ata.kindlepublishingservice.models.response;

import com.amazon.ata.kindlepublishingservice.models.Book;

import java.util.Objects;

public class RemoveBookFromCatalogResponse {

    private Book book;

    public RemoveBookFromCatalogResponse(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    public void setBook() {
        this.book = book;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoveBookFromCatalogResponse)) return false;
        RemoveBookFromCatalogResponse that = (RemoveBookFromCatalogResponse) o;
        return Objects.equals(getBook(), that.getBook());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBook());
    }

    public RemoveBookFromCatalogResponse(Builder builder) {
        this.book = builder.book;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Book book;

        private Builder(){}

        public Builder withBook(Book book) {
            this.book = book;
            return this;
        }

        public RemoveBookFromCatalogResponse build() {
            return new RemoveBookFromCatalogResponse(this);
        }
    }

}
