package dk.brics.string.mlfa;

import dk.brics.automaton.Automaton;

abstract class MLFATransition {}

class AutomatonTransition extends MLFATransition
{
    Automaton a;

    AutomatonTransition(Automaton a)
    {
	this.a = a;
    }

    public String toString()
    {
	return "{"+a.getInfo()+"}";
    }
}

class EpsilonTransition extends MLFATransition 
{
    public String toString()
    {
	return "\"\"";
    }
}

class IdentityTransition extends MLFATransition
{
    MLFAState s, f;

    IdentityTransition(MLFAState s, MLFAState f)
    {
	this.s = s;
	this.f = f;
    }

    public String toString()
    {
	return "("+s+","+f+")";
    }
}

class UnaryTransition extends MLFATransition
{
    UnaryOperation op;
    MLFAState s, f;

    UnaryTransition(UnaryOperation op, MLFAState s, MLFAState f)
    {
	this.op = op;
	this.s = s;
	this.f = f;
    }

    public String toString()
    {
	return op+"("+s+","+f+")";
    }
}

class BinaryTransition extends MLFATransition
{
    BinaryOperation op;
    MLFAState s1, f1, s2, f2;

    BinaryTransition(BinaryOperation op, MLFAState s1, MLFAState f1, MLFAState s2, MLFAState f2)
    {
	this.op = op;
	this.s1 = s1;
	this.f1 = f1;
	this.s2 = s2;
	this.f2 = f2;
    }

    public String toString()
    {
	return op+"(("+s1+","+f1+"),("+s2+","+f2+"))";
    }
}
