package it.polito.ai.laboratorio3.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Essay {

    public enum stati {Letto, Consegnato, Rivisto, Terminato}

    @Id
    @GeneratedValue
    private Long id;
    private Long voto;
    private stati stato;
    private String idStudente;
    private Timestamp lastModified;

    @OneToMany(mappedBy = "essay", cascade = {CascadeType.REMOVE})
    private List<Image> images = new ArrayList<>();

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Student student;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Task task;

    //corso e docente si possono prendere dal task
    public void setStudent(Student student){
        this.student = student;
        student.addEssay(this);
    }

    public void setTask(Task task){
        this.task = task;
        task.addEssay(this);
    }

    public void addImage(Image image){
        images.add(image);
        image.setEssay(this);
    }

    public void removeTask(){
        task = null;
    }

}
