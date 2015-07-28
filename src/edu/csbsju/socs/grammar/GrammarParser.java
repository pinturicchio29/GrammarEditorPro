/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

class GrammarParser {
	public static class ParseException extends Exception {
		private int line_num;
		private ParseException(String message) { this(-1, message); }
		private ParseException(int line_num, String message) {
			super(message);
			this.line_num = line_num;
		}
		public String toString() {
			if(line_num < 0) {
				return getMessage();
			} else {
				return Strings.get("grammarLinePrefix") + " " + line_num
					+ "- " + getMessage();
			}
		}
	}
	
	/*
	 * Verifica se la stringa s è composta da simboli terminali e non
	 */
	public static boolean isCompSymbol(String s)
	{
		int upper = 0;
		int lover = 0;
		
		for(int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if(Character.isUpperCase(c))
			{
				upper++;
			}
			else
			{
				lover++;
			}
		}
		
		if((lover>0) && (upper>0)) return true;
		else if(upper > 1) return true;
		else return false;
	}
	public static String purificaStr(String buf)
	{
		return buf.toString().replaceAll("\" ", "").replaceAll(" \"", "");
	}
	/*
	 * Controlla se un token è composto da simboli terminali e non terminali in talcaso li separa in più token
	 */
	public static ArrayList<String> separaSingolaProduzione(String tok)
	{
		ArrayList<String> s = new ArrayList<String>();
		boolean prev_is_upper = false;
		String cur = "";
		if(isCompSymbol(tok))//il token è composto da simboli terminali e non
		{
			for(int i = 0; i < tok.length(); i++) 
			{
				char c = tok.charAt(i);
				if(!Character.isUpperCase(c))
				{
					if(prev_is_upper)
					{
						cur = ""+c;
						prev_is_upper = false;
					}
					else
					{
						cur += (""+c);
					}
				}	
				else
				{
					if(prev_is_upper)
					{
						
					}
					else
					{
						if(cur != "")
							s.add(cur);
						prev_is_upper = true;
					}
					cur = "";
					s.add(""+c);
				}
			}
			if(cur != "")
				s.add(cur);
		}
		else
			s.add(tok);
		
		return s;
	}
	
	public static ArrayList<String> splitLine(String line)
	{
		ArrayList<String> s = new ArrayList<String>();
		s.add(""+line.charAt(0));//il primo carattere DEVE essere un simbolo non terminale
		s.add(line.substring(1, 3));//secondo e terzo carattere devon rappresentare la freccetta ->
		String singolaProduzione = "";
		for(int i = 3; i<line.length();i++)//partiamo dal quarto carattere perchè i primi 3 sono sempre gli stessi
		{
			char curr = line.charAt(i);
			if(i == line.length()-1 && curr!='|')
			{
				singolaProduzione += curr;
				if(singolaProduzione != "")
				{
					if(isCompSymbol(singolaProduzione))//produzione composta da simboli terminali e non
					{
						s.addAll(separaSingolaProduzione(singolaProduzione));
					}
					else if(singolaProduzione.compareTo(" ") == 0)//epsiolon produzione
					{
						s.add("-");
					}
					else
					{
						s.add(singolaProduzione);
					}
				}
			}
			else if(curr!='|')
			{
				singolaProduzione += curr;
			}

			else
			{
				if(isCompSymbol(singolaProduzione))//produzione composta da simboli terminali e non
				{
					s.addAll(separaSingolaProduzione(singolaProduzione));
				}
				else if(singolaProduzione.compareTo(" ") == 0)//epsiolon produzione
				{
					s.add("-");
				}
				else
				{
					s.add(singolaProduzione);
				}
				s.add(""+curr);//curr è un |
				singolaProduzione = "";
			}
		}
		
		return s;
	}

