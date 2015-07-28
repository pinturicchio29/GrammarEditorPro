package edu.csbsju.socs.grammar;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;

import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;


public class TreePanelToggleButton extends JToggleButton
{
	private int id;
	
	public TreePanelToggleButton(String s, int i)
	{
		super(s);
		this.setBorder(null);
		this.setBorderPainted(false);
	    setContentAreaFilled(false);
	    setRolloverEnabled(true);
	    this.setMargin(new Insets(0,0,0,0));
	    
	  //other stuff I always do to my image-buttons:
	    this.setFocusPainted(false);
	    
	    this.setContentAreaFilled(false);
	    this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	     
	    //not sure exactly if this even does anything...
	    this.setVerticalAlignment(SwingConstants.TOP);
	    this.setAlignmentX(TOP_ALIGNMENT);
	    
		this.id=i;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void setId(int i)
	{
		this.id = i;
	}
}
 