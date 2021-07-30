package it.polito.ai.laboratorio3.controllers;

import it.polito.ai.laboratorio3.dtos.*;
import it.polito.ai.laboratorio3.exceptions.*;
import it.polito.ai.laboratorio3.services.NotificationService;
import it.polito.ai.laboratorio3.services.TeamService;
import net.minidev.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Lob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.StubNotFoundException;
import java.security.Principal;
import java.util.*;


import static it.polito.ai.laboratorio3.controllers.ModelHelper.enrich;

@RestController
@RequestMapping("/API/courses")
public class CourseController {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TeamService teamService;

    @Autowired
    NotificationService notificationService;

    @GetMapping({"", "/"})
    public List<CourseDTO> all() {
        List<CourseDTO> courses = teamService.getAllCourses();
        courses.forEach(ModelHelper::enrich);
        return courses;
    }

    @GetMapping("/{name}")
    public CourseDTO getOne(@PathVariable String name) {
        Optional<CourseDTO> courseDTO = teamService.getCourse(name);
        if (courseDTO.isPresent())
            return enrich(courseDTO.get());
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
    }

    @GetMapping("/{name}/enrolled")
    public List<StudentDTO> enrolledStudents(@PathVariable String name) {
        if (teamService.getCourse(name).isPresent())
            return teamService.getEnrolledStudents(name);
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
    }

    @PostMapping({"", "/"})
    public CourseDTO addCourse(@RequestBody Map<String, Object> requestBody, @AuthenticationPrincipal UserDetails userDetails) {
        CourseDTO courseDTO = new CourseDTO();
        List<String> ids = new ArrayList<>();
        if(requestBody.containsKey("courseDTO"))
           courseDTO = modelMapper.map(requestBody.get("courseDTO"),CourseDTO.class);

        if(requestBody.containsKey("ids"))
            ids = (List<String>) requestBody.get("ids");

        if (!ids.contains(userDetails.getUsername()))
            ids.add(userDetails.getUsername());
            System.out.println(userDetails.getUsername());
        if (teamService.addCourse(courseDTO, ids))
            return enrich(courseDTO);
        else throw new ResponseStatusException(HttpStatus.CONFLICT, courseDTO.getName());
    }

