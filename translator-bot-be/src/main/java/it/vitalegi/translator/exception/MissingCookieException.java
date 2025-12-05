package it.vitalegi.translator.exception;

public class MissingCookieException extends RuntimeException {
    public MissingCookieException(String message) {
        super(message);
    }
}
