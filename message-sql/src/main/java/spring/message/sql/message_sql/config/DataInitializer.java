package spring.message.sql.message_sql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import spring.message.sql.message_sql.model.OriginModel;
import spring.message.sql.message_sql.repository.OriginRepository;

@Configuration
public class DataInitializer {

    @Autowired
    private OriginRepository originRepository;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (originRepository.count() == 0) {
                originRepository.save(new OriginModel("12345", "Línea de origen 1"));
                originRepository.save(new OriginModel("23456", "Línea de origen 2"));
                originRepository.save(new OriginModel("34567", "Línea de origen 3"));
                originRepository.save(new OriginModel("45678", "Línea de origen 4"));
                originRepository.save(new OriginModel("56789", "Línea de origen 5"));
            }
        };
    }
}