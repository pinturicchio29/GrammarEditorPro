/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;

class Grammar {
	private static int last_alloc = 0;

	public static class Element {
		private String name;

		private Element() {
			this.name = "temp" + last_alloc;
			last_alloc++;
		}
		private Element(String name) {
			this.name = name;
		}

		public String getName() { return name; }
		public String toString() { return name; }
	}

	public static class Symbol extends Element {
		public Symbol() { super(); }
		public Symbol(String name) { super(name); }
	}
	
	public static class ComposeSymbol extends Symbol {
		private String prefix = "", suffix = "";
		
		public ComposeSymbol() 
		{ 
			super();
		}
		public ComposeSymbol(String name) 
		{ 
			super(name);
		}
		
		public ComposeSymbol(String name, String pre, String suf)
		{
			super(name);
			this.setPrefix(pre);
			this.setSuffix(suf);
		}
		public String getPrefix() {
			return prefix;
		}
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
		public String getSuffix() {
			return suffix;
		}
		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}
		
	}
	

	public static class Atom extends Element {
		public Atom() { super(); }
		public Atom(String name) { super(name); }
	}

	public static class Rule {
		private Symbol lhs;
		private Element[] rhs;

		public Rule(Symbol lhs, Element[] rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		public Symbol getLeftSide() { return lhs; }
		public Element[] getRightSide() { return rhs; }

		public String toString() {
			StringBuffer ret = new StringBuffer();
			ret.append(lhs + " ->");
			for(int i = 0; i < rhs.length; i++) {
				ret.append(rhs[i]);
			}
			return ret.toString();
		}
		
		public String toStringRhs() {
			StringBuffer ret = new StringBuffer();
			for(int i = 0; i < rhs.length; i++) {
				ret.append(rhs[i]);
			}
			return ret.toString();
		}
		
		public String toString2() {
			StringBuffer ret = new StringBuffer();
			for(int i = 0; i < rhs.length; i++) {
				ret.append(rhs[i]);
			}
			return ret.toString();
		}

	}

	private Symbol root = null;
	private HashSet symbols = new HashSet();
	private HashMap atoms = new HashMap();
	private HashSet rules = new HashSet();
	private HashMap rule_map = new HashMap();

	public Grammar() { }

	public void setRoot(Symbol root) {
		this.root = root;
		symbols.add(root);
	}
	public void addRule(Symbol lhs, Element[] rhs) {
		add(new Rule(lhs, rhs));
	}
	public void add(Rule rule) {
		Collection c = getRules(rule.lhs);
		if(c == null) {
			c = new ArrayList();
			rule_map.put(rule.lhs, c);
		}
		c.add(rule);
		rules.add(rule);

		symbols.add(rule.lhs);
		for(int i = 0; i < rule.rhs.length; i++) {
			Element e = rule.rhs[i];
			if(e instanceof Symbol) 
				{
					symbols.add(e);
				}
			if(e instanceof ComposeSymbol) 
			{
				symbols.add(e);
			}
			else atoms.put(e.getName(), e);
		}
	}

	public Symbol getRoot() { return root; }
	public Collection getRules(Symbol lhs) {
		return (Collection) rule_map.get(lhs);
	}
	public Collection getRules() { return rules; }
	public Collection getSymbols() { return symbols; }
	public Collection getAtoms() { return atoms.values(); }
	public Collection getLeftSideSymbols() { return rule_map.keySet(); }
	public Atom getAtom(String name) { return (Atom) atoms.get(name); }

	public void print(java.io.PrintStream out) {
		Iterator it = rule_map.keySet().iterator();
		while(it.hasNext()) {
			Grammar.Symbol sym = (Grammar.Symbol) it.next();
			boolean first = true;
			Iterator it2 = getRules(sym).iterator();
			while(it2.hasNext()) {
				Rule rule = (Rule) it2.next();
				out.print(first ? (sym + " ->") : "   |");
				first = false;
				Grammar.Element[] rhs = rule.getRightSide();
				for(int i = 0; i < rhs.length; i++) {
					out.print(" " + rhs[i]);
				}
				out.println();
			}
		}
	}
}
