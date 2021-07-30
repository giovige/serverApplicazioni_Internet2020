package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class TokenDTO {

    String id;
    Long teamId;
    String sId;
    String courseName;
    Timestamp expiryDate;
    boolean confirmation;

    public TokenDTO (){}
}
