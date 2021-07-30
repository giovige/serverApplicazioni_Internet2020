package it.polito.ai.laboratorio3.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    String id;
    Long teamId;
    String sId;
    String courseName;
    Timestamp expiryDate;
    boolean confirmation;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Student student;

    public void setStudent(Student student){
        this.student = student;
        student.addRequest(this);
    }
}
