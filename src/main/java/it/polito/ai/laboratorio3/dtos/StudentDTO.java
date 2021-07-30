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
public class StudentDTO extends RepresentationModel<StudentDTO> {
    @CsvBindByName
    private String id;
    @CsvBindByName
    private String firstName;
    @CsvBindByName
    private String name;
    private byte[] photoStudent;
}
