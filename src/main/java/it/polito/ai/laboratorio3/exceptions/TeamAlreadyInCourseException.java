package it.polito.ai.laboratorio3.exceptions;

public class TeamAlreadyInCourseException extends TeamServiceException {
    public TeamAlreadyInCourseException(){

        super("The team is already registered in a course! ");
    }
}
