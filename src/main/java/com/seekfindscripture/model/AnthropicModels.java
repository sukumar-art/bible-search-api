package com.seekfindscripture.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class AnthropicModels {

    @Data
    public static class AnthropicRequest {
        private String model;

        @JsonProperty("max_tokens")
        private int maxTokens;

        private String system;
        private List<Message> messages;

        @Data
        public static class Message {
            private String role;
            private String content;

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }
        }
    }

    @Data
    public static class AnthropicResponse {
        private String id;
        private String type;
        private String role;
        private List<ContentBlock> content;
        private String model;

        @JsonProperty("stop_reason")
        private String stopReason;

        private Usage usage;
        private Error error;

        @Data
        public static class ContentBlock {
            private String type;
            private String text;
        }

        @Data
        public static class Usage {
            @JsonProperty("input_tokens")
            private int inputTokens;

            @JsonProperty("output_tokens")
            private int outputTokens;
        }

        @Data
        public static class Error {
            private String type;
            private String message;
        }

        public String extractText() {
            if (content == null) return "";
            return content.stream()
                    .filter(b -> "text".equals(b.getType()))
                    .map(ContentBlock::getText)
                    .reduce("", String::concat);
        }
    }
}
