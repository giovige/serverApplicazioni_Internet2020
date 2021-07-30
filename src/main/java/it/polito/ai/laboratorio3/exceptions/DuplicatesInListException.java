package it.polito.ai.laboratorio3.exceptions;

public class DuplicatesInListException extends TeamServiceException {
    public DuplicatesInListException(){
        super("Two or more students replicated!");
    }
}
