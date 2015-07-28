/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.csbsju.socs.util.*;

class TreeFrame extends JFrame {
	private class FileMenu extends JMenu implements ActionListener {
		private JMenuItem export = new JMenuItem();
		private JMenuItem print = new JMenuItem();
		private JMenuItem close = new JMenuItem();

		private FileMenu() {
			add(export); export.addActionListener(this);
			add(print);  print.addActionListener(this);
			add(close);  close.addActionListener(this);
		}
		private void renewStrings() {
			this.setText(Strings.get("fileMenu"));
			export.setText(Strings.get("exportGifMenuItem"));
			print.setText(Strings.get("printMenuItem"));
			close.setText(Strings.get("closeMenuItem"));
		}
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == export) doExport();
			if(src == print) doPrint();
			if(src == close) TreeFrame.this.hide();
		}
		private void doExport() {
			chooser.setFileFilter(filter1);
			chooser.setFileHidingEnabled(false);
			int value = chooser.showSaveDialog(TreeFrame.this);
			if(value != chooser.APPROVE_OPTION) return;
			Image img = panel.getImage();
			if(img == null) {
				JOptionPane.showMessageDialog(TreeFrame.this,
					Strings.get("exportNothingError"));
				return;
			}
			try {
				StringTokenizer t = new StringTokenizer(chooser.getSelectedFile().getAbsolutePath(), ".");
				boolean extension = false;
				while(t.hasMoreTokens())
				{
					String temp = t.nextToken();
					if((temp.equalsIgnoreCase("jpeg") ||(temp.equalsIgnoreCase("jpg"))))
					{
						extension = true;
						System.out.println(temp);
					}
				}
				String ex = "";
				if(!extension)
					ex = ".jpeg";
			    File f = new File( chooser.getSelectedFile().getAbsolutePath()+ex);
			    ImageIO.write((RenderedImage) panel.getImage(), "JPEG", f);
			} catch (Exception e) {
			   e.printStackTrace();
			}
		}
		private void doPrint() {
			try {
				PrintUtilities.printComponent(panel);
			} catch(java.awt.print.PrinterException e) {
				JOptionPane.showMessageDialog(TreeFrame.this,
					Strings.get("printError") + ": " + e.getMessage());
			}
		}
	}

	private class WindowCloser extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			TreeFrame.this.hide();
		}
	}

	private FileMenu file_menu = new FileMenu();
	private TreePanel panel = new TreePanel();
	private JFileChooser chooser = new JFileChooser(".");
	FileFilter filter1 = new ExtensionFileFilter("JPG and JPEG", new String[] { "JPG", "JPEG" });
	private JScrollPane panel_scroll;

	public TreeFrame() {
		addWindowListener(new WindowCloser());
		JMenuBar menubar = new JMenuBar();
		menubar.add(file_menu);
		setJMenuBar(menubar);
		chooser.setFileFilter(filter1);
		
		panel_scroll = new JScrollPane(panel);
		getContentPane().add(panel_scroll, BorderLayout.CENTER);
		renewStrings();
		this.setIconImage(EditorFrame.getIcona());
	}

	public void renewStrings() {
		this.setTitle("Grammar editor pro | Albero di conversione");
		file_menu.renewStrings();
		pack();

		JFileChooser newch = new JFileChooser();
		java.io.File f = chooser.getCurrentDirectory();
		if(f != null) newch.setCurrentDirectory(f);
		f = chooser.getSelectedFile();
		if(f != null) newch.setSelectedFile(f);
		chooser = newch;
	}

	public void setTree(Tree t) {
		panel.setTree(t);
	}
}
