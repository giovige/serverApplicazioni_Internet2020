package it.polito.ai.laboratorio3.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vm {

    public enum stati {Accesa, Spenta}

    @Id
    @GeneratedValue
    private Long id;
    private int vcpu;
    private int GBDisk;
    private int GBRam;
    private stati status;
    private String idCreatore;
    @Lob
    private byte[] screenVm;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private Team team;
    //il corso lo prende dal team

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "students_vms",joinColumns = @JoinColumn(name = "vm_id"),inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> owners = new ArrayList<>();

    public void addOwner( Student student){
        if(!owners.contains(student)){
            owners.add(student);
            student.addVm(this);
        }
    }

    public void changeOwnerList(List<Student> studentList){
        for(Student st: owners){
            st.removeVm(this);
        }
        owners.clear();
        owners = studentList;
        for(Student st: owners){
            st.addVm(this);
        }
    }

    public void removeTeam(){
        owners.clear();
        this.team = null;
    }
}
