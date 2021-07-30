package it.polito.ai.laboratorio3.exceptions;

public class CourseNotFoundException extends TeamServiceException {
    public CourseNotFoundException(){
        super("Course not found!");
    }
}
