package it.polito.ai.laboratorio3.exceptions;

import it.polito.ai.laboratorio3.exceptions.TeamServiceException;

public class WrongTeamDimensionExcpetion extends TeamServiceException {
    public WrongTeamDimensionExcpetion(){
        super("Team dimension is wrong!");
    }
}
