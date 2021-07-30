package it.polito.ai.laboratorio3.exceptions;

public class EssayNotLoadedByStudentException extends TeamServiceException {
    public EssayNotLoadedByStudentException(){ super("No student has already load an essay for the task!");}
}
