package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
public class CourseDTO extends RepresentationModel<CourseDTO> {
    private String name;
    private int min;
    private int max;
    private boolean enabled;

    private int modelVM_cpu;
    private int modelVM_GBDisk;
    private int modelVM_GBRam;

    public CourseDTO(){}

}
