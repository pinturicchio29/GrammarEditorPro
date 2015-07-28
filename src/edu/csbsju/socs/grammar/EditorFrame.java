/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import edu.csbsju.socs.grammar.Generator.GenerateException;
import edu.csbsju.socs.util.*;

/*
 * Frame principale dell'applicazione
 */
public class EditorFrame extends JFrame implements LocaleManager.Listener
{
	private static final String HELP_DIR = "edu/csbsju/socs/grammar/doc/";
	private static Font USER_FONT = new Font("monospaced", Font.PLAIN, 16); //font
	private static Image icon;
	public static final int DIVISIONE = 7;
//=============== MENU ==============================================
	/*
	 * Classe menu file
	 */
	class FileMenu extends JMenu implements ActionListener {
		JMenuItem newi  = new JMenuItem();
		JMenuItem open  = new JMenuItem();
		JMenuItem save  = new JMenuItem();
		JMenuItem print = new JMenuItem();
		JMenuItem quit  = new JMenuItem();

		FileMenu() {
			add(newi);  newi.addActionListener(this);
			add(open);  open.addActionListener(this);
			add(save);  save.addActionListener(this);
			addSeparator();
			add(print); print.addActionListener(this);
			add(quit);  quit.addActionListener(this);
		}
		//assegna le etichette al menu
		public void renewStrings() {
			this.setText(Strings.get("fileMenu"));
			newi.setText(Strings.get("newMenuItem"));
			open.setText(Strings.get("openMenuItem"));
			save.setText(Strings.get("saveMenuItem"));
			print.setText(Strings.get("printMenuItem"));
			quit.setText(Strings.get("quitMenuItem"));
		}

		public void actionPerformed(ActionEvent evt) {
			Object src = evt.getSource();
			if(src == newi) doNew();
			else if(src == open) doOpen();
			else if(src == save) doSave();
			else if(src == print) doPrint();
			else if(src == quit) doQuit();
		}

		private void doNew() {
			if(!confirmLoss(Strings.get("confirmLossMsg"))) return;
			editor.setText("");
			setChanged(false);
		}

		private void doOpen() {
			if(!confirmLoss(Strings.get("confirmLossMsg"))) return;

			int val = chooser.showOpenDialog(this);
			if(val != JFileChooser.APPROVE_OPTION) return;

			StringBuffer buffer = new StringBuffer();
			try {
				java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.FileReader(chooser.getSelectedFile()));
				while(true) {
					String line = reader.readLine();
					if(line == null) break;
					buffer.append(line);
					buffer.append("\n");
				}
				reader.close();
			} catch(java.io.IOException e) {
				JOptionPane.showMessageDialog(this,
					Strings.get("saveError") + e.getMessage());
				return;
			}

			editor.setText(buffer.toString());
			setChanged(false);
		}