    @PostMapping({"/{courseName}/enrollOne"})
    public StudentDTO enrollOne(@PathVariable String courseName, @RequestBody StudentDTO studentDTO) {
        try {
            teamService.addStudentToCourse(studentDTO.getId(), courseName);
            return enrich(studentDTO);
        } catch (CourseNotFoundException courseExc) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (StudentNotFoundException studentExc) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, studentDTO.getId());
        }
    }

    @PostMapping("/{name}/enrollMany")
    public List<Boolean> enrollMany(@PathVariable String name, @RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (!contentType.equals("text/csv"))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, file.getContentType());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            return teamService.addAndEroll(reader, name);
        } catch (IOException exc) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/{name}/proposeTeam")
    public TeamDTO proposeTeam(@PathVariable String name, @RequestParam("team") String team, @RequestParam("timeout") Long hours,
                               @RequestParam("membersIds") List<String> membersIds, @AuthenticationPrincipal UserDetails userDetails) {
        membersIds.add(userDetails.getUsername());
        membersIds = new ArrayList<>(new HashSet<>(membersIds));
        System.out.println("-------ProposeTeam id senza duplicati:");
        for(String s : membersIds)
            System.out.println("-------"+s);
        try {
            String creator = userDetails.getUsername();
            TeamDTO teamDTO = teamService.proposeTeam(name, team, membersIds,creator);
            membersIds.remove(userDetails.getUsername());
            notificationService.notifyTeam(teamDTO, membersIds, hours);
            return teamDTO;
        } catch (CourseNotFoundException | StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (TeamAlreadyInCourseException | MemberNotInCourseException |
                WrongTeamDimensionExcpetion | CourseNotEnabledException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{name}/enableCourse")
    public CourseDTO enableCourse(@PathVariable String name, @AuthenticationPrincipal UserDetails userDetails) {
        teamService.enableCourse(name, userDetails.getUsername());
        Optional<CourseDTO> courseDTO = teamService.getCourse(name);
        return enrich(courseDTO.get());
    }

    @GetMapping("/{name}/disableCourse")
    public CourseDTO disableCourse(@PathVariable String name, @AuthenticationPrincipal UserDetails userDetails) {
        teamService.disableCourse(name, userDetails.getUsername());
        Optional<CourseDTO> courseDTO = teamService.getCourse(name);
        return enrich(courseDTO.get());
    }

    @GetMapping("/{name}/teams")
    public List<TeamDTO> getTeamsForCourse(@PathVariable String name) {
        List<TeamDTO> teamDTOS = teamService.getTeamForCourse(name);
        return teamDTOS;
    }

    @GetMapping("/{name}/availableStudents")
    public List<StudentDTO> getAvailableStudents(@PathVariable String name, @AuthenticationPrincipal UserDetails userDetails) {
        List<StudentDTO> studentDTOS = teamService.getAvailableStudents(name, userDetails.getUsername());
        studentDTOS.forEach(ModelHelper::enrich);
        return studentDTOS;
    }

    @GetMapping("/{name}/alreadyInTeamStudents")
    public List<StudentDTO> getAlreadyInTeamStudents(@PathVariable String name) {
        List<StudentDTO> studentDTOS = teamService.getStudentsInTeams(name);
        studentDTOS.forEach(ModelHelper::enrich);
        return studentDTOS;
    }

    @DeleteMapping("/{name}")
    public void deleteCourse(@PathVariable String name, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            teamService.deleteCourse(name, userDetails.getUsername());
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DocenteHasNotPrivilegeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PutMapping("/{name}")
    public CourseDTO updateCourse(@PathVariable String name, @RequestBody Map<String, Object> data, @AuthenticationPrincipal UserDetails userDetails) {

        CourseDTO courseDTO;
        List<String> ids = new ArrayList<>();
        if(data.containsKey("courseDTO"))
            courseDTO = modelMapper.map(data.get("courseDTO"),CourseDTO.class);
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insert field courseDTO");

        if(data.containsKey("ids"))
            ids = (List<String>) data.get("ids");

        if (!ids.contains(userDetails.getUsername()))
            ids.add(userDetails.getUsername());

        try {
            return teamService.updateCourse(name, courseDTO, ids);
        } catch (CourseNotFoundException | DocenteNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DocenteHasNotPrivilegeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PutMapping("/{name}/unsubscribeOne/{studentId}")
    public void updateCourseStudent(@PathVariable String name, @PathVariable String studentId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            teamService.unsubscribeOne(name, studentId, userDetails.getUsername());
        } catch (CourseNotFoundException | StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DocenteHasNotPrivilegeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }

    }

    @PutMapping("/{name}/unsubscribeMany")
    public void updateCourseStudents(@PathVariable String name, @RequestBody Map<String, Object> data, @AuthenticationPrincipal UserDetails userDetails) {

        if (!data.containsKey("ids"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request must contain filed ids with the ids of the students");

        List<String> ids = (List<String>) data.get("ids");
        try {
            teamService.unsubscribeMany(name, ids, userDetails.getUsername());
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DocenteHasNotPrivilegeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }

    }

    @GetMapping("/{name}/teams/{teamId}/vms")
    public List<VmDTO> getVmsFromTeam(@PathVariable String name, @PathVariable String teamId, @AuthenticationPrincipal UserDetails userDetails) {

        try {
            return teamService.getVmsByTeam(name, Long.valueOf(teamId), userDetails.getUsername());
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @PutMapping("/{name}/teams/{teamId}")
    public void changeVmsLimit(@PathVariable String name, @PathVariable String teamId, @AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Integer> data) {
        int vcpus, GBram, GBdisk, maxAccese;
        vcpus = data.getOrDefault("vcpus", -1);
        GBram = data.getOrDefault("gbram", -1);
        GBdisk = data.getOrDefault("gbdisk", -1);
        maxAccese = data.getOrDefault("maxaccese", -1);

        try {
            teamService.changeVmsLimit(name, Long.valueOf(teamId), userDetails.getUsername(), vcpus, GBram, GBdisk, maxAccese);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (TooManyResourcesUsedException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }

    @GetMapping("/{name}/tasks")
    public List<TaskDTO> getTasks(@PathVariable String name) {
        try {
            return teamService.getTasks(name);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{name}/tasks/{taskId}")
    public TaskDTO getTask(@PathVariable String name, @PathVariable String taskId, @AuthenticationPrincipal UserDetails userDetails){
        try{
            return teamService.getTask(name,Long.valueOf(taskId),userDetails);
        }catch (TaskNotFoundException | StudentNotFoundException | CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @PostMapping("/{name}/task")
    public TaskDTO createTask(@PathVariable String name, @AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> data) {
        if (!data.containsKey("days"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insert field days, duration of task");
        int days = (Integer) data.get("days");
        if (days < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be at least one day");

        try {
            return teamService.createTask(name, userDetails.getUsername(), days);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DocenteHasNotPrivilegeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PutMapping("/{name}/task/{taskId}")
    public void uploadImageIntoTask(@PathVariable String name,@PathVariable String taskId, @AuthenticationPrincipal UserDetails userDetails,@RequestBody MultipartFile imageFile ){
        try{
            teamService.uploadImageIntoTask(name,Long.valueOf(taskId),userDetails,imageFile);
        }catch (TaskNotFoundException | StudentNotFoundException | CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @GetMapping("/{name}/task/{taskId}/studentId/{id}/essay")
    public EssayDTO getEssayByStudentId(@PathVariable String name,@PathVariable String taskId, @PathVariable String id, @AuthenticationPrincipal UserDetails userDetails){

        try{
           return teamService.getEssayByStudentId(name,Long.valueOf(taskId),id);
        }catch (TaskNotFoundException | StudentNotFoundException | CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @PostMapping("/{name}/task/{taskId}/essay")
    public EssayDTO loadFirstEssay(@PathVariable String name, @PathVariable String taskId, @AuthenticationPrincipal UserDetails userDetails, @RequestBody EssayDTO dto){
        try{
            return teamService.loadFirstEssay(name, Long.valueOf(taskId), userDetails.getUsername());
        } catch (TaskNotFoundException | StudentNotFoundException | CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @GetMapping("/{name}/tasks/{taskId}/essays")
    //TODO controllare se funziona anche senza parametro name
    public List<EssayDTO> getEssays(@PathVariable String taskId, @PathVariable String name){
        try{
            return teamService.getEssays(Long.valueOf(taskId));
        }catch (TaskNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @GetMapping("/{name}/tasks/{taskId}/essays/{essayId}")
    public EssayDTO getEssay(@PathVariable String taskId, @PathVariable String name, @PathVariable String essayId,@AuthenticationPrincipal UserDetails userDetails){
        try{
            return teamService.getEssay(Long.valueOf(taskId),Long.valueOf(essayId),userDetails);
        } catch (EssayNotFoundException | TaskNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @GetMapping("/{name}/tasks/{taskId}/essays/{essayId}/storical")
    public List<ImageDTO> getEssayStorical(@PathVariable String taskId, @PathVariable String name, @PathVariable String essayId) {
        try {
            return teamService.getStorical(name, Long.valueOf(taskId), Long.valueOf(essayId));
        } catch (EssayNotFoundException | TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{name}/tasks/{taskId}/essays/{essayId}")
    public EssayDTO loadEssay(@PathVariable String name, @PathVariable String taskId, @PathVariable String essayId, @AuthenticationPrincipal UserDetails userDetails, @RequestBody MultipartFile imageFile){
        try{
            return teamService.loadEssay(Long.valueOf(taskId),Long.valueOf(essayId), imageFile.getBytes(), userDetails);
        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore caricamento file");
        }
        catch (EssayNotFoundException | TaskNotFoundException | EssayNotLoadedByStudentException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
        catch (TaskExpiredException | EssayNotModifiableException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @PutMapping("/{name}/tasks/{taskId}/essays/{essayId}/valuta")
    public void valutaEssay(@PathVariable String name, @PathVariable String taskId, @PathVariable String essayId, @AuthenticationPrincipal UserDetails userDetails, @RequestBody String voto){
        try{
            teamService.valutaEssay(Long.valueOf(taskId),Long.valueOf(essayId), userDetails, Long.valueOf(voto));
        } catch (DocenteHasNotPrivilegeException e ){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,e.getMessage());
        }
        catch (EssayNotFoundException | TaskNotFoundException | EssayNotLoadedByStudentException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }


    @GetMapping("/{name}/student/{id}/tasks/{taskId}/essays/{essayId}/mystorical")
    public List<ImageDTO> getEssayStoricalForStudent(@PathVariable String taskId, @PathVariable String name, @PathVariable String essayId, @PathVariable String id) {
        try {
            return teamService.getMyStorical(name, id, Long.valueOf(taskId), Long.valueOf(essayId));
        } catch (EssayNotFoundException | TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{name}/student/{id}/teams/{teamId}/membersPerRequest")
    public List<StudentRequestDTO> getMembersPerRequest(@PathVariable String id, @PathVariable String teamId, @PathVariable String name) {
        return teamService.getMembersByRequest(id, Long.valueOf(teamId), name);
    }


}
