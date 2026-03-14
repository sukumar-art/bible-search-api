package com.seekfindscripture.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seekfindscripture.model.AnthropicModels.AnthropicRequest;
import com.seekfindscripture.model.AnthropicModels.AnthropicResponse;
import com.seekfindscripture.model.BibleSearchResult;
import com.seekfindscripture.model.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class BibleSearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api-key}")
    private String apiKey;

    @Value("${anthropic.base-url}")
    private String baseUrl;

    @Value("${anthropic.model}")
    private String model;

    @Value("${anthropic.max-tokens}")
    private int maxTokens;

    @Value("${anthropic.version}")
    private String anthropicVersion;

    public BibleSearchService(WebClient anthropicWebClient, ObjectMapper objectMapper) {
        this.webClient = anthropicWebClient;
        this.objectMapper = objectMapper;
    }

    public BibleSearchResult search(SearchRequest request) {
        String langLabel  = nvl(request.getLangLabel(), "English");
        String langNative = nvl(request.getLangNative(), "English");
        String v1 = versionsField(request, "v1", "KJV");
        String v2 = versionsField(request, "v2", "NIV");
        String v3 = versionsField(request, "v3", "ESV");

        String systemPrompt = buildSystemPrompt(langLabel, langNative, v1, v2, v3);
        String userMessage  = buildUserMessage(request.getQuery());

        log.debug("Searching: '{}' in language: {}", request.getQuery(), langLabel);

        AnthropicRequest anthropicRequest = new AnthropicRequest();
        anthropicRequest.setModel(model);
        anthropicRequest.setMaxTokens(maxTokens);
        anthropicRequest.setSystem(systemPrompt);
        anthropicRequest.setMessages(List.of(
                new AnthropicRequest.Message("user", userMessage)
        ));

        String rawResponse = callClaude(anthropicRequest);
        log.debug("Raw response (500 chars): {}", rawResponse.substring(0, Math.min(500, rawResponse.length())));

        BibleSearchResult result = parseResponse(rawResponse);

        BibleSearchResult.Versions versions = new BibleSearchResult.Versions();
        versions.setV1(v1);
        versions.setV2(v2);
        versions.setV3(v3);
        result.setVersions(versions);

        return result;
    }

    private String callClaude(AnthropicRequest request) {
        try {
            AnthropicResponse response = webClient
                    .post()
                    .uri(baseUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", anthropicVersion)
                    .header(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AnthropicResponse.class)
                    .block();

            if (response == null) {
                throw new BibleSearchException("Empty response from Claude API");
            }
            if (response.getError() != null) {
                throw new BibleSearchException("Claude API error: " + response.getError().getMessage());
            }

            String text = response.extractText();
            if (text.isBlank()) {
                throw new BibleSearchException("Claude returned empty content");
            }
            return text;

        } catch (WebClientResponseException e) {
            log.error("Claude API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BibleSearchException("Claude API error " + e.getStatusCode());
        }
    }

    private BibleSearchResult parseResponse(String raw) {
        String cleaned = stripMarkdownFences(raw).trim();

        try {
            return objectMapper.readValue(cleaned, BibleSearchResult.class);
        } catch (JsonProcessingException e) {
            log.warn("Initial JSON parse failed: {}. Trying salvage...", e.getMessage());
            String salvaged = extractJsonObject(cleaned);
            if (salvaged != null) {
                try {
                    return objectMapper.readValue(salvaged, BibleSearchResult.class);
                } catch (JsonProcessingException e2) {
                    log.error("Salvage parse failed: {}", e2.getMessage());
                }
            }
            throw new BibleSearchException("Failed to parse Claude response as JSON.");
        }
    }

    private String stripMarkdownFences(String text) {
        String result = text.trim();
        if (result.startsWith("```")) {
            int newline = result.indexOf('\n');
            if (newline != -1) result = result.substring(newline + 1);
        }
        if (result.endsWith("```")) {
            result = result.substring(0, result.lastIndexOf("```"));
        }
        return result.trim();
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) return null;
        return text.substring(start, end + 1);
    }

    private String buildSystemPrompt(String langLabel, String langNative,
                                     String v1, String v2, String v3) {
        return String.format("""
                You are a Bible scholar. Reply in %s (%s).
                Rules:
                - reference and testament fields must stay in English
                - All other fields must be written in %s
                - Provide full verse text, rich context, and detailed insight
                - Return exactly 4 related verses
                - The 'text' field = verse in %s version
                - The 'v2' field = same verse in %s version
                - The 'v3' field = same verse in %s version (omit if null)
                """,
                langLabel, langNative, langLabel, v1, v2, v3);
    }

    private String buildUserMessage(String query) {
        return String.format("""
                Find the best Bible verse for: "%s"

                Respond with ONLY valid JSON — no markdown, no code fences, no extra text:
                {"primary":{"reference":"","text":"","v2":"","v3":"","context":"","testament":""},"related":[{"reference":"","text":"","connection":""},{"reference":"","text":"","connection":""},{"reference":"","text":"","connection":""},{"reference":"","text":"","connection":""}],"theme":"","insight":""}
                """, query);
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    private String versionsField(SearchRequest req, String field, String fallback) {
        if (req.getVersions() == null) return fallback;
        return switch (field) {
            case "v1" -> nvl(req.getVersions().getV1(), fallback);
            case "v2" -> nvl(req.getVersions().getV2(), fallback);
            case "v3" -> req.getVersions().getV3();
            default   -> fallback;
        };
    }
}
