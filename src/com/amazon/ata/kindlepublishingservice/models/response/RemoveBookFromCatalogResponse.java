package com.amazon.ata.kindlepublishingservice.models.response;

import com.amazon.ata.kindlepublishingservice.models.Book;

import java.util.Objects;

public class RemoveBookFromCatalogResponse {

    private Book book;

    private String message;

    public RemoveBookFromCatalogResponse(String message, Book book) {
        this.message = message;
        this.book = book;
    }

    public RemoveBookFromCatalogResponse(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    public String getMessage() {
        return message;
    }

    public void setBook() {
        this.book = book;
    }

    public void setMessage() {
        this.message = message;
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
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Book book;

        private String message;

        private Builder(){}

        public Builder withBook(Book book) {
            this.book = book;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public RemoveBookFromCatalogResponse build() {
            return new RemoveBookFromCatalogResponse(this);
        }
    }

}
