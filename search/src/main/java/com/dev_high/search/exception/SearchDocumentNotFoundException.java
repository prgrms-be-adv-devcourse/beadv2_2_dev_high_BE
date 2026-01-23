package com.dev_high.search.exception;

public class SearchDocumentNotFoundException extends Exception {
    public SearchDocumentNotFoundException(String productId) {
        super(String.format("SearchDocumentNotFound: productId=%s", productId));
    }
}
