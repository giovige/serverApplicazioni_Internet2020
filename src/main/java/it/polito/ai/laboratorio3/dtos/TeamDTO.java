package it.polito.ai.laboratorio3.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamDTO {
    private Long id;
    private String name;
    private int status;
    private int vcpuTot;
    private int GBDiskTot;
    private int GBRamTot;
    private int vcpuUsati;
    private int GBDiskUsati;
    private int GBRamUsati;
    private int maxVmAccese;
    private int vmAccese;
    private String idCreator;

    public TeamDTO(){}
}
