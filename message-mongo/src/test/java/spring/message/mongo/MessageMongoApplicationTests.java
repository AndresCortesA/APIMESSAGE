package spring.message.mongo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.core.Message;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import spring.message.mongo.config.ConsumerConfig;
import spring.message.sql.message_sql.MessageType;
import spring.message.sql.message_sql.model.MessageDocument;
import spring.message.sql.message_sql.model.MessageRequest;

@SpringBootTest
@ActiveProfiles("test")
public class MessageMongoApplicationTests {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConsumerConfig consumerConfig;


	@Test
	public void testHighLoadMessageProcessing() throws Exception {
		int numberOfMessages = 1000; 
		ExecutorService executorService = Executors.newFixedThreadPool(10);

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

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		// Esperar a que todos los hilos terminen
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);

		verify(mongoTemplate, never()).save(any(MessageDocument.class));

		System.out.println("Prueba de sobrecarga completada. Se procesaron " + numberOfMessages + " mensajes.");
	}


}