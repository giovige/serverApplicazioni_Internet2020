package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String email;
    private String password;
    private String role;
    private String nome;
    private String cognome;

    public UserDTO(){}
}
