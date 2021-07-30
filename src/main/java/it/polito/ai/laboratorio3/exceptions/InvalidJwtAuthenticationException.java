package it.polito.ai.laboratorio3.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtAuthenticationException extends AuthenticationException {
    /**
     *
     */
    private static final long serialVersionUID = -761503632186596342L;

    public InvalidJwtAuthenticationException(String e) {
        super(e);
    }
}