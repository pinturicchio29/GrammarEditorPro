package edu.csbsju.socs.grammar;

import java.awt.Font;

import javax.swing.JLabel;

public class labelTreeScelta extends JLabel
{
	private int id;
	private Font font;
	private final static Font f =  new Font("TimesNewRoman", Font.ITALIC, 23);
	public labelTreeScelta(String s, int i)
	{
		super(s);
		this.id = i;
		this.font = f;
		this.setFont(font);
	}
	
	public int getId()
	{return this.id;}
	
	public void setId(int i)
	{ this.id = i;}
	
	public static Font getFontLabel()
	{
		return f;
	}
}
