package edu.csbsju.socs.grammar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.sun.java.swing.plaf.motif.MotifBorders.BevelBorder;
import com.sun.java_cup.internal.runtime.Symbol;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.LocalAttribute;

import edu.csbsju.socs.grammar.EditorFrame.ControlPanel;
import edu.csbsju.socs.grammar.Generator.GenerateException;
import edu.csbsju.socs.grammar.Grammar.Atom;

public class frameScelta extends JDialog implements ActionListener, MouseListener
{
	private String sceltaCorrente;
	private JPanel pannello, southPanel, leftPanel, rightPanel, panelTempString;
	private JPanel panelMessage, choicePanel,cur_left_symbol;
	private JScrollPane leftScroll, rightScroll;
	private static ButtonGroup gruppo;
	private JButton buttonOK, buttonAnnulla;
	private JLabel message, message2,label_cur_left_sym;
	private JTextArea message1;
	private JLabel message3;
	private String string_cru_leftSymbol;
	private Color sfondo, selez;
	private statoRicorsivo curVar;
	private boolean isEnd = false;
	private JTree tab;
	private ArrayList<labelTreeScelta> indiciFoglie;
	private int c;
	@SuppressWarnings("deprecation")
	private class WindowCloser extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			doQuit();
		}
	}
	public frameScelta(String t, int w, int h, boolean end)
	{
		super(new JFrame(),true);
		this.isEnd = end;
		this.c = 0;
		indiciFoglie = new ArrayList<labelTreeScelta>();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Toolkit tlk = Toolkit.getDefaultToolkit();
		Dimension schermo = tlk.getScreenSize();
		w = (schermo.width/(EditorFrame.DIVISIONE+1))*2;
		h = (schermo.height/3)*2;
		setSize(w, h);
		setMinimumSize(new Dimension(w, h));
		setLocation((schermo.width/EditorFrame.DIVISIONE)+((schermo.width/EditorFrame.DIVISIONE)*3)-80, (schermo.height/EditorFrame.DIVISIONE)+60);
		if(!isEnd)
			t= "Grammar editor pro | scegli una produzione";
		else
			t = "Grammar editor pro | ";
		setTitle(t);
		setLayout(new BorderLayout());
		setIconImage(EditorFrame.getIcona());
		addWindowListener(new WindowCloser());
		this.pack();
		
		sfondo = Color.white;
		selez = Color.white;
		
		sceltaCorrente = "";
		BorderLayout gl1 = new BorderLayout();
		gl1.setVgap(10);
		pannello = new JPanel(gl1);
		southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		leftPanel = new JPanel(new BorderLayout(0, 6));
		rightPanel = new JPanel(new BorderLayout(0,6));
		GridLayout gl = new GridLayout(3, 1);
		choicePanel = new JPanel(gl);
		BorderLayout bl = new BorderLayout();
		bl.setVgap(10);
		cur_left_symbol = new JPanel(bl);
		
		//sud pannello----------------
		if(isEnd)
		{	
			buttonAnnulla = new JButton("Chiudi");
			buttonAnnulla.setToolTipText("Chiudi la finestra");
			buttonAnnulla.addActionListener(this);
			southPanel.add(buttonAnnulla);
		}
		else
		{
			buttonOK = new JButton("Next >>");
			buttonOK.addActionListener(this);
			southPanel.add(buttonOK);
		}
		//-------------------------
		
		//left pannello-------------------
		
		
		message1 = new JTextArea();
		message1.setColumns(20);
		message1.setLineWrap(true);
		message1.setFont(new Font("monospaced", Font.PLAIN, 16));
		message1.invalidate();
		message1.setEditable(false);
		leftScroll = new JScrollPane(message1);
		message3 = new JLabel();
		message3.setOpaque(true);
		message3.setBackground(sfondo);
		message1.setFont(new Font("monospaced", Font.PLAIN, 16));
		leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panelMessage = new JPanel(new BorderLayout());
		panelMessage.setBackground(sfondo);
		message2 = new JLabel("  Tree production");
		message2.setFont(new Font("monospaced", Font.BOLD, 14));
		message2.setForeground(Color.black);
		leftPanel.add(panelMessage,BorderLayout.CENTER);
		leftPanel.add(message2,BorderLayout.NORTH);
		leftPanel.setMinimumSize(new Dimension(w, h/4*3));
		pannello.add(leftPanel,BorderLayout.CENTER);
		//---------------------------------
		
		//pannello di scelta------------
			
			rightScroll = new JScrollPane();
			rightScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			choicePanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());
			choicePanel.setBackground(selez);
			
			label_cur_left_sym = new JLabel();
			cur_left_symbol.add(label_cur_left_sym, BorderLayout.CENTER);
			cur_left_symbol.setBorder(BorderFactory.createLoweredSoftBevelBorder());
			String title_choisePanel = "";
			if(!isEnd)
			{
				rightPanel.add(choicePanel,BorderLayout.CENTER);
				rightPanel.add(cur_left_symbol, BorderLayout.WEST);
				rightPanel.setMaximumSize(new Dimension(100, 50));
				title_choisePanel = "Scegli una produzione";
			}
			else
			{
				title_choisePanel = "";
			}
			message = new JLabel(title_choisePanel);
			message.setFont(new Font("monospaced", Font.BOLD, 14));
			message.setForeground(Color.black);
			rightPanel.add(message,BorderLayout.NORTH);
			pannello.add(rightPanel,BorderLayout.SOUTH);
		//-----------------------------
		
		gruppo = new ButtonGroup();
		getContentPane().add(pannello, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		this.curVar = new statoRicorsivo(this);
	}
	public void addRadio(String n, int ind)
	{
		radioScelta rb = new radioScelta(n,ind);
		if (ind==0)
		{
			rb.setSelected(true);
			Generator.setScelta(ind);
		}
		rb.addActionListener(new listenerFrameScelta());
		gruppo.add(rb);
		rb.setBackground(Color.white);
		choicePanel.add(rb);
		choicePanel.revalidate();
		choicePanel.repaint();
		this.transferFocus();
		this.revalidate();
		this.repaint();
		this.validate();
		this.pack();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		 if(e.getSource() instanceof JButton)
		{
			JButton b = (JButton)e.getSource();
			if(b.equals(buttonOK))
			{
				this.dispose();
			}
			else if(b.equals(buttonAnnulla))
			{
				this.dispose();
			}
		}
	}
	
	public void doQuit()
	{
		Generator.setCurrent_run(Generator.NOT_RUN);
		this.dispose();
	}
	
	public void mostra()
	{
		this.show();
	}
	
	public void setMessage(String s)
	{
		message3.setText("<html><body style='text-align:center' >"+s+"</bodi></html>");
	}
	
	public DefaultMutableTreeNode creaAlbero(DefaultMutableTreeNode parent, nodo ricorsione, Iterator<nodo> fratelli)
	{
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(ricorsione.toString());
		DefaultMutableTreeNode cur = root;
		if(parent == null)
			parent = root;
		else
			parent.add(root);
		
		if(!ricorsione.getFigli().isEmpty())
		{
			Iterator<nodo> it = ricorsione.getFigli().iterator();
			while(it.hasNext())
			{
				nodo tmp = it.next();
				this.string_cru_leftSymbol = tmp.getStato().getLeftSimbol().getName();
				c = c+1;
				creaAlbero(root, tmp, ricorsione.getFigli().iterator());
			}
		}
		else
		{
			if((ricorsione.getStato().getLeftSimbol() instanceof edu.csbsju.socs.grammar.Grammar.Symbol) || (ricorsione.getStato().getLeftSimbol() instanceof Atom)) // se è un simbolo non terminale va inserito nella stringa temporanea 
			{
				
				String tempS = ricorsione.getStato().getLeftSimbol().getName();
				indiciFoglie.add(new labelTreeScelta(tempS, c));
			}
		}
		if(this.string_cru_leftSymbol == null)
			this.string_cru_leftSymbol = ricorsione.getStato().getLeftSimbol().getName();
		this.label_cur_left_sym.setText("  "+string_cru_leftSymbol+"->");
		
		return cur;
	}
	
	public void setMessage(Collection r, nodo ricorsione, ArrayList log, Stack st)
	{
		
		panelMessage.removeAll();
		//------------------
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		tab = new JTree(root);
		tab.setBackground(Color.white);
		DefaultMutableTreeNode cur = creaAlbero(root, ricorsione, null);
		int j=0;
		for (int i = 0; i < tab.getRowCount(); i++) {
	         tab.expandRow(i);
	        j=i; 
		}
		tab.setSelectionPath(tab.getPathForRow(j));
		tab.setRootVisible(true);
		
		tab.putClientProperty("JTree.lineStyle", "Angled");
		
		JScrollPane scrollPane = new JScrollPane(tab);
		tab.setFont(new Font("Arial", Font.PLAIN, 16));
		panelMessage.setLayout(new BorderLayout());
		scrollPane.revalidate();
		scrollPane.repaint();
		panelMessage.add(scrollPane, BorderLayout.CENTER);
		//---- parte riguardante la stampa della parte a sinistra della root -------
		FlowLayout flpst = new FlowLayout();
		flpst.setHgap(0);
		JPanel pannelloStringaTemporanea = new JPanel(flpst);
		pannelloStringaTemporanea.setBackground(Color.white);
		Iterator<labelTreeScelta> lts = indiciFoglie.iterator();
		while(lts.hasNext())
		{
			labelTreeScelta tempLabel = lts.next();
			tempLabel.addMouseListener(this);
			pannelloStringaTemporanea.add(tempLabel);
			
		}
		//----------------------------------------------------------------------------
		//----- parte rigurardante la stampa della stringa corrente dopo la root corrente ------
		Object[] stt = st.toArray();
		String convertStack ="";
		for(int i = stt.length-1; i>=0; i--)
			convertStack += stt[i].toString();
		JLabel stackStringaRimanente = new JLabel(convertStack);
		stackStringaRimanente.setFont( new Font("arial", Font.PLAIN, 21));
		stackStringaRimanente.setForeground(Color.GRAY);
		pannelloStringaTemporanea.add(stackStringaRimanente);
		//---------------------------------------------------------------------------------------
		String titleStringTemp = "    Stringa temporanea";
		if(isEnd)
			titleStringTemp = "    Stringa";
		JLabel label_titleStringTemp = new JLabel(titleStringTemp);
		label_titleStringTemp.setFont(new Font("arial",Font.PLAIN,16));
		
		JScrollPane jsp2 = new JScrollPane(pannelloStringaTemporanea);
		jsp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		jsp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp2.getHorizontalScrollBar().setPreferredSize(new Dimension(1,10));
		BorderLayout bl2 = new BorderLayout();
		bl2.setVgap(0);
		bl2.setHgap(10);
		panelTempString = new JPanel(bl2);
		panelTempString.setBorder(BorderFactory.createRaisedSoftBevelBorder());
		if(!indiciFoglie.isEmpty()){
			panelTempString.add(jsp2, BorderLayout.CENTER);
			panelTempString.add(new JPanel().add(label_titleStringTemp), BorderLayout.NORTH);
		}
		panelTempString.add(new JPanel(), BorderLayout.WEST);
		panelTempString.add(new JPanel(), BorderLayout.EAST);
		panelMessage.add(panelTempString,BorderLayout.SOUTH);
			panelMessage.revalidate();
		panelMessage.repaint();
	}
	
	public void resetPanels()
	{
		panelMessage.removeAll();
		choicePanel.removeAll();
		this.curVar.reset();
	}
	
	public statoRicorsivo getCurVar()
	{return this.curVar;}
	
	public void setCurVar(Grammar.Symbol r, int d, Grammar grammatica, ArrayList l, Grammar.Element[] e_p, int i_p, Collection ru)
	{
		this.curVar.set(r, d, grammatica, l, e_p, i_p, ru);
	}
	
	public void setCurVar(statoRicorsivo sr)
	{
		this.curVar = sr;
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof labelTreeScelta)
		{
			labelTreeScelta temp = (labelTreeScelta)e.getSource();
			tab.setSelectionInterval(temp.getId()+1, temp.getId()+1);
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof labelTreeScelta)
		{
			labelTreeScelta temp = (labelTreeScelta)e.getSource();
			temp.setForeground(Color.red);
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof labelTreeScelta)
		{
			labelTreeScelta temp = (labelTreeScelta)e.getSource();
			temp.setForeground(Color.black);
		}
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public JTree getTab() {
		return tab;
	}
	public void setTab(JTree tab) {
		this.tab = tab;
	}
}
