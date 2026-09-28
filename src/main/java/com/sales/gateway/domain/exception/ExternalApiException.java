package com.sales.gateway.domain.exception;

public class ExternalApiException extends DomainException {

    private final Integer httpStatus;

    public ExternalApiException(String message, Integer httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ExternalApiException(String message, Integer httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public boolean isTransient() {
        return httpStatus == null || httpStatus >= 500;
    }
}