		private void doPrint() {
			try {
				PrintUtilities.printText(editor.getText());
			} catch(java.awt.print.PrinterException e) {
				JOptionPane.showMessageDialog(EditorFrame.this,
					Strings.get("printError") + e);
			}
		}
	}
	/*
	 * Classe menu modifica
	 */
	class EditMenu extends JMenu implements ActionListener {
		JMenuItem cut = new JMenuItem();
		JMenuItem copy = new JMenuItem();
		JMenuItem paste = new JMenuItem();

		EditMenu() {
			add(cut);   cut.addActionListener(this);
			add(copy);  copy.addActionListener(this);
			add(paste); paste.addActionListener(this);
		}

		public void renewStrings() {
			this.setText(Strings.get("editMenu"));
			cut.setText(Strings.get("cutMenuItem"));
			copy.setText(Strings.get("copyMenuItem"));
			paste.setText(Strings.get("pasteMenuItem"));
		}

		public void actionPerformed(ActionEvent evt) {
			Object src = evt.getSource();
			if(src == cut)        editor.cut();
			else if(src == copy)  editor.copy();
			else if(src == paste) editor.paste();
		}
	}

	class FontSizeItem extends JRadioButtonMenuItem implements ActionListener {
		private int size;
		FontSizeItem(int size, ButtonGroup bgroup) {
			this.size = size;
			bgroup.add(this);
			addActionListener(this);
			setSelected(size == USER_FONT.getSize());
		}
		public void actionPerformed(ActionEvent evt) {
			if(isSelected()) {
				Font old = editor.getFont();
				if(size != old.getSize()) {
					Font newf = old.deriveFont((float) size);
					editor.setFont(newf);
					controls.field.setFont(newf);
					//EditorFrame.this.pack();
				}
			}
		}
	}

	class OptionsMenu extends JMenu {
		JMenu size = new JMenu();
		FontSizeItem[] size_items;
		OptionsMenu() {
			ButtonGroup bgroup = new ButtonGroup();
			int[] sizes = { 12, 14, 16, 18, 20, 24 };
			size_items = new FontSizeItem[sizes.length];
			for(int i = 0; i < sizes.length; i++) {
				size_items[i] = new FontSizeItem(sizes[i], bgroup);
				size.add(size_items[i]);
			}
			add(size);
		}
		public void renewStrings() {
			this.setText(Strings.get("optionsMenu"));
			size.setText(Strings.get("fontSizeMenuItem"));
			for(int i = 0; i < size_items.length; i++) {
				size_items[i].setText(size_items[i].size
					+ " " + Strings.get("fontSizeUnits"));
			}
		}
	}

	class HelpMenu extends JMenu implements ActionListener {
		private JMenuItem help = new JMenuItem();
		private JMenuItem about = new JMenuItem();
		private JMenuItem helpSlide = new JMenuItem();
		java.net.URL index = null;

		HelpMenu() {
			add(helpSlide); helpSlide.addActionListener(this);
		}
		public void renewStrings() {
			this.setText(Strings.get("helpMenu"));
			help.setText("Guide");
			helpSlide.setText("Impara ad usarmi!");

			help_frame.setTitle(Strings.get("helpTitle"));
			help_frame.clearContentsItems();
			try {
				index = help_frame.getURL(HELP_DIR
					+ Strings.get("helpOverviewLoc"));
				help_frame.setLocation(index);
				help_frame.addContentsItem(Strings.get("helpOverview"),
					index);
				help_frame.addContentsItem(Strings.get("helpSyntax"),
					help_frame.getURL(HELP_DIR + Strings.get("helpSyntaxLoc")));
			} catch(HelpFrame.NotFoundException e) { }
		}
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == help) {
				try {
					if(index == null) {
						index = help_frame.getURL(HELP_DIR
							+ Strings.get("helpOverviewLoc"));
					}
					help_frame.load(index);
					help_frame.show();
				} catch(HelpFrame.NotFoundException e) {
					JOptionPane.showMessageDialog(EditorFrame.this,
						Strings.get("helpNotFoundError") + ": "
						+ e.getMessage());
				}
			}
				else if(event.getSource() == helpSlide) {
					slideHelpFrame fra = new slideHelpFrame();
					fra.show();
				}
				else if(event.getSource() == about) {
					JOptionPane.showMessageDialog(EditorFrame.this,
						Strings.get("aboutMessage"));
				}
		}
	}
