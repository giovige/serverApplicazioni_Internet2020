package it.polito.ai.laboratorio3.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    private String name;
    private int min;
    private int max;
    private boolean enabled;

    private int modelVM_cpu;
    private int modelVM_GBDisk;
    private int modelVM_GBRam;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();

    @ManyToMany(mappedBy = "courses")
    private List<Docente> docenti = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = {CascadeType.REMOVE})
    private List<Team> teams = new ArrayList<>();

    //Progetto
    @OneToMany(mappedBy = "course", cascade = {CascadeType.REMOVE})
    private List<Task> tasks = new ArrayList<>();

    public void addStudent(Student student){
        students.add(student);
        student.getCourses().add(this);
    }

    public void addDocente (Docente docente){
        if(!docenti.contains(docente))
            docenti.add(docente);
        //docente.addCourse(this);
    }

    public void addTeam(Team team){
        team.setCourse(this);
    }

    public void unsubscribe(Student student) {
        if(!this.students.contains(student))
            return;
        this.students.remove(student);
        student.unsubscribe(this);
    }

    public void addTask(Task task) {
        if(!this.tasks.contains(task))
            this.tasks.add(task);
    }

    public void deleteDependences(){
        for(Student s: students)
            s.removeCourse(this);
        for(Docente d: docenti)
            d.removeCourse(this);


        for(Team t: teams)
            t.removeCourse();
        for (Task ta: tasks)
            ta.removeCourse();


    }
}
