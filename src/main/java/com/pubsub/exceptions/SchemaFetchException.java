package com.pubsub.exceptions;

public class SchemaFetchException extends RuntimeException {
    public SchemaFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}