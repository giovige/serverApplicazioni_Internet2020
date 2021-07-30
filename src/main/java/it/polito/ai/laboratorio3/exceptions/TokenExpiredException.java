package it.polito.ai.laboratorio3.exceptions;

public class TokenExpiredException extends TeamServiceException {
    public TokenExpiredException(){super("Token expired");}
}
