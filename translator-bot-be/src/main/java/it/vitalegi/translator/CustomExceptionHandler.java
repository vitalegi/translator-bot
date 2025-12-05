package it.vitalegi.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.vitalegi.translator.exception.MissingCookieException;
import it.vitalegi.translator.exception.UnauthorizedAccessException;
import it.vitalegi.translator.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@ControllerAdvice
public class CustomExceptionHandler {
    ObjectMapper mapper;
    List<String> skipClasses;
    boolean singleLine;

    public CustomExceptionHandler(ObjectMapper mapper, @Value("${exception.logging.skip-classes}") List<String> skipClasses, @Value("${exception.logging.single-line}") boolean singleLine) {
        this.mapper = mapper;
        this.skipClasses = Objects.requireNonNullElse(skipClasses, Collections.emptyList());
        this.singleLine = singleLine;
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handle(Throwable e) {
        log(e);
        return new ResponseEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handle(UnauthorizedAccessException e) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        return new ResponseEntity<>(new ErrorResponse("UnauthorizedAccessException", ""), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingCookieException.class)
    public ResponseEntity<ErrorResponse> handle(MissingCookieException e) {
        log.debug(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(e.getClass().getSimpleName(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected void log(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        String lineSeparator = ">>";
        if (!singleLine) {
            lineSeparator = "\n";
        }

        String error = Stream.of(sw.toString().split("\n")).filter(s -> skipClasses.stream().noneMatch(s::contains)).collect(Collectors.joining(lineSeparator));
        log.error(error);
    }
}
