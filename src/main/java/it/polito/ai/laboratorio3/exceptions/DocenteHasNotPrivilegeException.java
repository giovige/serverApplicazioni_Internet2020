package it.polito.ai.laboratorio3.exceptions;

public class DocenteHasNotPrivilegeException extends TeamServiceException {
    public DocenteHasNotPrivilegeException() {
        super("Wrong Privileges");
    }
}
