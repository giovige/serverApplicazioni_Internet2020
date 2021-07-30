package it.polito.ai.laboratorio3.entities;

import it.polito.ai.laboratorio3.dtos.UserDTO;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Data
public class RegistrationToken {
    @Id
    private String id;
    private Timestamp expirationDate;
    private String userName;
    private String userSurname;
    private String userPassword;
    private String userEmail;
    private String userRole;
    private String userMatricola;

}
