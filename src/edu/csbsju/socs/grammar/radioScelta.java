package edu.csbsju.socs.grammar;

import javax.swing.JRadioButton;

public class radioScelta extends JRadioButton
{
	private int indice;
	public radioScelta(String n, int c)
	{
		super(n);
		setIndice(c);
	}
	public int getIndice() {
		return indice;
	}
	public void setIndice(int indice) {
		this.indice = indice;
	}
}