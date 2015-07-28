/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.*;
import java.net.URL;
import javax.swing.text.EditorKit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class HelpFrame extends JFrame
		implements HyperlinkListener {
	public static class NotFoundException extends Exception {
		public NotFoundException(String msg) { super(msg); }
	}

	protected class ContentMenuItem extends JMenuItem
			implements ActionListener {
		URL url;

		public ContentMenuItem(String title, URL url) {
			super(title);
			this.url = url;
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				load(url);
			} catch(NotFoundException ex) {
				JOptionPane.showMessageDialog(HelpFrame.this,
					ex.getMessage());
			}
		}
	}

	protected class History {
		LinkedList urls = new LinkedList();
		protected int pos = -1;

		public void init(URL first) throws NotFoundException {
			loadURL(first);
			pos = 0;
			urls.clear();
			urls.add(first);
		}
		public URL getCurrent() {
			if(pos < 0 || pos >= urls.size()) return null;
			return (URL) urls.get(pos);
		}
		public void back() {
			if(pos - 1 >= 0) {
				--pos;
				try { loadURL((URL) urls.get(pos));
				} catch(NotFoundException e) { }
			}
		}
		public void forward() {
			if(pos + 1 < urls.size()) {
				++pos;
				try { loadURL((URL) urls.get(pos));
				} catch(NotFoundException e) { }
			}
		}
		public void addURL(URL url) throws NotFoundException {
			URL current = getCurrent();
			if(current != null && url.equals(current)) return;
			if(loadURL(url)) {
				while(urls.size() > pos + 1) {
					urls.remove(pos + 1);
				}
				urls.add(url);
				pos = urls.size() - 1;
			}
		}
	}

	private class FileMenu extends JMenu implements ActionListener {
		JMenuItem close = new JMenuItem();

		FileMenu() {
			add(close); close.addActionListener(this);
		}

		public void renewStrings() {
			this.setText(Strings.get("fileMenu"));
			close.setText(Strings.get("closeMenuItem"));
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == close) HelpFrame.this.hide();
		}
	}

	private class GoMenu extends JMenu implements ActionListener {
		JMenuItem back = new JMenuItem();
		JMenuItem forward = new JMenuItem();

		GoMenu() {
			add(back); back.addActionListener(this);
			add(forward); forward.addActionListener(this);
			addSeparator();

			back.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_B, ActionEvent.ALT_MASK));
			forward.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F, ActionEvent.ALT_MASK));
		}

		public void renewStrings() {
			this.setText(Strings.get("goMenu"));
			back.setText(Strings.get("backMenuItem"));
			forward.setText(Strings.get("forwardMenuItem"));
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == back) history.back();
			if(src == forward) history.forward();
		}
	}

	protected FileMenu		menu_file = new FileMenu();
	protected GoMenu		menu_go = new GoMenu();
	protected JEditorPane	editor = new JEditorPane();
	protected JScrollPane	scroll_pane;
	protected History		history = new History();
	protected String		base_prefix = null;
	protected ArrayList     contents_items = new ArrayList();

	public HelpFrame() {
		JMenuBar menubar = new JMenuBar();
		menubar.add(menu_file);
		menubar.add(menu_go);
		setJMenuBar(menubar);

        editor.setEditable(false);
		editor.setContentType("text/html");
		editor.addHyperlinkListener(this);
		editor.setPreferredSize(new Dimension(800, 600));

		scroll_pane = new JScrollPane(editor);
		scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll_pane.setMinimumSize(new Dimension(10, 10));
		getContentPane().add(scroll_pane);
		renewStrings();
	}

	public void setLocation(URL url) throws NotFoundException {
		history.init(url);
	}

	public void renewStrings() {
		this.setTitle(Strings.get("helpDefaultTitle"));
		menu_file.renewStrings();
		menu_go.renewStrings();
		pack();
	}

	public URL getURL(String loc) throws NotFoundException {
		return HelpFrame.class.getClassLoader().getResource(loc);
	}

	public void clearContentsItems() {
		Iterator it = contents_items.iterator();
		while(it.hasNext()) {
			ContentMenuItem item = (ContentMenuItem) it.next();
			menu_go.remove(item);
		}
		contents_items.clear();
	}
	public void addContentsItem(String title, URL url) {
		ContentMenuItem to_add = new ContentMenuItem(title, url);
		contents_items.add(to_add);
		menu_go.add(to_add);
	}

	protected void showError(String message) {
		editor.setText("<h1>" + Strings.get("helpErrorTitle") + "</h1>\n"
			+ "<p>" + message + "</p>\n");
	}

	public URL getCurrent() {
		return history.getCurrent();
	}

	public void load(URL url) throws NotFoundException {
		if(url == null) {
			throw new NotFoundException(Strings.get("helpUrlMissingError")
				+ ": null");
		}
		history.addURL(url);
	}

	protected boolean loadURL(URL url) throws NotFoundException {
		try {
			try {
				HTMLDocument doc = (HTMLDocument) editor.getDocument();
				doc.setBase(url);
				load(url.openStream());
			} catch(java.io.IOException e) {
				throw new NotFoundException(Strings.get("helpUrlMissingError")
					+ ": " + url
					+ " (" + url.getProtocol() + "): "
					+ e.getMessage());
			}
			return true;
		} catch(Throwable e) {
			showError(e.getMessage());
			return false;
		}
	}


	protected void load(java.io.InputStream stream) {
		// slurp up the file into a StringBuffer
		StringBuffer file_contents = new StringBuffer();
		java.io.Reader reader = new java.io.InputStreamReader(stream);
		java.io.BufferedReader fin = new java.io.BufferedReader(reader);
		try {
			while(true) {
				String line = fin.readLine();
				if(line == null) break;
				file_contents.append(line);
			}
		} catch(java.io.IOException e) { }
		try {
			stream.close();
		} catch(java.io.IOException e) { }

		// now display the file
		editor.getEditorKit().createDefaultDocument();
		editor.setText(file_contents.toString());
		editor.setCaretPosition(0);
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) e.getSource();
			if(e instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				try {
					URL url = e.getURL();
					if(url == null) {
						url = new URL(history.getCurrent(), e.getDescription());
					}
					load(url);
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
