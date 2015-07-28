/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.csbsju.socs.grammar.EditorFrame.ControlPanel;
import edu.csbsju.socs.grammar.Grammar.Element;
import edu.csbsju.socs.grammar.Grammar.Symbol;

class Generator {
	private static final int MAX_DEPTH = 50;
	private static final int MAX_TRIES = 50;
	private static java.util.Random rand = new java.util.Random();
	private static int SCELTA = 1;
	public static final int RUN=0, NOT_RUN = 1;
	private static int current_run = RUN;
	private static nodo ricorsione;
	
	private static Symbol sc_root;
	private static int sc_depth, sc_i_prev;
	private static Grammar sc_g;
	private static ArrayList sc_log;
	private static Element[] sc_element_prev;
	private static Collection sc_rules;
	private static nodo sc_parent;

	public static class GenerateException extends Exception {
		public GenerateException(String msg) { super(msg); }
	}

	public static List generate(Grammar g) throws GenerateException {
		if(current_run == RUN)
		{
		
		int try_count = 0;
		boolean verified = false;
		while(try_count < MAX_TRIES) {
			++try_count;
			try {
				ArrayList log = new ArrayList();
				ricorsione = new nodo();
				Stack st = new Stack();
				ricorsione.setStato(new state(null, 0, g.getRoot()));
				generateSub(g.getRoot(), 0, g, log,null,0,ricorsione,st);
				return log;
			} catch(GenerateException e) {
				// ok, we must have gone too deep. See whether
				// generation is even possible
				if(!verified) {
					if(!terminates(g)) {
						throw new GenerateException(Strings.get("generateEmptyError"));
					}
					verified = true;
				}
			}
		}
		
		throw new GenerateException(Strings.get("generateFailedError"));
		}
		else
			return null;
	}
	
	private static void generateSub(Grammar.Symbol root, int depth,
			Grammar g, ArrayList log, Grammar.Element[] element_prev, int i_prev,nodo parent,Stack st) throws GenerateException {
		if(current_run == RUN){
		if(depth > MAX_DEPTH) {
			throw new GenerateException("maximum depth exceeded");
		}
		Collection rules = g.getRules(root); //prende tutte le regole con radice root
		int numRegole = rules.size();
			//------
		sc_root = root;
		sc_depth = depth;
		sc_i_prev = i_prev;
		sc_g = g;
		sc_log = log;
		sc_element_prev = element_prev;
		sc_rules = rules;
		sc_parent = parent;
		
		if((numRegole) > 1)//c'è più di una produzione
		{
			generateSub_DrawFinestraScelta(root, depth, g, log, element_prev, i_prev, rules,parent, false,st);
		}
		else// non bisogna effettuare una scelta
		{
			SCELTA = 0;
		}
		
		generateSub_ChiamataRicorsiva(root, depth, g, log, element_prev, i_prev, rules, parent,st);
	}
	}
	
	public static void generateSub_DrawFinestraScelta(Grammar.Symbol root, int depth,
			Grammar g, ArrayList log, Grammar.Element[] element_prev, int i_prev, Collection rules, nodo parent, boolean end,Stack st)
	{
		//----------Parte riguardante i radiobutton----------
		int contatore = 0;
		Iterator it_frame = rules.iterator();
		frameScelta finestraScelta = new frameScelta("", 800, 600, end);
		while(it_frame.hasNext()) {
			Grammar.Rule rule2 = (Grammar.Rule) it_frame.next();
			//-----
			finestraScelta.addRadio(rule2.toString2(),contatore);
			contatore++;
		}
		//--------- END radiobutton -------------------
		
		//----- PARTE RIGUARDANTE LA STAMPA DEL MESS PARZIALE ---------
		StringBuffer buf = new StringBuffer();
		Iterator LOG_it = log.iterator();
		while(LOG_it.hasNext()) {
			String tempS = LOG_it.next().toString();
			buf.append(tempS);
		}
		//-------------------------------------------------------------
		finestraScelta.setMessage(rules,ricorsione,log,st);
		finestraScelta.setCurVar(root, depth, g, log, element_prev, i_prev,rules);
		finestraScelta.setVisible(true);
		
	}
	
