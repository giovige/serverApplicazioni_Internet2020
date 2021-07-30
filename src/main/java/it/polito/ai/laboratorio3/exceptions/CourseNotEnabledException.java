package it.polito.ai.laboratorio3.exceptions;

public class CourseNotEnabledException extends TeamServiceException {
    public CourseNotEnabledException(){
        super("Course not enabled!");
    }
}