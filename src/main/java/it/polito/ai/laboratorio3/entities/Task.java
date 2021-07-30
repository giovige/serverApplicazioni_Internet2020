package it.polito.ai.laboratorio3.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Task {

    @Id
    @GeneratedValue
    private Long id;
    Timestamp dataRilascio;
    Timestamp dataScadenza;
    @Lob
    private byte[] description;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Course course;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Docente docente;

    @OneToMany(mappedBy = "task", cascade = {CascadeType.REMOVE})
    private List<Essay> essays = new ArrayList<>();

    public void addEssay(Essay essay) {
        this.essays.add(essay);
    }
    public void removeCourse(){
        this.course = null;
        this.docente = null;
        for (Essay e: essays)
            e.removeTask();
    }
}
