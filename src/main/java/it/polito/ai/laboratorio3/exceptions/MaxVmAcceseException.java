package it.polito.ai.laboratorio3.exceptions;

public class MaxVmAcceseException extends TeamServiceException {
    public MaxVmAcceseException() {
        super("Reach limit of Vm Accese");
    }
}
