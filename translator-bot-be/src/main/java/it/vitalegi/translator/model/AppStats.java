package it.vitalegi.translator.model;

import lombok.Data;

import java.time.Instant;

@Data
public class AppStats {
    Instant applicationStart;
    Instant applicationReady;
}
