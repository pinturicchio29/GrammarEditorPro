package edu.csbsju.socs.grammar;

import java.util.ArrayList;
import java.util.Collection;

public class statoRicorsivo 
{
	private Grammar.Symbol root;
	private Grammar g;
	private int depth, i_prev;
	private ArrayList log;
	private Grammar.Element[] element_prev;
	private frameScelta finestraScelta;
	private Collection rules;
	
	public statoRicorsivo(Grammar.Symbol r, int d,
			Grammar grammatica, ArrayList l, Grammar.Element[] e_p, int i_p, frameScelta fs, Collection ru)
	{
		this.root = r;
		this.g = grammatica;
		this.depth = d;
		this.i_prev = i_p;
		this.log = l;
		this.element_prev = e_p;
		this.finestraScelta = fs;
		this.setRules(ru);
	}
	
	public statoRicorsivo(frameScelta fs)
	{
		this.root = null;
		this.g =null;
		this.depth = 0;
		this.i_prev = 0;
		this.log = null;
		this.element_prev = null;
		this.finestraScelta = fs;
		this.setRules(null);
	}
	
	public void set(Grammar.Symbol r, int d,
			Grammar grammatica, ArrayList l, Grammar.Element[] e_p, int i_p, Collection ru)
	{
		this.root = r;
		this.g = grammatica;
		this.depth = d;
		this.i_prev = i_p;
		this.log = l;
		this.element_prev = e_p;
		this.setRules(ru);
	}
	
	public void reset()
	{
		this.root = null;
		this.g =null;
		this.depth = 0;
		this.i_prev = 0;
		this.log = null;
		this.element_prev = null;
		this.setRules(null);
	}

	public Grammar getG() {
		return g;
	}

	public void setG(Grammar grammatica) {
		this.g = grammatica;
	}

	public Grammar.Symbol getRoot() {
		return root;
	}

	public void setRoot(Grammar.Symbol root) {
		this.root = root;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getI_prev() {
		return i_prev;
	}

	public void setI_prev(int i_prev) {
		this.i_prev = i_prev;
	}

	public ArrayList getLog() {
		return log;
	}

	public void setLog(ArrayList log) {
		this.log = log;
	}
	
	public Grammar.Element[] getElement_prev() {
		return this.element_prev;
	}

	public void setElement_prev(Grammar.Element[] e) {
		this.element_prev = e;
	}
	
	public frameScelta getFinestraScelta()
	{return this.finestraScelta;}
	
	public void setFinestraScelta(frameScelta fs)
	{this.finestraScelta = fs;}
	
	public boolean isNull()
	{
		return this.root == null;
	}

	public Collection getRules() {
		return rules;
	}

	public void setRules(Collection rules) {
		this.rules = rules;
	}
}

