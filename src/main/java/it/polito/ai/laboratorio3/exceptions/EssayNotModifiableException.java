package it.polito.ai.laboratorio3.exceptions;

public class EssayNotModifiableException extends TeamServiceException {
    public EssayNotModifiableException() { super("Essay already have a vote");}
}
