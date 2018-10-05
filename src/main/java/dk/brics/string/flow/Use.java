package dk.brics.string.flow;

import java.util.HashSet;
import java.util.Set;

/** 
 * Set of incoming flow edges.
 * Has references to all corresponding definitions. 
 */
public class Use 
{
    Node user;
    Set/*<Node>*/ defs;

    Use(Node user) 
    {
	this.user = user;
	defs = new HashSet/*<Node>*/();
    }

    /** Returns user node for this use. */
    public Node getUser() 
    {
	return user;
    }

    /** 
     * Adds definition node to this use. 
     * Should be invoked if there is a possible flow from the given definition to this use.
     */
    public void addDef(Node def) 
    {
	defs.add(def);
	def.uses.add(this);
    }

    /** Returns set of definition nodes for this use. */
    public Set/*<Node>*/ getDefs() 
    {
	return defs;
    }

}
