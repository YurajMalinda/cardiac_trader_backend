package com.scu.uob.dsa.cardiac_trader_backend.exception;

/**
 * Exception thrown when external API calls fail
 */
public class ExternalAPIException extends RuntimeException {
    private String apiName;
    private Integer statusCode;

    public ExternalAPIException(String message) {
        super(message);
    }

    public ExternalAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalAPIException(String apiName, String message, Integer statusCode) {
        super(message);
        this.apiName = apiName;
        this.statusCode = statusCode;
    }

    public String getApiName() {
        return apiName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}

