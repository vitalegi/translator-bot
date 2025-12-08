package it.vitalegi.translator.exception;

import lombok.Getter;

@Getter
public class UnauthorizedDiscordAccessException extends RuntimeException {

    public UnauthorizedDiscordAccessException(String message) {
        super(message);
    }
}
