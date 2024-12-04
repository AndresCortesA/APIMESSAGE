package spring.message.sql.message_sql.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "line_origin")
public class OriginModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_number", nullable = false, unique = true)
    private String originNumber;

    private String description;

    public OriginModel() {
    }

    public OriginModel(String originNumber, String description) {
        this.originNumber = originNumber;
        this.description = description;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginNumber() {
        return originNumber;
    }

    public void setOriginNumber(String originNumber) {
        this.originNumber = originNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OriginModel{" +
                "id=" + id +
                ", originNumber='" + originNumber + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}