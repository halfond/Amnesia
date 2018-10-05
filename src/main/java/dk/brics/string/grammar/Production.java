package dk.brics.string.grammar;

import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.string.mlfa.BinaryOperation;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.Operation;
import dk.brics.string.mlfa.UnaryOperation;
import dk.brics.string.operations.Basic;

abstract class Production 
{
    void addNexts(Set nexts) {}

    void addPrevs(Nonterminal a) {}

    abstract CharSet charsetTransfer();

    boolean isOperationCycle(Component comp)
    {
	return false;
    }

    Operation getOperation()
    {
	return null;
    }
}

class UnitProduction extends Production
{
    Nonterminal b;

    UnitProduction(Nonterminal b)
    {
	this.b = b;
    }

    void addNexts(Set nexts)
    {
	nexts.add(b);
    }

    void addPrevs(Nonterminal a)
    {
	b.prevs.add(a);
    }

    public String toString()
    {
	return b.toString();
    }

    CharSet charsetTransfer()
    {
	return b.charset;
    }
}

class PairProduction extends Production
{
    Nonterminal b, c;
    
    PairProduction(Nonterminal b, Nonterminal c)
    {
	this.b = b;
	this.c = c;
    }

    void addNexts(Set nexts)
    {
	nexts.add(b);
	nexts.add(c);
    }

    void addPrevs(Nonterminal a)
    {
	b.prevs.add(a);
	c.prevs.add(a);
    }

    public String toString()
    {
	return b+" "+c;
    }

    CharSet charsetTransfer()
    {
	return b.charset.union(c.charset);
    }
}

class AutomatonProduction extends Production
{
    Automaton n;

    AutomatonProduction(Automaton n)
    {
	this.n = n;
    }

    public String toString()
    {
	return Basic.getName(n);
    }

    CharSet charsetTransfer()
    {
	return new CharSet(n);
    }
}

class EpsilonProduction extends Production 
{
    public String toString()
    {
	return "\"\"";
    }

    CharSet charsetTransfer()
    {
	return new CharSet();
    }
}

class UnaryProduction extends Production
{
    UnaryOperation op;
    Nonterminal b;

    UnaryProduction(UnaryOperation op, Nonterminal b)
    {
	this.op = op;
	this.b = b;
    }

    void addNexts(Set nexts)
    {
	nexts.add(b);
    }

    void addPrevs(Nonterminal a)
    {
	b.prevs.add(a);
    }

    public String toString()
    {
	return op+"("+b+")";
    }

    CharSet charsetTransfer()
    {
	return op.charsetTransfer(b.charset);
    }

    boolean isOperationCycle(Component comp)
    {
	return comp.nonterminals.contains(b);
    }

    Operation getOperation()
    {
	return op;
    }
}

class BinaryProduction extends Production
{
    BinaryOperation op;
    Nonterminal b, c;

    BinaryProduction(BinaryOperation op, Nonterminal b, Nonterminal c)
    {
	this.op = op;
	this.b = b;
	this.c = c;
    }

    void addNexts(Set nexts)
    {
	nexts.add(b);
	nexts.add(c);
    }

    void addPrevs(Nonterminal a)
    {
	b.prevs.add(a);
	c.prevs.add(a);
    }

    public String toString()
    {
	return op+"("+b+","+c+")";
    }

    CharSet charsetTransfer()
    {
	return op.charsetTransfer(b.charset, c.charset);
    }

    boolean isOperationCycle(Component comp)
    {
	return comp.nonterminals.contains(b) || comp.nonterminals.contains(c);
    }

    Operation getOperation()
    {
	return op;
    }
}
