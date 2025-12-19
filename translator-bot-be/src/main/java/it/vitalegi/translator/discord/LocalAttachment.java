package it.vitalegi.translator.discord;

import java.nio.file.Path;

public record LocalAttachment(long id, String name, Path localRef) {
}
