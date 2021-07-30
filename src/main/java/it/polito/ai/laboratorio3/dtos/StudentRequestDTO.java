package it.polito.ai.laboratorio3.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Lob;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentRequestDTO extends RepresentationModel<StudentRequestDTO> {

    private String id;
    private String firstName;
    private String name;
    private String stato;
}
