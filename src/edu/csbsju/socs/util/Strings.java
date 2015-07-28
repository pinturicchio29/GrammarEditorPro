/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.util;

class Strings {
	private static LocaleManager source = new LocaleManager(
		"edu/csbsju/socs/util/lang");

	public static String get(String key) {
		return source.get(key);
	}
	public static javax.swing.JMenuItem createLocaleMenuItem() {
		return source.createLocaleMenuItem();
	}
}
