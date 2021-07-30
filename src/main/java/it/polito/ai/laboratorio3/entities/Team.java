package it.polito.ai.laboratorio3.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int status;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "team_student", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> members = new ArrayList<>();

    //PROGETTO
    private int vcpuTot;
    private int GBDiskTot;
    private int GBRamTot;
    private int vcpuUsati;
    private int GBDiskUsati;
    private int GBRamUsati;
    private int maxVmAccese;
    private int vmAccese;
    private String idCreator;

    @OneToMany(mappedBy = "team", cascade = {CascadeType.REMOVE})
    private List<Vm> vms = new ArrayList<>();

    public void setCourse(Course course){
        if (course == null){
            this.course.getTeams().remove(this);
            this.course = null;
        }
        else {
            this.course = course;
            course.getTeams().add(this);
        }
    }

    public void addMember(Student member){
        members.add(member);
        member.getTeams().add(this);
    }

    public void removeMember(Student student){
        //members.remove(student);
        student.getTeams().remove(this);
    }

    public void aggiornaRisorseTotali() {
        AtomicInteger vcputotali = new AtomicInteger();
        AtomicInteger gbdisktotali = new AtomicInteger();
        AtomicInteger gbramtotali = new AtomicInteger();
        vms.stream().forEach(v->{
                    vcputotali.set(vcputotali.get() + v.getVcpu());
                    gbdisktotali.set(gbdisktotali.get() + v.getGBDisk());
                    gbramtotali.set(gbramtotali.get() + v.getGBRam());
                });
        setGBDiskTot(gbdisktotali.get());
        setVcpuTot(vcputotali.get());
        setGBRamTot(gbramtotali.get());
    }

    public void removeCourse(){
        course = null;
        for (Vm v: vms)
            v.removeTeam();
        members.clear();
    }

}
