package it.polito.ai.laboratorio3.exceptions;

public class EssayNotFoundException extends TeamServiceException {
    public EssayNotFoundException(){super("Essay not found!");}
}
