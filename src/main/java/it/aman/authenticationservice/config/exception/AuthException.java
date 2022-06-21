package it.aman.authenticationservice.config.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AuthException extends Exception {
    private static final long serialVersionUID = -5464526854597284442L;

    private final HttpStatus httpCode;
    private final int internalCode;
    private String errorMessage;
    private final List<String> errors;

    public AuthException(String message) {
        this(null, 500001, message);
    }

    public AuthException(HttpStatus httpCode, int internalCode, String message) {
        this(httpCode, internalCode, message, new ArrayList<>());
    }

    public AuthException(HttpStatus httpCode, int internalCode, String message, List<String> errors) {
        super();
        this.httpCode = httpCode;
        this.internalCode = internalCode;
        this.errorMessage = message;
        this.errors = errors;
    }

    public AuthException setErrorMessage(String message) {
        this.errorMessage = message;
        return this;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
