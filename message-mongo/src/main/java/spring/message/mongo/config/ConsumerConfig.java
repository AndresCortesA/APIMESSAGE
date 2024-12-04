package spring.message.mongo.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

import spring.message.sql.message_sql.model.MessageDocument;
import spring.message.sql.message_sql.model.MessageRequest;

@Component
public class ConsumerConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "messageQueue")
    public void processMessage(Message message) {
        String payload = new String(message.getBody());
        long timestamp = (long) message.getMessageProperties().getHeaders().get("timestamp");

        // Convertir el payload JSON a un objeto MessageRequest
        MessageRequest request;
        try {
            request = objectMapper.readValue(payload, MessageRequest.class);
        } catch (Exception e) {
            // Manejar el error de deserialización
            MessageDocument errorDoc = new MessageDocument();
            errorDoc.setOrigin(null);
            errorDoc.setDestination(null);
            errorDoc.setMessageType(null);
            errorDoc.setContent(payload);
            errorDoc.setTimestamp(timestamp);
            errorDoc.setProcessingTime(System.currentTimeMillis() - timestamp);
            errorDoc.setCreatedDate(Instant.now());
            errorDoc.setError("Error al deserializar el mensaje: " + e.getMessage());
            mongoTemplate.save(errorDoc);
            return;
        }

        // Validar si el destinatario ya ha alcanzado el límite de 3 mensajes en 24 horas
        Instant twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60);
        long count = mongoTemplate.count(Query.query(Criteria.where("destination").is(request.getDestination())
                        .and("createdDate").gte(twentyFourHoursAgo)),
                MessageDocument.class);

        if (count >= 3) {
            // Si el destinatario ya ha alcanzado el límite, registrar el error y no procesar el mensaje
            MessageDocument errorMessage = new MessageDocument();
            errorMessage.setOrigin(request.getOrigin());
            errorMessage.setDestination(request.getDestination());
            errorMessage.setMessageType(request.getMessageType().name());
            errorMessage.setContent(request.getContent());
            errorMessage.setTimestamp(timestamp);
            errorMessage.setProcessingTime(System.currentTimeMillis() - timestamp);
            errorMessage.setCreatedDate(Instant.now());
            errorMessage.setError("Límite de mensajes alcanzado");
            mongoTemplate.save(errorMessage);
            return;
        }

        // Si no ha alcanzado el límite, continuar con el procesamiento
        MessageDocument doc = new MessageDocument();
        doc.setOrigin(request.getOrigin());
        doc.setDestination(request.getDestination());
        doc.setMessageType(request.getMessageType().name());
        doc.setContent(request.getContent());
        doc.setTimestamp(timestamp);
        doc.setProcessingTime(System.currentTimeMillis() - timestamp);
        doc.setCreatedDate(Instant.now());

        mongoTemplate.save(doc);
    }
}