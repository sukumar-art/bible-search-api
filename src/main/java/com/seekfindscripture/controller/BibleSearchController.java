package com.seekfindscripture.controller;

import com.seekfindscripture.model.BibleSearchResult;
import com.seekfindscripture.model.SearchRequest;
import com.seekfindscripture.service.BibleSearchException;
import com.seekfindscripture.service.BibleSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BibleSearchController {

    private final BibleSearchService bibleSearchService;

    /**
     * POST /api/search
     * Called by the React frontend to search Bible verses.
     */
    @PostMapping(
        value = "/search",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public ResponseEntity<?> search(@Valid @RequestBody SearchRequest request) {
        log.info("Search: query='{}' lang='{}'", request.getQuery(), request.getLangLabel());
        try {
            BibleSearchResult result = bibleSearchService.search(request);
            return ResponseEntity.ok(result);
        } catch (BibleSearchException e) {
            log.error("Search failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/health
     * Used by Railway to verify the app is running.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Seek & Find Scripture API"
        ));
    }
}
