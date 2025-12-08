package it.vitalegi.translator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "message_translation")
@Table(name = "message_translation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTranslatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String sourceLanguage;
    private String targetLanguage;
    private String sourceMessage;
    private String targetMessage;
    private int sourceLength;
    private int targetLength;
    private int hits;
    Instant creationDate;
    Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageTranslatorEntity that = (MessageTranslatorEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "MessageTranslatorEntity{" + "id='" + id + '\'' + ", sourceLanguage='" + sourceLanguage + '\'' + ", targetLanguage='" + targetLanguage + '\'' + ", sourceMessage='" + sourceMessage + '\'' + ", targetMessage='" + targetMessage + '\'' + ", sourceLength=" + sourceLength + ", targetLength=" + targetLength + ", hits=" + hits + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
