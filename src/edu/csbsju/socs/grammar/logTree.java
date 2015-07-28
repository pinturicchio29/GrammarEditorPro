package edu.csbsju.socs.grammar;

public class logTree 
{
	private nodo root;
	
	public logTree ()
	{
		this.root = null;
	}
	
	public boolean isEmpty()
	{
		return this.root == null;
	}
	
	public nodo getRoot()
	{return this.root;}
	
	public void setRoot(nodo r)
	{this.root = r;}
	
	
}
