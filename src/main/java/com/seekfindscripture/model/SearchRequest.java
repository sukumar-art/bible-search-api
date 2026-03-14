package com.seekfindscripture.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchRequest {

    @NotBlank(message = "query must not be blank")
    private String query;

    private String langLabel;
    private String langNative;
    private BibleVersions versions;

    @Data
    public static class BibleVersions {
        private String v1;
        private String v2;
        private String v3;
    }
}
