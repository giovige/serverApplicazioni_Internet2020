package it.polito.ai.laboratorio3.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
public class Image {
    @Id
    @GeneratedValue
    private Long id;
    private String filename;
    private Timestamp creationDate;
    private String idCreator;

    @Lob
    private byte[] data;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Essay essay;

    public Image(){ }
}
