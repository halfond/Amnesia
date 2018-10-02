/*
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved 
 */
package sql.models;

import java.io.Serializable;

import dk.brics.automaton.State;

public class HotspotToken extends Token implements Serializable{
    private static final long serialVersionUID = 1L;

    public HotspotToken() {
        super();
    }
    
    public HotspotToken(State s) {
        super(s);
    }
    
}
