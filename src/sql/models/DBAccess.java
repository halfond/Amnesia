/*
 * Created on Nov 16, 2005
 *
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package sql.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBAccess implements Serializable {
    static final long serialVersionUID=1;
    
    static public final int NONE=0;
    static public final int ATTACK=2;
    static public final int NORMAL=1;
    
    static int globalID=0;
    
    protected int id=0;
    
    protected Set<TokenTransition> visited;
    protected List<TokenTransition> pathQuery;
    protected List<TokenTransition> pathModel;
    protected String autName;
    protected int type;
    protected String queryString;
    
    public DBAccess() {
    	
    }
    
    public DBAccess(String autName, Set<TokenTransition> visited, List<TokenTransition> pathQuery, List<TokenTransition> pathModel, int type, String queryString) {
        this.autName=autName;
        this.visited=new HashSet<TokenTransition>(visited);
        this.pathQuery=new ArrayList<TokenTransition>(pathQuery);
        this.pathModel=new ArrayList<TokenTransition>(pathModel);
        this.type=type;
        this.queryString=queryString;
        id = globalID;
        globalID++;
    }
    
    public String getUniqueName() {
        StringBuffer temp = new StringBuffer();
        temp.append(id);
        temp.append("-");
        switch (type) {
            case ATTACK:
                temp.append("Attack");
                break;
            case NORMAL:
                temp.append("Normal");
                break;
            case NONE:
                temp.append("Unknown");
                break;
        }
        return temp.toString();
    }
    
   
    public int getID() {
        return id;
    }
    
    public List<TokenTransition> getPathQuery() {
        return pathQuery;
    }

    public List<TokenTransition> getPathModel() {
	    return pathModel;
    }
    
    public String getAutName() {
        return autName;
    }
    
    public int getType() {
        return type;
    }

    public Set<TokenTransition> getVisited() {
    	return visited;
    }
    
	public String getQueryString() {
		return queryString;
	}
}