//=============== END MENU ==============================================
/*
 * Pannello dei comandi inferiore
 * contiene il parser e il generate
 */
	public class ControlPanel extends JPanel implements ActionListener{
		private JButton parse = new JButton();
		private JButton generate = new JButton();
		private JButton clear = new JButton();
		private JLabel field_label = new JLabel();
		private JTextField field = new JTextField();
		private JTextField invisible_field = new JTextField();

		ControlPanel() {
			super(new BorderLayout());
			
			JPanel controls = new JPanel();
			controls.add(generate); generate.addActionListener(this);
			controls.add(parse);    parse.addActionListener(this);
			controls.add(clear);    clear.addActionListener(this);
			clear.setEnabled(false);
			parse.setEnabled(false);

			field_label.setBorder(BorderFactory.createMatteBorder(5, 5,
				0, 0, getBackground()));
			add(field_label, BorderLayout.WEST);
			field.addActionListener(this);
			field.setFont(USER_FONT);
			field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(5, 5, 0, 5, getBackground()),
				field.getBorder()));
			field.setEditable(false);
			add(field, BorderLayout.CENTER);
			add(controls, BorderLayout.SOUTH);
		}

		public void renewStrings() {
			parse.setText(Strings.get("parseButton"));
			generate.setText(Strings.get("generateButton"));
			clear.setText(Strings.get("clearButton"));
			parse.setToolTipText(Strings.get("parseTip"));
			generate.setToolTipText(Strings.get("generateTip"));
			clear.setToolTipText(Strings.get("clearTip"));
			field_label.setText(Strings.get("textLabel"));
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src == parse || src == field) doParse(); //albero
			if(src == generate) doGenerate(); //generazione della grammatica
			if(src == clear) //clear è clear....
			{
				field.setText("");
				clear.setEnabled(false);
				parse.setEnabled(false);
			}
		}

		private void doGenerate() {
			Generator.setCurrent_run(Generator.RUN);
			Grammar g = getGrammar(); 
			if(g == null) return;
			List sentence;
				try {
					sentence = Generator.generate(g);
					StringBuffer buf = new StringBuffer();
					StringBuffer buf2 = new StringBuffer();
					Iterator it = sentence.iterator();
					while(it.hasNext()) {
						String tempBuf = it.next().toString();
						buf.append(tempBuf);
						buf2.append(tempBuf+"/");//serve per creare l'albero di ricorsione finale.... tutti gli atomi saranno divisi da uno /
					}
					if(Generator.getCurrent_run() == Generator.RUN)
					{
						field.setText(buf.toString());
						invisible_field.setText(buf2.toString());
						clear.setEnabled(true);
						parse.setEnabled(true);
						Stack s = new Stack();
						//FRAME ALBERO FINALE
						Generator.generateSub_DrawFinestraScelta(Generator.getSc_root(), Generator.getSc_depth(), Generator.getSc_g(), Generator.getSc_log(), Generator.getSc_element_prev(), Generator.getSc_i_prev(), Generator.getSc_rules(), Generator.getSc_parent(), true,s);
					}
				} catch (GenerateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		public void doParse() {
			// first get Chomsky normal form
			Grammar g = getGrammar();
			if(g == null) return;
			StringTokenizer toks = new StringTokenizer(invisible_field.getText(),"/");
			Grammar.Atom[] text = new Grammar.Atom[toks.countTokens()];
			boolean text_ok = true;
			for(int i = 0; toks.hasMoreTokens(); i++) {
				text[i] = g.getAtom(toks.nextToken());
				if(text[i] == null) text_ok = false;
			}
			Tree t = null;
			if(text_ok) {
				if(parser == null) parser = new Parser(g);
				t = parser.parse(text);
			}
			tree_frame.setTree(t);
			field.selectAll();
			field.requestFocus();
			tree_frame.show();
		}

		private Grammar getGrammar() {
			try {
				if(grammar == null) {
					grammar = GrammarParser.parse(editor.getText());
				}
				return grammar;
			} catch(GrammarParser.ParseException e) {
				JOptionPane.showMessageDialog(EditorFrame.this,
					e.toString());
				return null;
			}
		}
	}
	
//======================= LISTENER =============================
	class MyDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent evt) {
			setChanged(true);
        }
        public void removeUpdate(DocumentEvent evt) {
			setChanged(true);
        }
        public void changedUpdate(DocumentEvent evt) {
			setChanged(true);
        }
    }
	class MyKeyListener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			switch(e.getKeyChar())
			{
				case ' ':
					try{
						int offset=editor.getLineOfOffset(editor.getCaretPosition());
					    int start=editor.getLineStartOffset(offset);
					    int end=editor.getLineEndOffset(offset);
					    String curRow = editor.getText(start, (end-start));
					    if(editor.getCaretOffsetFromLineStart() == 2 && curRow.length()<=2)
					    {
					    	editor.replaceRange("->", editor.getCaretPosition()-1, editor.getCaretPosition());
					    }}
					catch (Exception err)  {
						// TODO Auto-generated catch block
						err.printStackTrace();
					}
					break;
				case '|':

					break;
				default: break;
			}
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
//======================= END LISTENER =============================

	private class WindowCloser extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			doQuit();
		}
	}

	private FileMenu file_menu = new FileMenu();
	private EditMenu edit_menu = new EditMenu();
	private OptionsMenu options_menu = new OptionsMenu();
	private HelpMenu help_menu = new HelpMenu();

	private JLabel editor_label = new JLabel("", SwingConstants.LEFT);
	private RSyntaxTextArea editor = new RSyntaxTextArea(4, 20);
	private ControlPanel controls;

	private JFileChooser chooser = new JFileChooser(".");
	private TreeFrame tree_frame = new TreeFrame();
	private HelpFrame help_frame = new HelpFrame();
	private boolean changed = false;
	private Grammar grammar = null;
	private Parser parser = null;
	private int dim_x = 800, dim_y = 600; //dim frame principale

	public EditorFrame() {
		addWindowListener(new WindowCloser());
		this.setTitle("Grammar editor pro");

		controls = new ControlPanel();
		
		JMenuBar menubar = new JMenuBar();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Toolkit tlk = Toolkit.getDefaultToolkit();
		Dimension schermo = tlk.getScreenSize();
		dim_x = (schermo.width/DIVISIONE)*3;
		dim_y = (schermo.height/3)*2;
		setSize(dim_x, dim_y);
		setMinimumSize(new Dimension(dim_x, dim_y));
		
		setLocation((schermo.width/DIVISIONE), (schermo.height/DIVISIONE));
		icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("g.png"));
		setIconImage(icon);
		menubar.add(file_menu);
		menubar.add(edit_menu);
		menubar.add(options_menu);
		menubar.add(help_menu);
		
		setJMenuBar(menubar);

		//----------- TEXT AREA ----------------
		editor.setFont(USER_FONT);
		editor.getDocument().addDocumentListener(new MyDocumentListener());
		editor.addKeyListener(new MyKeyListener());
		editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		
	    editor.setCodeFoldingEnabled(true);
	    editor.setAntiAliasingEnabled(true);
	    
	  // Change a few things here and there.
	      SyntaxScheme scheme = editor.getSyntaxScheme();
	      scheme.getStyle(Token.ANNOTATION).background = Color.white;
	      scheme.getStyle(Token.COMMENT_DOCUMENTATION).background = Color.white;
	      scheme.getStyle(Token.COMMENT_EOL).background = Color.white;
	      scheme.getStyle(Token.COMMENT_KEYWORD).background = Color.white;
	      scheme.getStyle(Token.COMMENT_MARKUP).background = Color.white;
	      scheme.getStyle(Token.COMMENT_MULTILINE).background = Color.white;
	      scheme.getStyle(Token.ERROR_CHAR).background = Color.white;
	      scheme.getStyle(Token.ERROR_IDENTIFIER).background = Color.white;
	      scheme.getStyle(Token.ERROR_NUMBER_FORMAT).background = Color.white;
	      scheme.getStyle(Token.ERROR_STRING_DOUBLE).background = Color.white;
	      scheme.getStyle(Token.FUNCTION).background = Color.white;
	      scheme.getStyle(Token.IDENTIFIER).background = Color.white;
	      scheme.getStyle(Token.LITERAL_BACKQUOTE).background = Color.white;
	      scheme.getStyle(Token.LITERAL_BOOLEAN).background = Color.white;
	      scheme.getStyle(Token.LITERAL_CHAR).background = Color.white;
	      scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).background = Color.white;
	      scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).background = Color.white;
	      scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).background = Color.white;
	      scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).background = Color.white;
	      scheme.getStyle(Token.MARKUP_CDATA).background = Color.white;
	      scheme.getStyle(Token.MARKUP_PROCESSING_INSTRUCTION).background = Color.white;
	      scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).background = Color.white;
	      scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).background = Color.white;
	      scheme.getStyle(Token.MARKUP_TAG_DELIMITER).background = Color.red;
	      scheme.getStyle(Token.MARKUP_TAG_NAME).background = Color.white;
	      scheme.getStyle(Token.PREPROCESSOR).background = Color.white;
	      scheme.getStyle(Token.REGEX).background = Color.white;
	      scheme.getStyle(Token.RESERVED_WORD).background = Color.white;
	      scheme.getStyle(Token.RESERVED_WORD_2).background = Color.white;
	      scheme.getStyle(Token.SEPARATOR).background = Color.white;
	      scheme.getStyle(Token.VARIABLE).background = Color.white;
	      scheme.getStyle(Token.WHITESPACE).background = Color.white;
	      scheme.getStyle(Token.IDENTIFIER).background = Color.white;
	      
	      scheme.getStyle(Token.ANNOTATION).foreground = Color.black;
	      scheme.getStyle(Token.COMMENT_DOCUMENTATION).foreground = Color.black;
	      scheme.getStyle(Token.COMMENT_EOL).foreground = Color.black;
	      scheme.getStyle(Token.COMMENT_KEYWORD).foreground = Color.black;
	      scheme.getStyle(Token.COMMENT_MARKUP).foreground = Color.black;
	      scheme.getStyle(Token.COMMENT_MULTILINE).foreground = Color.black;
	      scheme.getStyle(Token.ERROR_CHAR).foreground = Color.black;
	      scheme.getStyle(Token.ERROR_IDENTIFIER).foreground = Color.black;
	      scheme.getStyle(Token.ERROR_NUMBER_FORMAT).foreground = Color.black;
	      scheme.getStyle(Token.ERROR_STRING_DOUBLE).foreground = Color.black;
	      scheme.getStyle(Token.FUNCTION).foreground = Color.black;
	      scheme.getStyle(Token.IDENTIFIER).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_BACKQUOTE).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_CHAR).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).foreground = Color.black;
	      scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = Color.black;
	      scheme.getStyle(Token.MARKUP_CDATA).foreground = Color.black;
	      scheme.getStyle(Token.MARKUP_PROCESSING_INSTRUCTION).foreground = Color.black;
	      scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).foreground = Color.black;
	      scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).foreground = Color.black;
	      scheme.getStyle(Token.MARKUP_TAG_DELIMITER).foreground = Color.black;
	      scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = Color.black;
	      scheme.getStyle(Token.PREPROCESSOR).foreground = Color.black;
	      scheme.getStyle(Token.REGEX).foreground = Color.black;
	      scheme.getStyle(Token.RESERVED_WORD).foreground = Color.black;
	      scheme.getStyle(Token.RESERVED_WORD_2).foreground = Color.black;
	      scheme.getStyle(Token.SEPARATOR).foreground = Color.black;
	      scheme.getStyle(Token.VARIABLE).foreground = Color.black;
	      scheme.getStyle(Token.WHITESPACE).foreground = Color.black;
	      scheme.getStyle(Token.IDENTIFIER).foreground = Color.black;
	      
	      scheme.getStyle(Token.DATA_TYPE).foreground = Color.black;
	      scheme.getStyle(Token.OPERATOR).font = new Font("monospaced", Font.BOLD, 16);
	      scheme.getStyle(Token.OPERATOR).foreground = Color.blue;
	     

	      editor.revalidate();
	      changed = false;
	      JPanel centro_panel = new JPanel();
	      centro_panel.setLayout(new BorderLayout());
	      RTextScrollPane scroll = new RTextScrollPane(editor);
	      scroll.setFoldIndicatorEnabled(true);
	      scroll.setBorder(BorderFactory.createCompoundBorder(
		  BorderFactory.createMatteBorder(0, 5, 5, 5, getBackground()),
		  scroll.getBorder()));
	      editor_label.setBorder(BorderFactory.createMatteBorder(5, 5,
			0, 5, getBackground()));
		centro_panel.add(scroll, BorderLayout.CENTER);
		//-------------------------------------------------------
		
		//--------- FRAME PRINCIPALE ---------------------
		Container contents = getContentPane();
		centro_panel.add(editor_label, BorderLayout.NORTH);
		contents.add(centro_panel, BorderLayout.CENTER);
		contents.add(controls, BorderLayout.SOUTH);
		BorderLayout layout = new BorderLayout();
		LocaleManager.addListener(this);
		localeChanged();
	}
	
	public static Image getIcona()
	{return icon;}
	
	public static void setIcona(String s)
	{icon = Toolkit.getDefaultToolkit().getImage(s);}

	public void localeChanged() {
		file_menu.renewStrings();
		edit_menu.renewStrings();
		options_menu.renewStrings();
		help_menu.renewStrings();
		controls.renewStrings();
		tree_frame.renewStrings();
		help_frame.renewStrings();
		editor_label.setText(Strings.get("grammarLabel"));
		JFileChooser newch = new JFileChooser();
		java.io.File f = chooser.getCurrentDirectory();
		if(f != null) newch.setCurrentDirectory(f);
		f = chooser.getSelectedFile();
		if(f != null) newch.setSelectedFile(f);
		chooser = newch;
	}

	private void setChanged(boolean value) {
		changed = value;
		grammar = null;
		parser = null;
	}

	private boolean doSave() {
		int val = chooser.showSaveDialog(this);
		if(val != JFileChooser.APPROVE_OPTION) return false;

		try {
			java.io.PrintWriter writer = new java.io.PrintWriter(
				new java.io.FileWriter(chooser.getSelectedFile()));
			writer.print(editor.getText());
			writer.close();
		} catch(java.io.IOException e) {
			JOptionPane.showMessageDialog(this,
				Strings.get("saveError") + e.getMessage());
			return false;
		}
		setChanged(false);
		return true;
	}

	private void doQuit() {
		if(!confirmLoss(Strings.get("confirmQuitMsg"))) return;
		else
			System.exit(0);
	}

	private boolean confirmLoss(String msg) {
		if(!changed) return true;
		int val = JOptionPane.showConfirmDialog(this, msg);
		if(val == JOptionPane.CANCEL_OPTION) return false;
		if(val == JOptionPane.YES_OPTION && !doSave()) return false;
		return true;
	}
}
