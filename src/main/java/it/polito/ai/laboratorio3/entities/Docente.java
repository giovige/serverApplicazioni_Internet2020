package it.polito.ai.laboratorio3.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
public class Docente{

    @Id
    private String id;
    private String name;
    private String firstName;
    @Lob
    private byte[] photoDocente;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    @JoinTable(name = "docente_course",joinColumns = @JoinColumn(name = "docente_id"),inverseJoinColumns = @JoinColumn(name = "course_name"))
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "docente")
    List<Task> tasks = new ArrayList<>();

    public void addCourse(Course course){
        if(courses.contains(course))
            return;

        courses.add(course);
        course.addDocente(this);
    }

    public void removeCourse(Course course){
        if(courses.contains(course))
            courses.remove(course);
    }

}
