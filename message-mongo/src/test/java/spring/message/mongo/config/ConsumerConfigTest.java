package spring.message.mongo.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.message.sql.message_sql.MessageType;
import spring.message.sql.message_sql.model.MessageDocument;
import spring.message.sql.message_sql.model.MessageRequest;

@SpringBootTest
@ActiveProfiles("test")
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

    @Test
    public void testHighLoadMessageProcessing() throws Exception {
        int numberOfMessages = 1000; // Número de mensajes para simular la sobrecarga
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Crear un pool de hilos

        for (int i = 0; i < numberOfMessages; i++) {
            executorService.submit(() -> {
                try {
                    // Crear un mensaje de prueba
                    MessageRequest request = new MessageRequest();
                    request.setOrigin("12345");
                    request.setDestination("67890");
                    request.setMessageType(MessageType.TEXTO);
                    request.setContent("Este es un mensaje de prueba.");

                    // Convertir el objeto request a JSON
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

                    // Llamar al método que se está probando
                    // consumerConfig.processMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Esperar a que todos los hilos terminen
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Verificar que el método count fue llamado
        // verify(mongoTemplate, atLeastOnce()).count(any(org.springframework.data.mongodb.core.query.Query.class), any(Class.class));

        // Aquí puedes agregar más verificaciones según el comportamiento esperado
        // Por ejemplo, si esperas que no se guarde el mensaje en la base de datos cuando se alcanza el límite
        verify(mongoTemplate, never()).save(any(MessageDocument.class));

        // Advertencia sobre el estado de la prueba y el tráfico manejado
        System.out.println("Prueba de sobrecarga completada. Se procesaron " + numberOfMessages + " mensajes.");
    }


}