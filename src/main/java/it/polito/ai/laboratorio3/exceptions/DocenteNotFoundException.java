package it.polito.ai.laboratorio3.exceptions;

public class DocenteNotFoundException extends TeamServiceException {
    public DocenteNotFoundException() {
        super("Id Docente not found");
    }
}
