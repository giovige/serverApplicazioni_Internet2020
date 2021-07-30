package it.polito.ai.laboratorio3.exceptions;

public class TeamNotFoundException extends TeamServiceException {
    public TeamNotFoundException(){super("Team not present!");}
}
