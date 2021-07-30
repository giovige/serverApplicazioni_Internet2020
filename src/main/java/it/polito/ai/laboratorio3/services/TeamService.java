package it.polito.ai.laboratorio3.services;

import it.polito.ai.laboratorio3.dtos.*;
import it.polito.ai.laboratorio3.entities.Course;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TeamService {
    static final int ATTIVO=1;
    static final int NON_ATTIVO=0;

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    boolean addCourse(CourseDTO course, List<String> ids);

    Optional<CourseDTO> getCourse(String name);
    List<CourseDTO> getCoursesByProf(String teacherId);
    List<CourseDTO> getAllCourses();
    boolean addStudent(StudentDTO student, byte[] studentImg);
    boolean addProfessor(ProfessorDTO professor);
    Optional<StudentDTO> getStudent(String studentId);
    Optional<ProfessorDTO> getProf(String professorId);
    List<StudentDTO> getAllStudents();
    List<StudentDTO> getEnrolledStudents(String courseName);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    boolean addStudentToCourse(String studentId, String courseName);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void enableCourse(String courseName, String docenteId);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void disableCourse(String courseName, String docenteId);

    List<Boolean> addAll(List<StudentDTO> students);
    List<Boolean> enrollAll(List<String> studentIds, String courseName);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    List<Boolean> addAndEroll(Reader r, String courseName);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<CourseDTO> getCourses(String studentId);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<TeamDTO> getTeamsForStudent(String studentId);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    Optional<TeamDTO> getTeamForStudentByCourseName(String studentId, String name);

    List<StudentDTO>getMembers(Long TeamId);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    TeamDTO proposeTeam(String courseId, String name, List<String> memberIds,String creator);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    List<TeamDTO> getTeamForCourse(String courseName);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<StudentDTO> getStudentsInTeams(String courseName);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<StudentDTO> getAvailableStudents(String courseName, String id);

    void activeTeam(Long teamId);
    void evictTeam(Long teamId);


    List<TokenDTO> getRequestsForStudent(String id, String name);
    List<TeamDTO> getMyRequestsAsCreator(String idCreator, String coursename);

    String getCourseNameByTeamId(Long id);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void deleteCourse(String name, String username);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    CourseDTO updateCourse(String name,CourseDTO dto, List<String> ids);
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void unsubscribeOne(String name, String studentId, String username);
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void unsubscribeMany(String name, List<String> ids, String username);

    List<VmDTO> getVmsByTeam(String name, Long teamId, String username);
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void changeVmsLimit(String name, Long teamId, String username, int vcpus, int GBram, int GBdisk, int vmAccese);

    List<TaskDTO> getTasks(String name);
    TaskDTO getTask(String name, Long taskId,UserDetails userDetails);
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    TaskDTO createTask(String name, String username, int days);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    List<EssayDTO> getEssays(Long taskId);

    EssayDTO getEssay(Long taskId, Long essayId, UserDetails userDetails);
    EssayDTO loadEssay(Long taskId, Long essayId, byte[] data, UserDetails userDetails);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<VmDTO> getVmsByStudent(String studentId, Long teamId);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    VmDTO createVm(String id, Long teamId, VmDTO dto);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    void switchVm(String id, Long teamId, Long vmId);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    void deleteVm(String id, Long teamId, Long vmId);

    byte[] getImage(String id);

    List<ImageDTO> getStorical(String name, Long taskId, Long essayId);

    List<ProfessorDTO> getAllProfessor();

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    StudentDTO uploadImageIntoStudent(MultipartFile file, String studentId);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    ProfessorDTO uploadImageIntoProfessor(MultipartFile imageFile, String professorId);

    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    void uploadPhotoIntoVm(String id, Long teamId, Long vmId, MultipartFile imageFile);

    void uploadImageIntoTask(String name, Long taskId, UserDetails userDetails, MultipartFile imageFile);

    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    void valutaEssay(Long taskId, Long essayId, UserDetails userDetails, Long voto);

    EssayDTO getEssayByStudentId(String name, Long taskId, String id);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    EssayDTO loadFirstEssay(String name, Long taskId, String username);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    void uploadVmParamsByStudent(String id, Long teamId, Long vmId, Map<String, Integer> data);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    void addVmOwner(String id, Long teamId, Long vmId, List<String> ownerList);

    List<ImageDTO> getMyStorical(String name, String id,Long taskId, Long essayId);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<TokenDTO> getRequestsByTeamId(Long teamId);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    List<StudentRequestDTO> getMembersByRequest(String id, Long teamId, String name);
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    boolean isOwner(String id,Long teamId, Long vmId);
}
