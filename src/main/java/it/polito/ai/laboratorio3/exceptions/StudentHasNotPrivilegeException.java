package it.polito.ai.laboratorio3.exceptions;

public class StudentHasNotPrivilegeException extends TeamServiceException {
    public StudentHasNotPrivilegeException(){ super("This student is not the owner of the VM");}
}
