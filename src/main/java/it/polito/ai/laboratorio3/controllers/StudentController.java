package it.polito.ai.laboratorio3.controllers;

import it.polito.ai.laboratorio3.dtos.*;
import it.polito.ai.laboratorio3.exceptions.*;
import it.polito.ai.laboratorio3.services.NotificationService;
import it.polito.ai.laboratorio3.services.TeamService;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.polito.ai.laboratorio3.controllers.ModelHelper.enrich;

@RestController
@RequestMapping("/API/students")
public class StudentController {
    @Autowired
    TeamService teamService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    NotificationService notificationService;

    @GetMapping({"","/"})
    public List<StudentDTO> all(){
        List<StudentDTO> students = teamService.getAllStudents();
        students.forEach(ModelHelper::enrich);
        return students;
    }

    @GetMapping("/{studentId}")
    public StudentDTO getStudent(@PathVariable String studentId){
        Optional<StudentDTO> studentDTO = teamService.getStudent(studentId);
        if(studentDTO.isPresent())
            return enrich(studentDTO.get());
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,studentId);
    }

    //TODO da eliminare
   /* @PostMapping({"","/"})
    public StudentDTO addStudent(@RequestBody StudentDTO studentDTO, @RequestBody MultipartFile studentImg){
        try {
            if (teamService.addStudent(studentDTO, studentImg.getBytes()))
                return enrich(studentDTO);
            else throw new ResponseStatusException(HttpStatus.CONFLICT, studentDTO.getId());
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Errore caricamento immagine!");
        }
    }/*/

   @PutMapping({"/{studentId}"})
   public StudentDTO uploadImageForStudent(@RequestBody MultipartFile imageFile, @PathVariable String studentId, @AuthenticationPrincipal UserDetails userDetails){
       try {
           if (!studentId.equals(userDetails.getUsername())) {
               throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non hai i privilegi!");
           } else {
               return teamService.uploadImageIntoStudent(imageFile, studentId);
           }
       }catch (StudentNotFoundException e) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
       }
   }


    @GetMapping("/{id}/courses")
    public List<CourseDTO> getCoursesForStudent(@PathVariable String id){
        List<CourseDTO> courseDTOS = teamService.getCourses(id);
        courseDTOS.forEach(ModelHelper::enrich);
        return courseDTOS;
    }

    @GetMapping("/{id}/teams")
    public List<TeamDTO> getTeamsForStudent(@PathVariable String id){
        return teamService.getTeamsForStudent(id);
    }

    @GetMapping("/{id}/courses/{name}/teams")
    public Optional<TeamDTO> getTeamsForStudentByCourse(@PathVariable String id, @PathVariable String name){
        return teamService.getTeamForStudentByCourseName(id,name);

    }

    @GetMapping("/{id}/courses/{coursename}/hasTeam")
    public boolean studentHasTeam(@PathVariable String id, @PathVariable String coursename) {
        Optional<TeamDTO> t = teamService.getTeamForStudentByCourseName(id,coursename);
        return t.isPresent();
    }

    @GetMapping("/teams/{teamId}/members")
    public List<StudentDTO> getTeamMembers(@PathVariable String teamId) {
        return teamService.getMembers(Long.valueOf(teamId));
    }

    @GetMapping("/teams/{teamId}/request")
    public List<TokenDTO> getTeamRequests(@PathVariable String teamId) {
       try{
           return teamService.getRequestsByTeamId(Long.valueOf(teamId));
       }catch (TeamNotFoundException e){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
       }

    }

    @GetMapping("/{id}/courses/{name}/requests")
    public List<TokenDTO> getRequests (@PathVariable String id, @PathVariable String name){
        List<TokenDTO> tokens = teamService.getRequestsForStudent(id, name);
        return tokens;
    }

    @GetMapping("/{idCreator}/courses/{coursename}/myRequestsAsCreator")
    public List<TeamDTO> getMyRequests (@PathVariable String idCreator, @PathVariable String coursename){
        List<TeamDTO> teams = teamService.getMyRequestsAsCreator(idCreator, coursename);
        return teams;
    }



    @GetMapping("/{id}/teams/{teamId}/vms")
    public List<VmDTO> getVms (@PathVariable String id, @PathVariable String teamId){
        try {
            return teamService.getVmsByStudent(id, Long.valueOf(teamId));
        }
        catch (StudentNotFoundException | TeamNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @PostMapping("/{id}/teams/{teamId}/vm")
    public VmDTO createVm (@PathVariable String id, @PathVariable String teamId, @RequestBody Map<String,Object> data, @AuthenticationPrincipal UserDetails userDetails){

        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must create a vm with your Id");

        VmDTO dto;


        if(data.containsKey("dto"))
            dto = modelMapper.map(data.get("dto"),VmDTO.class);
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insert field vmdto");

        try {
            return teamService.createVm(id, Long.valueOf(teamId), dto);
        }
        catch (StudentNotFoundException | TeamNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
        catch (InsufficientResourcesException | StudentHasNotPrivilegeException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }

    }

    @DeleteMapping("/{id}/teams/{teamId}/vms/{vmId}")
    public void deleteVm(@PathVariable String id, @PathVariable String teamId, @PathVariable String vmId, @AuthenticationPrincipal UserDetails userDetails){
        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must delete a vm with your Id");

        try {
            teamService.deleteVm(id, Long.valueOf(teamId), Long.valueOf(vmId));
        }
        catch (StudentNotFoundException | TeamNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
        catch (StudentHasNotPrivilegeException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @PutMapping("/{id}/teams/{teamId}/vms/{vmId}")
    public void uploadPhotoIntoVm(@PathVariable String id, @PathVariable String teamId, @PathVariable String vmId, @AuthenticationPrincipal UserDetails userDetails, @RequestBody MultipartFile imageFile){
        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must update a vm with your Id");

        try {
            teamService.uploadPhotoIntoVm(id, Long.valueOf(teamId), Long.valueOf(vmId), imageFile);
        }
        catch (StudentNotFoundException | TeamNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
        catch (StudentHasNotPrivilegeException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @PutMapping("/{id}/teams/{teamId}/vms/{vmId}/changeParams")
    public void uploadPhotoIntoVm(@PathVariable String id, @PathVariable String teamId, @PathVariable String vmId, @AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Integer> data){
        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must update a vm with your Id");

        try {
            teamService.uploadVmParamsByStudent(id, Long.valueOf(teamId), Long.valueOf(vmId), data);
        }
        catch (StudentNotFoundException | TeamNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
        catch (StudentHasNotPrivilegeException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @PutMapping("/{id}/teams/{teamId}/vms/{vmId}/switch")
    public void switchVm(@PathVariable String id, @PathVariable String teamId, @PathVariable String vmId, @AuthenticationPrincipal UserDetails userDetails){

        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must update a vm with your Id");

        try {
            teamService.switchVm(id,Long.valueOf(teamId),Long.valueOf(vmId));
        }
        catch (StudentNotFoundException | TeamNotFoundException | VmNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        } catch (MaxVmAcceseException | TooManyResourcesUsedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}/teams/{teamId}/vms/{vmId}/addOwner")
    public void addOwnersToVm(@PathVariable String id, @PathVariable String teamId, @PathVariable String vmId, @AuthenticationPrincipal UserDetails userDetails, @RequestParam("ownerList") List<String> ownerList){
        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You must update a vm with your Id");
        System.out.println(ownerList);
        try {
            teamService.addVmOwner(id, Long.valueOf(teamId), Long.valueOf(vmId), ownerList);
        }
        catch (StudentNotFoundException | TeamNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
        catch (StudentHasNotPrivilegeException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @GetMapping("/{id}/teams/{teamId}/vms/{vmId}/isOwner")
    public boolean isOwner(@PathVariable String id, @PathVariable String teamId, @PathVariable String vmId, @AuthenticationPrincipal UserDetails userDetails) {
        if(!id.equals(userDetails.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Forbidden");
        return teamService.isOwner(id,Long.valueOf(teamId),Long.valueOf(vmId));
    }


    //Accetta o rifiuta gruppo ( REST ) dalla tabela

    @GetMapping("/confirm/{token}")
    public void acceptRequest(@PathVariable String token){
       try {
           notificationService.confirm(token);
       } catch (TokenNotFoundException | TokenExpiredException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @GetMapping("/reject/{token}")
    public void rejectRequest(@PathVariable String token){
        try {
            notificationService.reject(token);
        } catch (TokenNotFoundException | TokenExpiredException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }
}
