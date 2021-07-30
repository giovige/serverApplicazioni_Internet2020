package it.polito.ai.laboratorio3.exceptions;


public class TaskNotFoundException extends TeamServiceException {
    public TaskNotFoundException(){ super("Task not found!");}
}
