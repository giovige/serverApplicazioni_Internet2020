package it.polito.ai.laboratorio3.exceptions;

public class TaskExpiredException extends TeamServiceException {
        public TaskExpiredException(){
            super("Task expired");
        }
}

