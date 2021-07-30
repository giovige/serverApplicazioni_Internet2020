package it.polito.ai.laboratorio3.exceptions;

public class TooManyResourcesUsedException extends TeamServiceException {
    public TooManyResourcesUsedException(){super("Team already used these resources");}
}
