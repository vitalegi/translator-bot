package it.vitalegi.translator.integration.aws.translate;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;

@Slf4j
@Service
public class AwsTranslateImpl {

    TranslateClient translateClient;

    public AwsTranslateImpl(@Value("${AWS_REGION}") Region region) {
        log.info("Init translateClient, region={}", region);
        translateClient = TranslateClient.builder().credentialsProvider(EnvironmentVariableCredentialsProvider.create()).region(region).build();
        log.info("Created translateClient");
    }

    @PostConstruct
    public void execute() {
        //log.info("Translation: {}", translate("it", "en", "Ciao!"));
    }

    public String translate(String sourceLanguage, String targetLanguage, String message) {
        var textRequest = TranslateTextRequest.builder().sourceLanguageCode(sourceLanguage).targetLanguageCode(targetLanguage).text(message).build();
        return translateClient.translateText(textRequest).translatedText();
    }
}
