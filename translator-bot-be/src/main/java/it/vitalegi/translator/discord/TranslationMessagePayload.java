package it.vitalegi.translator.discord;

public record TranslationMessagePayload(TranslationMessageContext context, TranslationMessageRequest request,
                                        String targetMessage) {
}
