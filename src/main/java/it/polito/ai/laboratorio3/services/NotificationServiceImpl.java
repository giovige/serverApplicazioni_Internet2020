package it.polito.ai.laboratorio3.services;

import it.polito.ai.laboratorio3.dtos.ProfessorDTO;
import it.polito.ai.laboratorio3.dtos.StudentDTO;
import it.polito.ai.laboratorio3.dtos.TeamDTO;
import it.polito.ai.laboratorio3.dtos.UserDTO;
import it.polito.ai.laboratorio3.entities.RegistrationToken;
import it.polito.ai.laboratorio3.entities.Student;
import it.polito.ai.laboratorio3.entities.Token;
import it.polito.ai.laboratorio3.entities.User;
import it.polito.ai.laboratorio3.exceptions.StudentNotFoundException;
import it.polito.ai.laboratorio3.exceptions.TokenExpiredException;
import it.polito.ai.laboratorio3.exceptions.TokenNotFoundException;
import it.polito.ai.laboratorio3.exceptions.UserAlreadyRegisterException;
import it.polito.ai.laboratorio3.repositories.RegistrationTokenRepository;
import it.polito.ai.laboratorio3.repositories.StudentRepository;
import it.polito.ai.laboratorio3.repositories.TokenRepository;
import it.polito.ai.laboratorio3.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
@Transactional
public class NotificationServiceImpl implements NotificationService {
    @Bean("teamTemplateMessage")
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Confirm the partecipation at:\nhttp://localhost:8080/API/notification/confirm/%s\n" +
                        "Reject the partecipation at:\nhttp://localhost:8080/API/notification/reject/%s\n");
        return message;
    }

    @Bean(name = "registrationTemplateMessage")
    public SimpleMailMessage registrationTemplateMessage(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Confirm the registration at:\nhttp://localhost:8080/API/notification/confirmRegistration/%s\n"
        );
        return message;
    }


    @Autowired
    public JavaMailSender emailSender;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    RegistrationTokenRepository registrationTokenRepository;

    @Autowired
    TeamService teamService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Qualifier("teamTemplateMessage")
    @Autowired
    SimpleMailMessage template;

    @Qualifier("registrationTemplateMessage")
    @Autowired
    SimpleMailMessage registrationTemplate;

    @Override
    public void sendMessage(String address, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(address);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }

    @Override
    public boolean confirm(String token) {
        System.out.println("\n\n***********\n\n");
        System.out.println(token);
        Optional<Token> tokenOptional = tokenRepository.findById(token);
        if(!tokenOptional.isPresent())
            throw new TokenNotFoundException();
        else{
            if (!tokenOptional.get().getExpiryDate().after(Timestamp.valueOf(LocalDateTime.now())))
                throw new TokenExpiredException();
        }
        tokenOptional.get().setConfirmation(true);
        Long teamId = tokenOptional.get().getTeamId();
        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
        for(Token teamToken: teamTokens){
            if(!teamToken.isConfirmation())
                return false;
        }
        teamService.activeTeam(teamId);
        return true;
    }


    @Override
    public boolean reject(String token) {
        Optional<Token> tokenOptional = tokenRepository.findById(token);
        if(!tokenOptional.isPresent())
            throw new TokenNotFoundException();
        else{
            if (!tokenOptional.get().getExpiryDate().after(Timestamp.valueOf(LocalDateTime.now())))
                throw new TokenExpiredException();
        }
        Long teamId = tokenOptional.get().getTeamId();
        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
        teamTokens.forEach(tk -> tokenRepository.delete(tk));
        teamService.evictTeam(teamId);
        return true;
    }

    @Override
    public void notifyTeam(TeamDTO dto, List<String> memberIds, Long hours) {

        String courseName = teamService.getCourseNameByTeamId(dto.getId());
        Student student;

        for (String s : memberIds) {
            if(!studentRepository.existsById(s))
                throw new StudentNotFoundException();

            student = studentRepository.getOne(s);
            System.out.println("\n\n********\n\n");
            System.out.println(student.getId());

            String tokendId = UUID.randomUUID().toString();
            Long teamId = dto.getId();
            Timestamp expiryDate = Timestamp.valueOf(LocalDateTime.now().plusHours(hours));
            Token token = new Token();
            token.setId(tokendId);
            token.setTeamId(teamId);
            token.setSId(s);
            token.setExpiryDate(expiryDate);
            token.setCourseName(courseName);
            token = tokenRepository.save(token);
            token.setStudent(student);
            //Adesso funziona
            tokenRepository.save(token);


            String bodyMessage = String.format(template.getText(),tokendId,tokendId);
            sendMessage(s+"@studenti.polito.it","Team confirmation",bodyMessage);
        }
    }

    @Override
    public void notifyRegistration(UserDTO userDTO) {
        System.out.println(userDTO);
        String[] username = userDTO.getEmail().split("@");
        if(userRepository.existsById(username[0]))
            throw new UserAlreadyRegisterException();
        if(username[1].startsWith("polito.it") || username[1].startsWith("studenti.polito.it"))
        {
        String tokenId = UUID.randomUUID().toString();
        Timestamp expiryDate = Timestamp.valueOf(LocalDateTime.now().plusHours(24));
        RegistrationToken token = new RegistrationToken();
        token.setId(tokenId);
        token.setExpirationDate(expiryDate);
        token.setUserName(userDTO.getNome());
        token.setUserSurname(userDTO.getCognome());
        token.setUserPassword(passwordEncoder.encode(userDTO.getPassword()));
        token.setUserEmail(userDTO.getEmail());
        token.setUserRole(userDTO.getRole());
        token.setUserMatricola(userDTO.getUsername());

        registrationTokenRepository.save(token);

        String bodyMessage = String.format(registrationTemplate.getText(),tokenId);
        sendMessage(userDTO.getEmail(),"Registration confirmation", bodyMessage);
    } }

    @Override
    public UserDetails confirmRegistration(String token) {
        Optional<RegistrationToken> tokenOptional = registrationTokenRepository.findById(token);
        if(!tokenOptional.isPresent())
            throw new TokenNotFoundException();
        else{
            if (!tokenOptional.get().getExpirationDate().after(Timestamp.valueOf(LocalDateTime.now())))
                throw new TokenExpiredException();
        }
        RegistrationToken registrationToken = tokenOptional.get();
        registrationTokenRepository.delete(registrationToken);
        User user = User.builder()
                .username(registrationToken.getUserMatricola())
                .password(registrationToken.getUserPassword())
                .roles(Arrays.asList(registrationToken.getUserRole()))
                .build();
        userRepository.save(user);
        if(registrationToken.getUserRole().equals("ROLE_STUDENT")){
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setName(registrationToken.getUserName());
            studentDTO.setFirstName(registrationToken.getUserSurname());
            studentDTO.setId(registrationToken.getUserMatricola());
            teamService.addStudent(studentDTO,new byte[0]);
        }else{
            if(registrationToken.getUserRole().equals("ROLE_PROFESSOR")){
                ProfessorDTO professorDTO = new ProfessorDTO();
                professorDTO.setName(registrationToken.getUserName());
                professorDTO.setFirstName(registrationToken.getUserSurname());
                professorDTO.setId(registrationToken.getUserMatricola());
                teamService.addProfessor(professorDTO);
            }
        }
        return user;
    }

}
