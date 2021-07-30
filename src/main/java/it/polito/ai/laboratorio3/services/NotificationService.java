package it.polito.ai.laboratorio3.services;

import it.polito.ai.laboratorio3.dtos.TeamDTO;
import it.polito.ai.laboratorio3.dtos.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body);
    boolean confirm(String token);
    boolean reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds, Long hours);

    void notifyRegistration(UserDTO userDTO);
    UserDetails confirmRegistration(String token);

}
