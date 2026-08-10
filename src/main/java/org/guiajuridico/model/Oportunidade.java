package org.guiajuridico.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "oportunidades")
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

    @Column(name = "data_criacao", updatable = false)
    @JsonIgnore
    private Timestamp dataCriacao;

    @Column(name = "data_atualizacao")
    @JsonIgnore
    private Timestamp dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id")
    @JsonIgnore
    private Usuario criadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por_id")
    @JsonIgnore
    private Usuario atualizadoPor;

    @PrePersist
    protected void onCreate() {
        Timestamp agora = new Timestamp(System.currentTimeMillis());
        if (dataCriacao == null) {
            dataCriacao = agora;
        }
        dataAtualizacao = agora;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = new Timestamp(System.currentTimeMillis());
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getOpeningDate() { return openingDate; }
    public void setOpeningDate(Date openingDate) { this.openingDate = openingDate; }
    public Date getClosingDate() { return closingDate; }
    public void setClosingDate(Date closingDate) { this.closingDate = closingDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getApplicationLink() { return applicationLink; }
    public void setApplicationLink(String applicationLink) { this.applicationLink = applicationLink; }
    public Timestamp getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Timestamp dataCriacao) { this.dataCriacao = dataCriacao; }
    public Timestamp getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(Timestamp dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
    public Usuario getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(Usuario atualizadoPor) { this.atualizadoPor = atualizadoPor; }
}
