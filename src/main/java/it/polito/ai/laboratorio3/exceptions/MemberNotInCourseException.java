package it.polito.ai.laboratorio3.exceptions;

public class MemberNotInCourseException extends TeamServiceException {
    public MemberNotInCourseException(){
        super("Member not in course!");
    }
}
