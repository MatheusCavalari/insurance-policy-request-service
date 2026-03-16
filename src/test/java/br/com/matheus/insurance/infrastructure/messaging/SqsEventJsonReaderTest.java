package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.infrastructure.messaging.dto.PaymentProcessedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SqsEventJsonReaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final SqsEventJsonReader jsonReader = new SqsEventJsonReader(objectMapper);

    @Test
    void should_parse_valid_json_message() {
        String rawMessage = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "requestId": "22222222-2222-2222-2222-222222222222",
                  "status": "APPROVED",
                  "occurredAt": "2026-03-15T04:20:00Z"
                }
                """;

        PaymentProcessedEvent event = jsonReader.read(rawMessage, PaymentProcessedEvent.class);

        assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), event.eventId());
        assertEquals(UUID.fromString("22222222-2222-2222-2222-222222222222"), event.requestId());
        assertEquals("APPROVED", event.status());
        assertEquals(Instant.parse("2026-03-15T04:20:00Z"), event.occurredAt());
    }

    @Test
    void should_normalize_backticks_and_outer_single_quotes() {
        String rawMessage = """
                '{
                  `eventId`: `11111111-1111-1111-1111-111111111111`,
                  `requestId`: `22222222-2222-2222-2222-222222222222`,
                  `status`: `APPROVED`,
                  `occurredAt`: `2026-03-15T04:20:00Z`
                }'
                """;

        PaymentProcessedEvent event = jsonReader.read(rawMessage, PaymentProcessedEvent.class);

        assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), event.eventId());
        assertEquals("APPROVED", event.status());
    }

    @Test
    void should_throw_illegal_argument_when_payload_is_invalid() {
        String rawMessage = """
                {
                  eventId: 123,
                  requestId: 456
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jsonReader.read(rawMessage, PaymentProcessedEvent.class)
        );

        assertEquals("Invalid SQS message payload", exception.getMessage());
        assertNotNull(exception.getCause());
    }
}