	public static Grammar parse(String text) throws ParseException {
		Grammar ret = new Grammar();

		StringTokenizer lines = new StringTokenizer(text, "\n");
		Grammar.Symbol cur_sym = null;
		Grammar.Symbol eps = new Grammar.Symbol("-");
		HashMap map = new HashMap(); // names mapped to Elements
		map.put("-", eps);
		HashMap usage = new HashMap(); // Elements mapped to line #'s
		ArrayList sequence = new ArrayList();
		int line_num = 0;
		while(lines.hasMoreTokens()) 
		{
			++line_num;
			String line = lines.nextToken();
			//-- parte riguardante i commenti --
			int comment = line.indexOf('#');
			if(comment >= 0) line = line.substring(0, comment);
			//----------------------------------

			ArrayList<String> tk = splitLine(line);
			Iterator<String>it_tk = tk.iterator();
			while(it_tk.hasNext()) {
				String tok = it_tk.next();
				if(tok.equals("->")) {
					if(sequence.size() == 0) {
						throw new ParseException(line_num, Strings.get("grammarNeedSymbol"));
					}
					Object last_obj = sequence.remove(sequence.size() - 1);
					if(!(last_obj instanceof Grammar.Symbol)) {
						throw new ParseException(line_num, Strings.get("grammarNotSymbol"));
					}
					Grammar.Symbol last = (Grammar.Symbol) last_obj;
					if(cur_sym == null) {
						if(sequence.size() > 0) {
							throw new ParseException(line_num, Strings.get("grammarOneSymbol"));
						}
						ret.setRoot(last);
					} else {
						addRule(ret, cur_sym, sequence, line_num, eps);
						sequence.clear();
					}
					cur_sym = last;
				} else if(tok.equals("|")) {
					if(cur_sym == null) {
						throw new ParseException(line_num, Strings.get("grammarBadBar"));
					}
					addRule(ret, cur_sym, sequence, line_num, eps);
					sequence.clear();
				} else {
					Grammar.Element e = (Grammar.Element) map.get(tok);
					if(e == null) {
						// verify token is valid
						boolean ok = true;
						boolean is_sym = true;
						for(int i = 0; ok && i < tok.length(); i++) {
							char c = tok.charAt(i);
							if(!Character.isDigit(c) && !Character.isLetter(c)) 
							{
								//(c != ' ')//simboli dell'alfab diverse dalle lettere
									ok = false;
							}
							if(!Character.isUpperCase(c)) {
								is_sym = false;
							}
						}

						if(!ok) {
							throw new ParseException(line_num,
								Strings.get("grammarBadToken")
								+ ": " + tok);
						}

						if(is_sym) 
							e = new Grammar.Symbol(tok);
						else       
							e = new Grammar.Atom(tok);
						map.put(tok, e);
						usage.put(e, new Integer(line_num));
					}
					sequence.add(e);
				}
			}
		}
		addRule(ret, cur_sym, sequence, line_num, eps);

		// confirm that all symbols used are defined
		if(!ret.getLeftSideSymbols().containsAll(ret.getSymbols())) {
			Iterator it = ret.getSymbols().iterator();
			while(it.hasNext()) {
				Grammar.Symbol sym = (Grammar.Symbol) it.next();
				if(ret.getRules(sym) == null) {
					Integer line = (Integer) usage.get(sym);
					throw new ParseException(line == null ? -1 : line.intValue(),
						Strings.get("grammarNullSymbol") + " " + sym);
				}
			}
		}
		if(ret.getRoot() == null) {
			throw new ParseException(Strings.get("grammarNoRootError"));
		}
		Collection root_rules = ret.getRules(ret.getRoot());
		if(root_rules == null || root_rules.size() == 0) {
			throw new ParseException(Strings.get("grammarNullRootError"));
		}
		return ret;
	}
	private static void addRule(Grammar ret, Grammar.Symbol lhs,
			ArrayList rhs, int line_num, Grammar.Symbol eps)
			throws ParseException {
		if(rhs.contains(eps)) {
			// if epsilon is in sequence, make sure it is
			// alone, and remove it.
			if(rhs.size() > 1) {
				throw new ParseException(line_num,
					Strings.get("grammarEpsNotAlone"));
			}
			ret.addRule(lhs, new Grammar.Element[] { });
		} else {
			ret.addRule(lhs, toElements(rhs));
		}
	}

	private static Grammar.Element[] toElements(ArrayList l) {
		Grammar.Element[] ret = new Grammar.Element[l.size()];
		int i = 0;
		Iterator it = l.iterator();
		while(it.hasNext()) {
			ret[i] = (Grammar.Element) it.next();
			i++;
		}
		return ret;
	}
}
