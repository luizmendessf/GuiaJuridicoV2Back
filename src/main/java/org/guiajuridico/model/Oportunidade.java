package org.guiajuridico.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "oportunidades")
@Getter
@Setter
public class Oportunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String company;
    private String location;

    @Column(nullable = false)
    private String type;

    private Date openingDate;
    private Date closingDate;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSON")
    private String requirements;

    private String salary;
    private String image;

    @Column(length = 2048)
    private String applicationLink;

    @Column(name = "data_criacao", updatable = false, insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    private Usuario criadoPor;
}