	public static void generateSub_ChiamataRicorsiva(Grammar.Symbol root, int depth,
			Grammar g, ArrayList log, Grammar.Element[] element_prev, int i_prev, Collection rules,nodo parent,Stack st) throws GenerateException
	{
		Iterator it = rules.iterator();
		int count2 =0;
		while(it.hasNext()) {
			Grammar.Rule rule = (Grammar.Rule) it.next();
			if(count2 == SCELTA) {
				Grammar.Element[] rhs = rule.getRightSide();
				for(int i = 0; i < rhs.length; i++) {
					Grammar.Element e = rhs[i];
					if(e instanceof Grammar.Symbol) {
						
						for(int i2 = rhs.length-1; i2 > i; i2--)
						{
							st.push(rhs[i2].getName());
						}
						
						nodo parent_temp = new nodo(null, i, e);
						parent.getStato().setElements(rhs);
						parent.addChild(parent_temp);
						generateSub((Grammar.Symbol) e, depth + 1, g, log, rhs, i, parent_temp,st);
						for(int i2 = i+1; i2 < rhs.length; i2++)
						{
							st.pop();
						}
					}
					else {
						parent.addChild(new nodo(null, i, e));
						parent.getStato().setElements(rhs);
						log.add(e);
						
					}
				}
				if(rhs.length==0)
					{
						parent.addChild(new nodo(null, 0, new Grammar.Atom("")));
						parent.getStato().setElements(rhs);
					}
				break;
			}
			count2++;
		}
	}

	private static boolean terminates(Grammar g) {
		// determine which symbols terminate in some string
		// (cf. page 89, Hopcroft and Ullman)
		HashSet terminating = new HashSet();
		boolean changed = true;
		while(changed) {
			changed = false;
			Iterator it = g.getRules().iterator();
			while(it.hasNext()) {
				Grammar.Rule rule = (Grammar.Rule) it.next();
				Grammar.Symbol lhs = rule.getLeftSide();
				if(terminating.contains(lhs)) continue;
				if(allSymsInSet(rule.getRightSide(), terminating)) {
					terminating.add(lhs);
					changed = true;
				}
			}
		}
		return terminating.contains(g.getRoot());
	}
	private static boolean allSymsInSet(Grammar.Element[] rhs, Set q) {
		for(int i = 0; i < rhs.length; i++) {
			if(rhs[i] instanceof Grammar.Symbol
					&& !q.contains(rhs[i])) {
				return false;
			}
		}
		return true;
	}
	
	public static void setScelta(int n)
	{
		SCELTA = n;
	}
	public static int getScelta()
	{return SCELTA;}

	public static int getCurrent_run() {
		return current_run;
	}

	public static void setCurrent_run(int c) {
		current_run = c;
	}

	public static Symbol getSc_root() {
		return sc_root;
	}

	public void setSc_root(Symbol sc_root) {
		this.sc_root = sc_root;
	}

	public static int getSc_depth() {
		return sc_depth;
	}

	public void setSc_depth(int sc_depth) {
		this.sc_depth = sc_depth;
	}

	public static int getSc_i_prev() {
		return sc_i_prev;
	}

	public void setSc_i_prev(int sc_i_prev) {
		this.sc_i_prev = sc_i_prev;
	}

	public static Grammar getSc_g() {
		return sc_g;
	}

	public void setSc_g(Grammar sc_g) {
		this.sc_g = sc_g;
	}

	public static Element[] getSc_element_prev() {
		return sc_element_prev;
	}

	public void setSc_element_prev(Element[] sc_element_prev) {
		this.sc_element_prev = sc_element_prev;
	}

	public static Collection getSc_rules() {
		return sc_rules;
	}

	public void setSc_rules(Collection sc_rules) {
		this.sc_rules = sc_rules;
	}

	public static nodo getSc_parent() {
		return sc_parent;
	}

	public void setSc_parent(nodo sc_parent) {
		this.sc_parent = sc_parent;
	}
	
	public static ArrayList getSc_log() {
		return sc_log;
	}

	public void setSc_log(ArrayList sc_log) {
		this.sc_log = sc_log;
	}
}
