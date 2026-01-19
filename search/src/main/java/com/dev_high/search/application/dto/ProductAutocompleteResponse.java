package com.dev_high.search.application.dto;
import java.util.List;

public record ProductAutocompleteResponse(
        List<String> suggestions
) {
    public static ProductAutocompleteResponse of(List<String> suggestions) {
        return new ProductAutocompleteResponse(suggestions);
    }
}
