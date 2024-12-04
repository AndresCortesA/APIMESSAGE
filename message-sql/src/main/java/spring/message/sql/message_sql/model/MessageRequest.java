package spring.message.sql.message_sql.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import spring.message.sql.message_sql.MessageType;

public class MessageRequest {
    @NotEmpty(message = "El campo 'origin' no puede estar vacío")
    private String origin;
    
    @NotEmpty(message = "El campo 'destination' no puede estar vacío")
    private String destination;
    
    @NotNull(message = "El campo 'messageType' no puede estar vacío")
    private MessageType messageType;
    
    @NotEmpty(message = "El campo 'content' no puede estar vacío")
    private String content;

    // Getters y setters
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}