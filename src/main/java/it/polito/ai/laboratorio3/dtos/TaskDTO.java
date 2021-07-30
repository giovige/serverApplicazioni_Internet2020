package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    Timestamp dataRilascio;
    Timestamp dataScadenza;
    byte [] description;

    public TaskDTO(){}
}
