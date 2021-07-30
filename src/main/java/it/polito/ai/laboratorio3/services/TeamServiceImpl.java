package it.polito.ai.laboratorio3.services;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.laboratorio3.dtos.*;
import it.polito.ai.laboratorio3.entities.*;
import it.polito.ai.laboratorio3.exceptions.*;
import it.polito.ai.laboratorio3.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    public SimpleMailMessage refuseTemplateMessage(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "La richiesta per la creazione del team è stata rifiutata"
        );
        return message;
    }

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    DocenteRepository docenteRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EssayRepository essayRepository;

    @Autowired
    VmRepository vmRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    NotificationService notificationService;


    @Override
    public boolean addCourse(CourseDTO course, List<String> ids) {
        Course courseClass = modelMapper.map(course, Course.class);
        if (courseRepository.existsById(courseClass.getName()))
            return false;

        for(String id: ids){
            Optional<Docente> docenteOpt = docenteRepository.findById(id);
            if (!docenteOpt.isPresent())
                throw new DocenteNotFoundException();
            Docente docente = docenteOpt.get();

            docente.addCourse(courseClass);
        }
        return true;
    }

    @Override
    public Optional<CourseDTO> getCourse(String name) {
        return courseRepository.findById(name)
                .map(course -> modelMapper.map(course, CourseDTO.class));
    }

    @Override
    public List<CourseDTO> getCoursesByProf(String teacherId) {
        if (!docenteRepository.findById(teacherId).isPresent())
            throw new DocenteNotFoundException();
        return docenteRepository.findById(teacherId).get().getCourses()
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addStudent(StudentDTO student, byte[] studentImg){
        Student studentClass = modelMapper.map(student, Student.class);
        studentClass.setPhotoStudent(studentImg);
        if (studentRepository.existsById(studentClass.getId()))
            return false;
        studentRepository.save(studentClass);
        return true;
    }

    @Override
    public boolean addProfessor(ProfessorDTO professor) {
        Docente docente = modelMapper.map(professor,Docente.class);
        if (docenteRepository.existsById(docente.getId()))
            return false;
        docenteRepository.save(docente);
        return true;
    }

    @Override
    public Optional<StudentDTO> getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .map(student -> modelMapper.map(student,StudentDTO.class));
    }

        @Override
    public Optional<ProfessorDTO> getProf(String professorId) {
        return docenteRepository.findById(professorId)
                .map(professor -> modelMapper.map(professor,ProfessorDTO.class));
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(student -> modelMapper.map(student,StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getEnrolledStudents(String courseName) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();

        return courseRepository.findById(courseName)
                .get()
                .getStudents()
                .stream()
                .map(student -> modelMapper.map(student,StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addStudentToCourse(String studentId, String courseName) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();
        if (!studentRepository.findById(studentId).isPresent())
            throw new StudentNotFoundException();
        if(courseRepository.getOne(courseName).getStudents().contains(studentRepository.getOne(studentId)))
            return false;
        else
            courseRepository.getOne(courseName).addStudent(studentRepository.getOne(studentId));
            //studentRepository.getOne(studentId).addCourse(courseRepository.getOne(courseName));
        return true;
    }

    @Override
    public void enableCourse(String courseName, String docenteId) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();
        Course course = courseRepository.getOne(courseName);
        if(course.getDocenti().stream()
                .filter(doc-> doc.getId().equals(docenteId))
                .count() < 1)
            throw new DocenteHasNotPrivilegeException();
        course.setEnabled(true);
    }

    @Override
    public void disableCourse(String courseName, String docenteId) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();
        Course course = courseRepository.getOne(courseName);
        if(course.getDocenti().stream()
                .filter(doc-> doc.getId().equals(docenteId))
                .count() < 1)
            throw new DocenteHasNotPrivilegeException();
        course.setEnabled(false);
    }

    @Override
    public List<Boolean> addAll(List<StudentDTO> students){
        List<Boolean> results = new ArrayList<>();
        students.stream()
                .forEach(studentDTO -> {
                    if(addStudent(studentDTO, new byte[0]))
                        results.add(true);
                    else
                        results.add(false);
                });
        return results;
    }

    @Override
    public List<Boolean> enrollAll(List<String> studentIds, String courseName) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();
        List<Boolean> results = new ArrayList<>();
        studentIds.stream()
                .forEach(studentId ->{
                    if (addStudentToCourse(studentId,courseName))
                        results.add(true);
                    else
                        results.add(false);
                });
        return results;
    }

    @Override
    public List<Boolean> addAndEroll(Reader r, String courseName) {
        CsvToBean<StudentDTO> csvToBean = new CsvToBeanBuilder(r)
                .withType(StudentDTO.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        List<StudentDTO> students = csvToBean.parse();
        List<String> studentsIds = students.stream()
                .map(StudentDTO::getId)
                .collect(Collectors.toList());
        addAll(students);
        return enrollAll(studentsIds,courseName);
    }

    @Override
    public List<CourseDTO> getCourses(String studentId) {
        if (!studentRepository.findById(studentId).isPresent())
            throw new StudentNotFoundException();
        return studentRepository.findById(studentId).get().getCourses()
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamDTO> getTeamsForStudent(String studentId) {
        if (!studentRepository.findById(studentId).isPresent())
            throw new StudentNotFoundException();
        return studentRepository.findById(studentId).get().getTeams()
                .stream()
                .filter(t -> t.getStatus() == ATTIVO)
                .map(team -> modelMapper.map(team,TeamDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TeamDTO> getTeamForStudentByCourseName(String studentId, String name) {
        if (!studentRepository.findById(studentId).isPresent())
            throw new StudentNotFoundException();
        if(!courseRepository.existsById(name))
            throw new CourseNotFoundException();

        return studentRepository.findById(studentId).get().getTeams()
                .stream()
                .filter(t-> t.getCourse().getName().equals(name))
                .filter(t-> t.getStatus()==1)
                .map(team -> modelMapper.map(team,TeamDTO.class))
                .collect(Collectors.toList()).stream().findFirst();
    }

    @Override
    public List<StudentDTO> getMembers(Long teamId) {
        if (!teamRepository.findById(teamId).isPresent())
            throw new TeamNotFoundException();
        return teamRepository.findById(teamId).get().getMembers()
                .stream()
                .map(student -> modelMapper.map(student,StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TeamDTO proposeTeam(String courseId, String name, List<String> memberIds, String creator) {
        if (!courseRepository.findById(courseId).isPresent())
            throw new CourseNotFoundException();
        CourseDTO courseDTO = modelMapper.map( courseRepository.getOne(courseId) , CourseDTO.class );
        if (!courseDTO.isEnabled())
            throw new CourseNotEnabledException();
        int countMembersMax = courseRepository.getOne(courseId).getMax();
        int countMembersMin = courseRepository.getOne(courseId).getMin();
        if (memberIds.size() > countMembersMax || memberIds.size() < countMembersMin)
            throw new WrongTeamDimensionExcpetion();
        List<Team> tx = teamRepository.findTeamsByName(name).stream()
                .filter(txx -> txx.getCourse().getName().equals(courseId))
                .collect(Collectors.toList());
        if(!tx.isEmpty())
        //SE ENTRIAMO QUI, ESISTE GIA UN GRUPPO CON LO STESSO NOME
            throw new TeamNameAlreadyInCourseExceptions();

        memberIds.forEach( memberId -> {
            Student st;
            if(!studentRepository.existsById(memberId))
                throw new StudentNotFoundException();

            st = studentRepository.getOne(memberId);
                    List<CourseDTO> courses = getCourses(memberId);
                    if (!courses.contains(courseDTO))
                        throw new MemberNotInCourseException();

            long flag = st.getTeams().stream()
                    .filter(t->t.getStatus()==ATTIVO)
                    .filter(team -> team.getCourse().getName().equals(courseId))
                    .count();

            if (flag > 0)
                throw new TeamAlreadyInCourseException();

                  /*  boolean check= getTeamForCourse(courseId).containsAll(getTeamsForStudent(memberId));
                    if(check)
                        throw new TeamAlreadyInCourseException();*/
                } );
        /*long numDuplicati = memberIds.size() - (memberIds.stream().distinct().count());
        if (numDuplicati>0)
            throw new DuplicatesInListException();*/

        Team team = new Team();
        team.setCourse(courseRepository.getOne(courseId));
        team.setName(name);
        team.setIdCreator(creator);
        if(memberIds.size()==1)
            //TEAM COMPOSTO DA 1 PERSONA
            team.setStatus(ATTIVO);
        else
        team.setStatus(NON_ATTIVO);
        memberIds.stream().forEach(memberId -> {
            team.addMember(studentRepository.getOne(memberId));
        });
        //lasciamo 4 vm accese come limite di default
        team.setMaxVmAccese(4);
        team.setVmAccese(0);
        team.setVcpuUsati(0);
        team.setGBRamUsati(0);
        team.setGBDiskUsati(0);
        //questi li prendiamo dal modello del corso
        team.setGBRamTot(courseDTO.getModelVM_GBRam());
        team.setGBDiskTot(courseDTO.getModelVM_GBDisk());
        team.setVcpuTot(courseDTO.getModelVM_cpu());

        Team newteam = teamRepository.save(team);

        return modelMapper.map(newteam,TeamDTO.class);
    }

    @Override
    public List<TeamDTO> getTeamForCourse(String courseName) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();
        return courseRepository.findById(courseName).get().getTeams()
                .stream()
                //.filter(t-> t.getStatus() == ATTIVO)
                .map(team -> modelMapper.map(team,TeamDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsInTeams(String courseName) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();
        return courseRepository.getStudentsInTeams(courseName)
                .stream()
                .map(student -> modelMapper.map(student,StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getAvailableStudents(String courseName, String id) {
        if (!courseRepository.findById(courseName).isPresent())
            throw new CourseNotFoundException();

        List<StudentDTO> sNotInTeam = courseRepository.getStudentsNotInTeams(courseName).stream()
                .map(student -> modelMapper.map(student,StudentDTO.class))
                .collect(Collectors.toList());

        Set<StudentDTO> set = new LinkedHashSet<>(sNotInTeam);

        List<StudentDTO> sInTeamInactive = courseRepository.getStudentsInInactiveTeams(courseName).stream()
                .map(student -> modelMapper.map(student,StudentDTO.class))
                .collect(Collectors.toList());

        set.addAll(sInTeamInactive);
        set.removeIf(s -> s.getId().equals(id));

        return new ArrayList<>(set);
    }

    @Override
    public void activeTeam(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if(!teamOpt.isPresent())
            throw new TeamNotFoundException();
        teamOpt.get().setStatus(ATTIVO);
        tokenRepository.deleteTokenAfterActiveTeam(teamId);
        String courseName = teamOpt.get().getCourse().getName();
        List<Long> teamIds = new ArrayList<>();
        for( Student s: teamOpt.get().getMembers()){
            for(Team t: s.getTeams()){
                if(t.getCourse().getName().equals(courseName) && t.getStatus() == NON_ATTIVO)
                    teamIds.add(t.getId());
            }
        }
        for( Long id: teamIds) {
            List<Token> teamTokens = tokenRepository.findAllByTeamId(id);
            teamTokens.forEach(tk -> tokenRepository.delete(tk));
            evictTeam(id); }

    }

    @Override
    public void evictTeam(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if(!teamOpt.isPresent())
            return;
        Team team = teamOpt.get();
        List<Student> membersToDelete = team.getMembers();
        membersToDelete.forEach(
                s-> notificationService.sendMessage(s.getId()+"@studenti.polito.it",
                        "Team Rifiutato",refuseTemplateMessage().getText())
        );

        membersToDelete.forEach( member -> teamOpt.get().removeMember(member));
        teamRepository.delete(team);
    }

    @Override
    public List<TokenDTO> getRequestsForStudent(String id, String name) {
        tokenRepository.deleteExpiredToken(Timestamp.valueOf(LocalDateTime.now()));
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (!studentOpt.isPresent())
            throw new StudentNotFoundException();
        Student student = studentOpt.get();
       return student.getRequests()
                .stream()
                .filter(req -> req.getCourseName().equals(name))
                //.filter( req-> req.getExpiryDate().after(Timestamp.valueOf(LocalDateTime.now())))
                .map(req-> modelMapper.map(req, TokenDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamDTO> getMyRequestsAsCreator(String idCreator, String coursename) {
        tokenRepository.deleteExpiredToken(Timestamp.valueOf(LocalDateTime.now()));
        Optional<Student> studentOpt = studentRepository.findById(idCreator);
        if (!studentOpt.isPresent())
            throw new StudentNotFoundException();
        //cerco team con creator = id
        List<Team> teams = teamRepository.findTeamsByIdCreator(idCreator);
        System.out.println(teams.size());
        return teams.stream()
                .filter(t-> t.getCourse().getName().equals(coursename))
                .map(v-> modelMapper.map(v,TeamDTO.class))
                .collect(Collectors.toList());

    }



    @Override
    public String getCourseNameByTeamId(Long id) {
        Optional<Team> teamOpt = teamRepository.findById(id);
        if( !teamOpt.isPresent())
            throw  new TeamNotFoundException();
        Team team = teamOpt.get();
        return team.getCourse().getName();
    }

    @Override
    public void deleteCourse(String name, String username) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();
        if(course.getDocenti().stream()
                .filter(doc-> doc.getId().equals(username))
                .count() < 1)
            throw new DocenteHasNotPrivilegeException();

        course.deleteDependences();
        courseRepository.delete(course);
    }

    @Override
    public CourseDTO updateCourse(String name, CourseDTO dto, List<String> ids) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();

        course.setName(dto.getName());
        course.setMax(dto.getMax());
        course.setMin(dto.getMin());
        course.setModelVM_cpu(dto.getModelVM_cpu());
        course.setModelVM_GBDisk(dto.getModelVM_GBDisk());
        course.setModelVM_GBRam(dto.getModelVM_GBRam());

        Docente doc;

        for(String d: ids){
           if(!docenteRepository.existsById(d))
               throw new DocenteNotFoundException();
           doc = docenteRepository.getOne(d);
           doc.addCourse(course);
        }

        return dto;
    }

    @Override
    public void unsubscribeOne(String name, String studentId, String username) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();

        if ( course.getDocenti().stream()
        .filter(d-> d.getId().equals(username))
        .count() < 1)
            throw new DocenteHasNotPrivilegeException();

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if( !studentOpt.isPresent())
            throw  new StudentNotFoundException();
        Student student = studentOpt.get();

        student.unsubscribe(course);
    }

    @Override
    public void unsubscribeMany(String name, List<String> ids, String username) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();

        if ( course.getDocenti().stream()
                .filter(d-> d.getId().equals(username))
                .count() < 1)
            throw new DocenteHasNotPrivilegeException();

        for (String id: ids){
            Optional<Student> studentOpt = studentRepository.findById(id);
            studentOpt.ifPresent(student -> student.unsubscribe(course));
        }
    }

    @Override
    public List<VmDTO> getVmsByTeam(String name, Long teamId, String username) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if( !teamOpt.isPresent())
            throw  new TeamNotFoundException();
        Team team = teamOpt.get();

        if(!team.getCourse().getName().equals(name))
            throw new TeamNotFoundException();

        return team.getVms().stream()
                .map(v-> modelMapper.map(v,VmDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void changeVmsLimit(String name, Long teamId, String username, int vcpus, int GBram, int GBdisk, int maxAccese) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if( !teamOpt.isPresent())
            throw  new TeamNotFoundException();
        Team team = teamOpt.get();

        if(!team.getCourse().getName().equals(name))
            throw new TeamNotFoundException();

        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();

        if ( course.getDocenti().stream()
                .filter(d-> d.getId().equals(username))
                .count() < 1)
            throw new DocenteHasNotPrivilegeException();

        if(team.getVcpuUsati() > vcpus || team.getGBDiskUsati() > GBdisk || team.getGBRamUsati() > GBram || maxAccese < team.getVmAccese())
            throw new TooManyResourcesUsedException();
        if(vcpus != -1)
            team.setVcpuTot(vcpus);
        if(GBdisk != -1)
            team.setGBDiskTot(GBdisk);
        if(GBram != -1)
        team.setGBRamTot(GBram);
        if(maxAccese != -1)
        team.setMaxVmAccese(maxAccese);
    }

    @Override
    public List<TaskDTO> getTasks(String name) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();
        return course.getTasks()
                .stream()
                .map(t-> modelMapper.map(t,TaskDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO getTask(String name, Long taskId, UserDetails userDetails) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();
        Optional<Task> taskOpt = course.getTasks().stream()
                .filter(t-> t.getId().equals(taskId))
                .findFirst();
        if (!taskOpt.isPresent())
            throw new TaskNotFoundException();

        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROFESSOR")))
            return modelMapper.map(taskOpt.get(),TaskDTO.class);
        else {
            if(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))){
                if(taskOpt.get().getEssays().stream()
                        .noneMatch(e -> e.getStudent().getId().equals(userDetails.getUsername()))){
                    //Creare essay
                    Essay essay = new Essay();
                    essay.setVoto(Long.valueOf("-1"));
                    essay.setStato(Essay.stati.Letto);

                    Optional<Student> studentOptional = studentRepository.findById(userDetails.getUsername());
                    if(!studentOptional.isPresent())
                        throw new StudentNotFoundException();
                    essay.setStudent(studentOptional.get());
                    essay.setTask(taskOpt.get());
                    essayRepository.save(essay);
                }
            }
            return modelMapper.map(taskOpt.get(),TaskDTO.class);
        }
    }

    @Override
    public TaskDTO createTask(String name, String username, int days) {

        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();

        if ( course.getDocenti().stream()
                .filter(d-> d.getId().equals(username))
                .count() < 1)
            throw new DocenteHasNotPrivilegeException();

        Task task = new Task();
        task.setDataRilascio(Timestamp.from(Instant.now()));
        task.setDataScadenza(Timestamp.from(Instant.now().plus(days, ChronoUnit.DAYS)));
        task.setCourse(course);
        task.setDocente(docenteRepository.getOne(username));
        task = taskRepository.save(task);
        course.addTask(task);
        return modelMapper.map(task, TaskDTO.class);
    }

    @Override
    public List<EssayDTO> getEssays(Long taskId){
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if ( !taskOpt.isPresent()){
            throw new TaskNotFoundException();
        }
        Task task = taskOpt.get();
        return task.getEssays().stream()
                .map(e -> modelMapper.map(e,EssayDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public EssayDTO getEssay(Long taskId, Long essayId, UserDetails userDetails) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if ( !taskOpt.isPresent()){
            throw new TaskNotFoundException();
        }
        Task task = taskOpt.get();
        Optional<Essay> essayOpt = task.getEssays().stream()
                .filter(e-> e.getId().equals(essayId))
                .findFirst();
        if (!essayOpt.isPresent())
            throw new EssayNotFoundException();

        if(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))){
            if(essayOpt.get().getStato().equals(Essay.stati.Rivisto)){
                essayOpt.get().setStato(Essay.stati.Letto);
            }
        }
        return modelMapper.map(essayOpt.get(),EssayDTO.class);
    }

    @Override
    public List<VmDTO> getVmsByStudent(String studentId, Long teamId) {
        if(!studentRepository.existsById(studentId))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(studentId);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
        Team team = teamRepository.getOne(teamId);

        return team.getVms().stream()
                .map(v-> modelMapper.map(v,VmDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public VmDTO createVm(String id, Long teamId, VmDTO dto) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
        Team team = teamRepository.getOne(teamId);

        if(team.getVcpuTot() - team.getVcpuUsati() < dto.getVcpu())
            throw new InsufficientResourcesException();
        if(team.getGBRamTot() - team.getGBRamUsati() < dto.getGBRam())
            throw new InsufficientResourcesException();
        if(team.getGBDiskTot() - team.getGBDiskUsati() < dto.getGBDisk())
            throw new InsufficientResourcesException();

        Vm vm = new Vm();
        //Alla creazione, la VM è spenta.
        vm.setStatus(Vm.stati.Spenta);

        vm.setGBDisk(dto.getGBDisk());
        vm.setGBRam(dto.getGBRam());
        vm.setVcpu(dto.getVcpu());
        vm.setTeam(team);
        vm = vmRepository.save(vm);
        vm.addOwner(student);
        vm.setIdCreatore(id);
        return modelMapper.map(vm,VmDTO.class);
    }

    @Override
    public void switchVm(String id, Long teamId, Long vmId) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
        Team team = teamRepository.getOne(teamId);
        if(team.getVms().stream().noneMatch(v-> v.getId().equals(vmId)))
            throw new VmNotFoundException();
        if(!vmRepository.existsById(vmId))
            throw new VmNotFoundException();
        Vm vm = vmRepository.getOne(vmId);

        if(vm.getOwners().stream().noneMatch(s-> s.getId().equals(id)))
            throw new StudentHasNotPrivilegeException();

        if(vm.getStatus().equals(Vm.stati.Accesa)) {
            vm.setStatus(Vm.stati.Spenta);
            team.setVmAccese(team.getVmAccese()-1);
            team.setGBDiskUsati(team.getGBDiskUsati() - vm.getGBDisk());
            team.setGBRamUsati(team.getGBRamUsati() - vm.getGBRam());
            team.setVcpuUsati(team.getVcpuUsati() - vm.getVcpu());
        }
        else {
            if(team.getMaxVmAccese() == team.getVmAccese())
                throw new MaxVmAcceseException();
            else
            vm.setStatus(Vm.stati.Accesa);
            team.setVmAccese(team.getVmAccese()+1);
            team.setGBDiskUsati(team.getGBDiskUsati() + vm.getGBDisk());
            team.setGBRamUsati(team.getGBRamUsati() + vm.getGBRam());
            team.setVcpuUsati(team.getVcpuUsati() + vm.getVcpu());

            if (team.getGBDiskUsati() >= team.getCourse().getModelVM_GBDisk() ||
                team.getGBRamUsati() >= team.getCourse().getModelVM_GBRam() ||
                team.getVcpuUsati() >= team.getCourse().getModelVM_cpu())
                throw new TooManyResourcesUsedException();
        }
    }

    @Override
    public void deleteVm(String id, Long teamId, Long vmId) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
        Team team = teamRepository.getOne(teamId);
        if(team.getVms().stream().noneMatch(v-> v.getId().equals(vmId)))
            throw new VmNotFoundException();
        if(!vmRepository.existsById(vmId))
            throw new VmNotFoundException();
        Vm vm = vmRepository.getOne(vmId);

        if(vm.getOwners().stream().noneMatch(s-> s.getId().equals(id)))
            throw new StudentHasNotPrivilegeException();
        if(vm.getStatus()== Vm.stati.Accesa) {
        team.setGBDiskUsati(team.getGBDiskUsati()-vm.getGBDisk());
        team.setGBRamUsati(team.getGBDiskUsati()-vm.getGBRam());
        team.setVcpuUsati(team.getVcpuUsati()-vm.getVcpu());
        team.setVmAccese(team.getMaxVmAccese()-1);}
        vmRepository.delete(vm);
    }

    @Override
    public byte[] getImage(String id) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        return studentRepository.getOne(id).getPhotoStudent();
    }

    @Override
    public List<ImageDTO> getStorical(String name, Long taskId, Long essayId) {
        if(!courseRepository.existsById(name))
            throw new CourseNotFoundException();
        if(!taskRepository.existsById(taskId))
            throw new TaskNotFoundException();
        Task task = taskRepository.getOne(taskId);
        if(!task.getCourse().getName().equals(name))
            throw new TaskNotFoundException();
        if(task.getEssays().stream().noneMatch(e->e.getId().equals(essayId)))
            throw new EssayNotFoundException();
        Essay essay = essayRepository.getOne(essayId);
        return essay.getImages().stream()
                .map(i-> modelMapper.map(i,ImageDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfessorDTO> getAllProfessor() {
        return docenteRepository.findAll()
                .stream()
                .map(p -> modelMapper.map(p,ProfessorDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO uploadImageIntoStudent(MultipartFile file, String studentId) {
        if(!studentRepository.existsById(studentId))
            throw new StudentNotFoundException();
        else {
            Student student = studentRepository.getOne(studentId);
            student.setPhotoStudent(fromFileToByteArray(file));
            return modelMapper.map(student, StudentDTO.class);
        }
    }

    @Override
    public ProfessorDTO uploadImageIntoProfessor(MultipartFile imageFile, String professorId) {
        if(!docenteRepository.existsById(professorId))
            throw new DocenteNotFoundException();
        else {
            Docente docente = docenteRepository.getOne(professorId);
            docente.setPhotoDocente(fromFileToByteArray(imageFile));
            return modelMapper.map(docente, ProfessorDTO.class);
        }
    }

    @Override
    public void uploadPhotoIntoVm(String id, Long teamId, Long vmId, MultipartFile imageFile) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
        Team team = teamRepository.getOne(teamId);
        if(team.getVms().stream().noneMatch(v-> v.getId().equals(vmId)))
            throw new VmNotFoundException();
        if(!vmRepository.existsById(vmId))
            throw new VmNotFoundException();
        Vm vm = vmRepository.getOne(vmId);

        if(vm.getOwners().stream().noneMatch(s-> s.getId().equals(id)))
            throw new StudentHasNotPrivilegeException();

        vm.setScreenVm(fromFileToByteArray(imageFile));
    }

    @Override
    public void uploadImageIntoTask(String name, Long taskId, UserDetails userDetails, MultipartFile imageFile) {
        Optional<Course> courseOpt = courseRepository.findById(name);
        if( !courseOpt.isPresent())
            throw  new CourseNotFoundException();
        Course course = courseOpt.get();
        Optional<Task> taskOpt = course.getTasks().stream()
                .filter(t-> t.getId().equals(taskId))
                .findFirst();
        if (!taskOpt.isPresent())
            throw new TaskNotFoundException();
        List<String> listDocenti = course.getDocenti().stream().map(docente -> docente.getId()).collect(Collectors.toList());
        if (!listDocenti.contains(userDetails.getUsername())){
            throw new DocenteHasNotPrivilegeException();
        }
        Task task = taskRepository.getOne(taskId);
        task.setDescription(fromFileToByteArray(imageFile));
    }


    @Override
    public void valutaEssay(Long taskId, Long essayId, UserDetails userDetails, Long voto) {
        if (voto>31)
            voto = 31L;
        else if(voto<0)
            voto = 0L;
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if ( !taskOpt.isPresent()){
            throw new TaskNotFoundException();
        }
        Task task = taskOpt.get();
        if(task.getEssays().stream().noneMatch(e->e.getId().equals(essayId)))
            throw new EssayNotFoundException();
        Essay essay = essayRepository.getOne(essayId);
        if (!task.getDocente().getId().equals(userDetails.getUsername()))
            throw new DocenteHasNotPrivilegeException();
        if(essay.getStato().equals(Essay.stati.Terminato))
            throw new EssayNotModifiableException();
        if (!essay.getStato().equals(Essay.stati.Consegnato))
            throw new EssayNotLoadedByStudentException();
        essay.setStato(Essay.stati.Terminato);
        essay.setVoto(voto);
    }

    @Override
    public EssayDTO getEssayByStudentId(String name, Long taskId, String id) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if ( !taskOpt.isPresent()){
            throw new TaskNotFoundException();
        }
        Task task = taskOpt.get();
        Optional<Essay> essayOpt = task.getEssays().stream()
                .filter(e-> e.getIdStudente().equals(id))
                .findFirst();
        if (!essayOpt.isPresent())
            throw new EssayNotFoundException();

       /* if(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))){
            if(essayOpt.get().getStato().equals(Essay.stati.Rivisto)){
                essayOpt.get().setStato(Essay.stati.Letto);
            }
        }/*/
        return modelMapper.map(essayOpt.get(),EssayDTO.class);
    }

    @Override
    public EssayDTO loadFirstEssay(String name, Long taskId, String studentId) {
        Student student;
        if(!studentRepository.existsById(studentId))
            throw new StudentNotFoundException();
        else
            student = studentRepository.getOne(studentId);

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if ( !taskOpt.isPresent()){
            throw new TaskNotFoundException();
        }
        Task task = taskOpt.get();

        Essay essay = new Essay();
        essay.setIdStudente(studentId);
        essay.setStato(Essay.stati.Letto);
        essay = essayRepository.save(essay);
        essay.setStudent(student);
        essay.setTask(task);
        essay.setLastModified(Timestamp.from(Instant.now()));
        return modelMapper.map(essay,EssayDTO.class);
    }

    @Override
    public void uploadVmParamsByStudent(String id, Long teamId, Long vmId, Map<String, Integer> data) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
        Team team = teamRepository.getOne(teamId);
        if(team.getVms().stream().noneMatch(v-> v.getId().equals(vmId)))
            throw new VmNotFoundException();
        if(!vmRepository.existsById(vmId))
            throw new VmNotFoundException();
        Vm vm = vmRepository.getOne(vmId);

        if(vm.getOwners().stream().noneMatch(s-> s.getId().equals(id)))
            throw new StudentHasNotPrivilegeException();

        if(vm.getStatus().equals(Vm.stati.Accesa)) {
            vm.setStatus(Vm.stati.Spenta);
            team.setVmAccese(team.getVmAccese()-1);
            team.setGBDiskUsati(team.getGBDiskUsati() - vm.getGBDisk());
            team.setGBRamUsati(team.getGBRamUsati() - vm.getGBRam());
            team.setVcpuUsati(team.getVcpuUsati() - vm.getVcpu());
        }

        if (data.containsKey("vcpus"))
            vm.setVcpu(data.get("vcpus"));
        if (data.containsKey("gbram"))
            vm.setGBRam(data.get("gbram"));
        if (data.containsKey("gbdisk"))
            vm.setGBDisk(data.get("gbdisk"));
       //team.aggiornaRisorseTotali();
    }

    @Override
    public void addVmOwner(String id, Long teamId, Long vmId, List<String> ownerList) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();

        Team team = teamRepository.getOne(teamId);
        if(team.getVms().stream().noneMatch(v-> v.getId().equals(vmId)))
            throw new VmNotFoundException();
        if(!vmRepository.existsById(vmId))
            throw new VmNotFoundException();
        Vm vm = vmRepository.getOne(vmId);

        if(vm.getOwners().stream().noneMatch(s-> s.getId().equals(id)))
            throw new StudentHasNotPrivilegeException();

        List<Student> newOwnersList = new ArrayList<>();
        for(String studId: ownerList){
            if(!studentRepository.existsById(studId))
                throw new StudentNotFoundException();
            Student studentToBeAddedAsOwner = studentRepository.getOne(studId);
            newOwnersList.add(studentToBeAddedAsOwner);
        }
        newOwnersList.add(student);
        vm.changeOwnerList(newOwnersList);
    }

    @Override
    public EssayDTO loadEssay(Long taskId, Long essayId, byte[] data, UserDetails userDetails) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if ( !taskOpt.isPresent()){
            throw new TaskNotFoundException();
        }
        Task task = taskOpt.get();

        if( task.getDataScadenza().before(new Timestamp(System.currentTimeMillis())))
            throw new TaskExpiredException();

        if(task.getEssays().stream().noneMatch(e->e.getId().equals(essayId)))
            throw new EssayNotFoundException();
        Essay essay = essayRepository.getOne(essayId);

        if(essay.getStato().equals(Essay.stati.Terminato))
            throw new EssayNotModifiableException();

        if(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))){
            essay.setStato(Essay.stati.Consegnato);
            essay.setIdStudente(userDetails.getUsername());
        }else{
            if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROFESSOR"))){

                if(!essay.getStato().equals(Essay.stati.Consegnato))
                    throw new EssayNotLoadedByStudentException();
                essay.setStato(Essay.stati.Rivisto);
            }
        }
        int tot = essay.getImages().size();
        tot ++;
        String filename = "essay" + essayId + "_" + tot;
        Image image = new Image();
        image.setCreationDate(Timestamp.from(Instant.now()));
        essay.setLastModified(Timestamp.from(Instant.now()));
        image.setData(data);
        image.setFilename(filename);
        image.setIdCreator(userDetails.getUsername());
        image = imageRepository.save(image);
        essay.addImage(image);
        return modelMapper.map(essay,EssayDTO.class);
    }

    public byte[] fromFileToByteArray(MultipartFile file){
        byte[] byteObjects = null;
        try {
            byteObjects = new byte[file.getBytes().length];
            int i=0;
            for (byte b : file.getBytes()){
                byteObjects[i++] = b;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteObjects;
    }


    @Override
    public List<ImageDTO> getMyStorical(String name, String id,Long taskId, Long essayId) {
        if(!courseRepository.existsById(name))
            throw new CourseNotFoundException();
        if(!taskRepository.existsById(taskId))
            throw new TaskNotFoundException();
        Task task = taskRepository.getOne(taskId);
        if(!task.getCourse().getName().equals(name))
            throw new TaskNotFoundException();
        if(task.getEssays().stream().noneMatch(e->e.getId().equals(essayId)))
            throw new EssayNotFoundException();
        Essay essay = essayRepository.getOne(essayId);
        return essay.getImages().stream()
                //.filter(t-> t.getIdCreator().equals(id))
                .map(i-> modelMapper.map(i,ImageDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TokenDTO> getRequestsByTeamId(Long teamId) {
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();
         return tokenRepository.findTokenByTeamId(teamId).stream()
                 .map(t-> modelMapper.map(t,TokenDTO.class))
                 .collect(Collectors.toList());
    }

    @Override
    public List<StudentRequestDTO> getMembersByRequest(String id, Long teamId, String name) {
        List<StudentDTO> studs = getMembers(Long.valueOf(teamId));
        studs.removeIf(s -> s.getId().equals(id));
        List<StudentRequestDTO> studRequest = new ArrayList<>();
        studs.forEach(s -> {
            List<TokenDTO> tokens = tokenRepository.findTokenByTeamId(teamId).stream()
                    .filter( t -> t.getSId().equals(s.getId()))
                    .map(t -> modelMapper.map(t, TokenDTO.class))
                    .collect(Collectors.toList());

            boolean flag;
            String confirm;
            if(tokens.isEmpty()) //Se siamo qui, vuol dire che questo studente è il creatore
                confirm="Creatore";
            else {
                //se sono qui, è un membro
                flag = tokens.get(0).isConfirmation();

                if(flag == false )
                    confirm = "Pendente";
                else
                    confirm = "Confermato"; }
            StudentRequestDTO stud= new StudentRequestDTO();
            stud.setName(s.getName());
            stud.setFirstName(s.getFirstName());
            stud.setId(s.getId());
            stud.setStato(confirm);
            studRequest.add(stud);
            //mappa.put(s, confirm);

        });

        return studRequest;
    }


    public boolean isOwner(String id,Long teamId, Long vmId) {
        if(!studentRepository.existsById(id))
            throw new StudentNotFoundException();
        Student student = studentRepository.getOne(id);

        if(student.getTeams().stream().noneMatch(t-> t.getId().equals(teamId)))
            throw new TeamNotFoundException();
        if(!teamRepository.existsById(teamId))
            throw new TeamNotFoundException();

        Team team = teamRepository.getOne(teamId);
        if(team.getVms().stream().noneMatch(v-> v.getId().equals(vmId)))
            throw new VmNotFoundException();
        if(!vmRepository.existsById(vmId))
            throw new VmNotFoundException();
        Vm vm = vmRepository.getOne(vmId);
        if(vm.getOwners().contains(student)) return true;
        else return false;
    }

}
