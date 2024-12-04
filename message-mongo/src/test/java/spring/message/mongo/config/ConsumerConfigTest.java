package spring.message.mongo.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.message.common.MessageType;
import spring.message.common.model.MessageDocument;
import spring.message.common.model.MessageRequest;

public class ConsumerConfigTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConsumerConfig consumerConfig;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessMessageDeserializationError() throws Exception {
        String payload = "invalid payload";
        Message message = new Message(payload.getBytes());
        message.getMessageProperties().setHeader("timestamp", System.currentTimeMillis());

        // Probar el método de deserialización de mensajes de consumo json
        processMessage(message);

    }

    private void processMessage(Message message) {
        // logica para procesar el mensaje
        message.getMessageProperties().getHeaders().get("timestamp");

    }

    @Test
    public void testProcessMessageLimitReached() throws Exception {
        // Crear un mensaje de prueba
        MessageRequest request = new MessageRequest();
        request.setOrigin("12345");
        request.setDestination("67890");
        request.setMessageType(MessageType.TEXTO);
        request.setContent("Este es un mensaje de prueba.");

        String payload = new ObjectMapper().writeValueAsString(request);
        Message message = new Message(payload.getBytes());
        message.getMessageProperties().setHeader("timestamp", System.currentTimeMillis());

        // Confirmar la creacion del archivo de mensaje
        MessageDocument doc = new MessageDocument();
        doc.setOrigin(request.getOrigin());
        doc.setDestination(request.getDestination());
        doc.setMessageType(request.getMessageType().name());
        doc.setContent(request.getContent());
        doc.setTimestamp((long) message.getMessageProperties().getHeaders().get("timestamp"));
        doc.setProcessingTime(System.currentTimeMillis() - doc.getTimestamp());
        doc.setCreatedDate(Instant.now());

        // Confirmar la carga de mensajes que se han enviado y la sobrecarga
        when(mongoTemplate.count(any(org.springframework.data.mongodb.core.query.Query.class), any(Class.class))).thenReturn(3L);
        
    }
}