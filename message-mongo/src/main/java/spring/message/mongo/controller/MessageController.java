package spring.message.mongo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import spring.message.sql.message_sql.model.MessageDocument;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<MessageDocument>> getMessagesByDestination(@PathVariable String destination) {
        List<MessageDocument> messages = mongoTemplate.find(Query.query(Criteria.where("destination").is(destination)), MessageDocument.class);
        return ResponseEntity.ok(messages);
    }
}