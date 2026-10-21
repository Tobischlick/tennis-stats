package com.tennisstats.backend.api;

/** Matches the {@code ErrorResponse} schema in api/openapi.yaml. */
public record ErrorResponse(String message) {
}
