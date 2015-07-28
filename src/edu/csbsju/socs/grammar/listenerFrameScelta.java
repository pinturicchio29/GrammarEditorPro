package edu.csbsju.socs.grammar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class listenerFrameScelta implements ActionListener
{

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof radioScelta)
		{
			radioScelta r = (radioScelta)e.getSource();
			Generator.setScelta(r.getIndice());
		}
	}

}
