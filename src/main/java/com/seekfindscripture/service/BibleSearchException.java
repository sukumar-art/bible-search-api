package com.seekfindscripture.service;

public class BibleSearchException extends RuntimeException {
    public BibleSearchException(String message) {
        super(message);
    }
    public BibleSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
