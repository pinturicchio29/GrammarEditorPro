/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import edu.csbsju.socs.util.*;

class Strings {
	private static LocaleManager source = null;

	public static void loadDefault() {
		if(source == null) {
			source = new LocaleManager("edu/csbsju/socs/grammar/lang");
		}
	}
	public static String get(String key) {
		if(source == null) loadDefault();
		return source.get(key);
	}
	public static javax.swing.JMenuItem createLocaleMenuItem() {
		loadDefault();
		return source.createLocaleMenuItem();
	}
}
