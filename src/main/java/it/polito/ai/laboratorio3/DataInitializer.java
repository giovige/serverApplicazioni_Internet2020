package it.polito.ai.laboratorio3;

import it.polito.ai.laboratorio3.dtos.CourseDTO;
import it.polito.ai.laboratorio3.dtos.ProfessorDTO;
import it.polito.ai.laboratorio3.dtos.StudentDTO;
import it.polito.ai.laboratorio3.entities.Course;
import it.polito.ai.laboratorio3.entities.Team;
import it.polito.ai.laboratorio3.entities.User;
import it.polito.ai.laboratorio3.entities.Vm;
import it.polito.ai.laboratorio3.repositories.*;
import it.polito.ai.laboratorio3.services.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.CharChunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    UserRepository users;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    DocenteRepository docenteRepository;

    @Autowired
    TeamService teamService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    VmRepository vmRepository;


    @Override
    public void run(String... args) throws Exception {

        if(!users.existsById("s263206")) {
            this.users.save(User.builder()
                    .username("s263206")
                    .password(this.passwordEncoder.encode("loripsw"))
                    .roles(Arrays.asList("ROLE_STUDENT"))
                    .build()
            );
            StudentDTO dto = new StudentDTO();
            dto.setId("s263206");
            dto.setName("lorenzo");
            dto.setFirstName("delsordo");
            dto.setPhotoStudent(new byte[]{0});
            teamService.addStudent(dto, new byte[]{0});
        }

        if(!users.existsById("d1")) {
            this.users.save(User.builder()
                    .username("d1")
                    .password(this.passwordEncoder.encode("malnatipsw"))
                    .roles(Arrays.asList("ROLE_PROFESSOR"))
                    .build()
            );

            teamService.addProfessor(new ProfessorDTO("d1","giovanni","malnati", new byte[]{0}));
        }

        if(!users.existsById("d2")) {
            this.users.save(User.builder()
                    .username("d2")
                    .password(this.passwordEncoder.encode("cabodipsw"))
                    .roles(Arrays.asList("ROLE_PROFESSOR"))
                    .build()
            );

            teamService.addProfessor(new ProfessorDTO("d2","gianpiero","cabodi",new byte[]{0}));
        }

        if(!courseRepository.existsById("PDS")){
            this.courseRepository.save(Course.builder()
            .name("PDS")
            .min(1)
            .max(5)
            .students(Arrays.asList(studentRepository.getOne("s263206")))
            .enabled(true)
            .docenti(Arrays.asList(docenteRepository.getOne("d2")))
            .build());
        }

        if(!teamRepository.existsById(1L)){
            this.teamRepository.save(Team.builder()
                    .id(1L)
                                .name("TeamProva")
                                .status(1)
                                .course(courseRepository.getOne("PDS"))
                                .members(Arrays.asList(studentRepository.getOne("s263206")))
                                .GBDiskTot(10)
                                .GBDiskUsati(0)
                                .GBRamTot(10)
                                .GBRamUsati(0)
                                .vcpuTot(10)
                                .vcpuUsati(0)
                                .build());
        }

        if(!vmRepository.existsById(1L)){
            this.vmRepository.save(Vm.builder()
                    .id(1L)
            .GBDisk(1)
            .GBRam(1)
            .vcpu(1)
            .status(Vm.stati.Accesa)
            .owners(Arrays.asList(studentRepository.getOne("s263206")))
            .team(teamRepository.getOne(1L))
            .build());
        }
        log.debug("printing all users...");
        this.users.findAll().forEach(v -> log.debug(" User :" + v.toString()));
    }
}
