package spring.message.sql.message_sql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import spring.message.sql.message_sql.model.OriginModel;

public interface OriginRepository extends JpaRepository<OriginModel, Long> {
    Optional<OriginModel> findByOriginNumber(String originNumber); 
}
