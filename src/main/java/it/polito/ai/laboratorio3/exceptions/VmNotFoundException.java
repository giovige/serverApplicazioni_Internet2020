package it.polito.ai.laboratorio3.exceptions;

public class VmNotFoundException extends TeamServiceException {
    public VmNotFoundException (){super("Vm Not Found");}
}
