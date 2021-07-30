package it.polito.ai.laboratorio3.exceptions;

public class StudentNotFoundException extends TeamServiceException {
    public StudentNotFoundException(){
        super("Student not found!");
    }
}
