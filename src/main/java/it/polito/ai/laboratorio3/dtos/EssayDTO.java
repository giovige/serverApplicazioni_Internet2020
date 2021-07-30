package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class EssayDTO {

    public enum stati {Letto, Consegnato, Rivisto, Terminato}

    private Long id;
    private Long voto;
    public stati stato;
    private String idStudente;
    private Timestamp lastModified;

    public EssayDTO(){}
}
