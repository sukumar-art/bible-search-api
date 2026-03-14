package com.seekfindscripture.model;

import lombok.Data;

import java.util.List;

@Data
public class BibleSearchResult {

    private PrimaryVerse primary;
    private List<RelatedVerse> related;
    private String theme;
    private String insight;
    private Versions versions;

    @Data
    public static class PrimaryVerse {
        private String reference;
        private String text;
        private String v2;
        private String v3;
        private String context;
        private String testament;
    }

    @Data
    public static class RelatedVerse {
        private String reference;
        private String text;
        private String connection;
    }

    @Data
    public static class Versions {
        private String v1;
        private String v2;
        private String v3;
    }
}
