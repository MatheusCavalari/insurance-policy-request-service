package br.com.matheus.insurance.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SqsEventJsonReader {

    private final ObjectMapper objectMapper;

    public SqsEventJsonReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T read(String rawMessage, Class<T> targetType) {
        try {
            String normalized = normalize(rawMessage);
            return objectMapper.readValue(normalized, targetType);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid SQS message payload", ex);
        }
    }

    private String normalize(String rawMessage) {
        if (rawMessage == null) {
            return "";
        }

        String normalized = rawMessage.trim();

        if (normalized.startsWith("'") && normalized.endsWith("'") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }

        normalized = normalized.replace('`', '"');
        return normalized;
    }
}