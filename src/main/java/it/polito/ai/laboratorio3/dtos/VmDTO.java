package it.polito.ai.laboratorio3.dtos;

import it.polito.ai.laboratorio3.entities.Vm;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VmDTO {
    public enum stati {Accesa, Spenta}

    private Long id;
    private int vcpu;
    private int GBDisk;
    private int GBRam;
    private Vm.stati status;
    private String idCreatore;
    private byte[] screenVm;

    public VmDTO(){}
}
