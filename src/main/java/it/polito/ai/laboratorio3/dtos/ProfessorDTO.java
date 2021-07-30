package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfessorDTO extends RepresentationModel<ProfessorDTO> {
    private String id;
    private String name;
    private String firstName;
    private byte[] photoDocente;
}
