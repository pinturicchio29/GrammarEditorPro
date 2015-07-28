/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class LocaleManager {
	// static members
	private static final String SETTINGS_NAME = "settings";
	private static final String LOCALE_NAME = "str";

	public static interface Listener {
		public void localeChanged();
	}

	private static class LocaleItem extends JRadioButtonMenuItem
			implements ActionListener {
		private Locale locale;
		LocaleItem(Locale locale, ButtonGroup bgroup) {
			this.locale = locale;
			bgroup.add(this);
			addActionListener(this);
			setSelected(locale.equals(LocaleManager.getLocale()));
		}
		public void actionPerformed(ActionEvent evt) {
			if(isSelected()) LocaleManager.setLocale(locale);
		}
	}

	private static class LocaleMenu extends JMenu implements Listener {
		LocaleItem[] items;
		LocaleMenu(Locale[] locales) {
			ButtonGroup bgroup = new ButtonGroup();
			items = new LocaleItem[locales.length];
			for(int i = 0; i < locales.length; i++) {
				items[i] = new LocaleItem(locales[i], bgroup);
				add(items[i]);
			}
			LocaleManager.addListener(this);
			localeChanged();
		}
		public void localeChanged() {
			this.setText(Strings.get("localeMenuItem"));
			Locale current = LocaleManager.getLocale();
			for(int i = 0; i < items.length; i++) {
				LocaleItem it = items[i];
				it.setText(it.locale.getDisplayName(current));
				it.setSelected(it.locale.equals(current));
			}
		}
	}

	private static ArrayList listeners = new ArrayList();

	public static Locale getLocale() { return Locale.getDefault(); }
	public static void setLocale(Locale loc) {
		Locale.setDefault(loc);
		fireLocaleChanged();
	}

	public static void addListener(Listener l) { listeners.add(l); }
	public static void removeListener(Listener l) { listeners.remove(l); }
	private static void fireLocaleChanged() {
		Iterator it = listeners.iterator();
		while(it.hasNext()) ((Listener) it.next()).localeChanged();
	}

	// instance members
	private class MyListener implements Listener {
		public void localeChanged() {
			loadDefault();
		}
	}

	private String dir_name;
	private ResourceBundle settings = null;
	private ResourceBundle locale = null;
	private ResourceBundle dflt_locale = null;

	public LocaleManager(String dir_name) {
		this.dir_name = dir_name;
		loadDefault();
	}

	private void loadDefault() {
		if(settings == null) {
			try {
				settings = ResourceBundle.getBundle(dir_name + "/" + SETTINGS_NAME);
			} catch(java.util.MissingResourceException e) { }
		}

		try {
			loadLocale(Locale.getDefault());
			if(locale != null) return;
		} catch(java.util.MissingResourceException e) { }
		try {
			loadLocale(Locale.US);
			if(locale != null) return;
		} catch(java.util.MissingResourceException e) { }
		Locale[] choices = getLocaleOptions();
		if(choices != null && choices.length > 0) setLocale(choices[0]);
		throw new RuntimeException("No locale bundles are available");
	}
	private void loadLocale(Locale loc) {
		locale = ResourceBundle.getBundle(dir_name + "/" + LOCALE_NAME,
			loc);
		Locale.setDefault(loc);
		if(dflt_locale == null) dflt_locale = locale;
	}

	public String get(String key) {
		try {
			return locale.getString(key);
		} catch(java.util.MissingResourceException e) { }
		try {
			return dflt_locale.getString(key);
		} catch(java.util.MissingResourceException e) { }
		return key;
	}

	public Locale[] getLocaleOptions() {
		String locs = null;
		try {
			if(settings != null) locs = settings.getString("locales");
		} catch(java.util.MissingResourceException e) { }
		if(locs == null) return new Locale[] { };

		ArrayList retl = new ArrayList();
		StringTokenizer toks = new StringTokenizer(locs);
		while(toks.hasMoreTokens()) {
			String f = toks.nextToken();
			String language = f.substring(0, 2);
			String country = f.substring(3, 5);
			Locale loc = new Locale(language, country);
			retl.add(loc);
		}

		Locale[] ret = new Locale[retl.size()];
		for(int i = 0; i < retl.size(); i++) {
			ret[i] = (Locale) retl.get(i);
		}
		return ret;
	}

	public JMenuItem createLocaleMenuItem() {
		Locale[] locales = getLocaleOptions();
		if(locales == null || locales.length == 0) return null;
		else return new LocaleMenu(locales);
	}

}
