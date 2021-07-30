package it.polito.ai.laboratorio3.exceptions;

public class UserAlreadyRegisterException extends TeamServiceException {
    public UserAlreadyRegisterException() {
        super("Utente already register");
    }
}
