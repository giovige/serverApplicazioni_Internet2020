package it.polito.ai.laboratorio3.exceptions;

public class InsufficientResourcesException extends TeamServiceException{
    public InsufficientResourcesException (){ super("You have not enough resources in terms of vcpu, Disk or RAM for this VM");}
}
