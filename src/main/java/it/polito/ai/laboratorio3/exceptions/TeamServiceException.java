package it.polito.ai.laboratorio3.exceptions;

public class TeamServiceException extends RuntimeException {
    public TeamServiceException(String err){
        super(err);
    }
}
