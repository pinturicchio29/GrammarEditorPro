package edu.csbsju.socs.grammar;

import java.util.ArrayList;
import java.util.Iterator;

public class nodo {
	private state stato;
	private ArrayList<nodo> figli;
	
	public nodo ()
	{
		this.stato = null;
		this.figli = new ArrayList<nodo>();
	}
	public nodo(Grammar.Element[] e, int i, Grammar.Element left)
	{
		setStato(new state(e,i,left));
		this.figli = new ArrayList<nodo>();
	}
	
	public nodo(Grammar.Element[] e, int i,Grammar.Element left,ArrayList<nodo>f)
	{
		setStato(new state(e,i,left));
		this.setFigli(f);
	}

	public state getStato() {
		return stato;
	}

	public void setStato(state stato) {
		this.stato = stato;
	}
	
	public String toString()
	{
		return this.stato.toString();
	}
	
	public String toHtml()
	{
		return this.stato.toHtml();
	}

	public ArrayList<nodo> getFigli() {
		return figli;
	}

	public void setFigli(ArrayList<nodo> figli) {
		this.figli = figli;
	}
	
	public void addChild(nodo n)
	{
		this.figli.add(n);
	}
	
	public boolean isLeaf()
	{
		return this.figli.isEmpty();
	}
	
	public String stampa()
	{
		String s = this.stato.getLeftSimbol().getName()+" -> "+this.stato.toString()+"\n";
		if(!this.figli.isEmpty())
		{
			Iterator<nodo> it = this.figli.iterator();
			while(it.hasNext())
			{
				nodo tmp = it.next();
				s += tmp.stampa();
			}
		}
		
		return s;
	}
}
