package spring.message.sql.message_sql.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import spring.message.sql.message_sql.MessageType;
import spring.message.sql.message_sql.model.MessageDocument;
import spring.message.sql.message_sql.model.MessageRequest;
import spring.message.sql.message_sql.model.OriginModel;
import spring.message.sql.message_sql.repository.OriginRepository;

@RestController
@RequestMapping("/api/messages")
public class OriginController {

    @Autowired
    private OriginRepository repository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<String> sendMessage(@Valid @RequestBody MessageRequest request) {
        Optional<OriginModel> origin = repository.findByOriginNumber(request.getOrigin());

        if (origin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Línea de origen no autorizada");
        }

        // Validar que el contenido sea una URL si el tipo de mensaje es multimedia
        if ((request.getMessageType() == MessageType.IMAGEN || 
             request.getMessageType() == MessageType.VIDEO || 
             request.getMessageType() == MessageType.DOCUMENTO) && 
            !isValidURL(request.getContent())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El contenido debe ser una URL válida para tipos de mensaje multimedia");
        }

        // Convertir el objeto MessageRequest a JSON
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el mensaje");
        }

        // Enviar el mensaje a RabbitMQ
        Message message = MessageBuilder.withBody(messageJson.getBytes())
                .setHeader("timestamp", System.currentTimeMillis())
                .build();

        rabbitTemplate.convertAndSend("messageQueue", message);
        return ResponseEntity.ok("Mensaje enviado correctamente");
    }

    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<MessageDocument>> getMessagesByDestination(@PathVariable String destination) {
        String url = "http://localhost:8081/api/messages/destination/" + destination;
        ResponseEntity<List<MessageDocument>> response = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            null, 
            new ParameterizedTypeReference<List<MessageDocument>>() {}
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    private boolean isValidURL(String url) {
        String regex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return url.matches(regex);
    }
}