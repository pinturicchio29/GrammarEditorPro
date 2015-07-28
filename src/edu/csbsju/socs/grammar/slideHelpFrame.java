package edu.csbsju.socs.grammar;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument;

import edu.csbsju.socs.util.HelpFrame.NotFoundException;


public class slideHelpFrame extends JFrame implements ChangeListener, ActionListener
{
	private int dim_x = 0, dim_y = 0, cur_pag = 0,tot_pag = 10;
	private JPanel nord, centro, sud, est, ovest;
	private JButton next,prev;
	private JCheckBox mostra;
	private JEditorPane	editor;
	private JScrollPane	scroll_pane;
	private String percorso = "/edu/csbsju/socs/grammar/doc/";
	
	public slideHelpFrame ()
	{
		this.setTitle("Benvenuti a Grammar editor pro");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Toolkit tlk = Toolkit.getDefaultToolkit();
		Image icon = tlk.getImage(getClass().getResource("g.png"));
		setIconImage(icon);
		Dimension schermo = tlk.getScreenSize();
		dim_x = (schermo.width/EditorFrame.DIVISIONE)*3;
		dim_y = (schermo.height/3)*2;
		setSize(dim_x, dim_y);
		setLocation((schermo.width/EditorFrame.DIVISIONE-50), (schermo.height/EditorFrame.DIVISIONE-50));
		
		this.setResizable(false);
		
		//------ pannello superiore ----------------
		BorderLayout blps = new BorderLayout();
		blps.setVgap(10);
		nord = new JPanel(blps);
		JPanel bho = new JPanel();
		bho.setPreferredSize(new Dimension(10, 10));
		JLabel titolo = new JLabel("        Come utilizzare Grammar editor pro");
		titolo.setFont(new Font("Arial", Font.ITALIC, 22));
		
		nord.add(bho, BorderLayout.NORTH);
		nord.add(titolo, BorderLayout.CENTER);
		//------------------------------------------
		
		//------ pannello centrale -----------------
		centro = new JPanel();
		centro.setLayout(new BorderLayout());
		
		editor = new JEditorPane();
		editor.setEditable(false);
		editor.setContentType("text/html");
		editor.setPreferredSize(new Dimension(dim_x, dim_y));
		
		try {
			URL url2 = this.getUrlRelativo(cur_pag+".html");
			this.carica(url2);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		scroll_pane = new JScrollPane(editor);
		scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll_pane.setMinimumSize(new Dimension(10, 10));
		centro.add(scroll_pane,BorderLayout.CENTER);
		
		//------------------------------------------
		
		//------ pannello inferiore -----------------
		sud = new JPanel();
		sud.setPreferredSize(new Dimension(this.getWidth(),60));
		
		next = new JButton("Next >>");
		next.addActionListener(this);
		
		prev = new JButton("Prev <<");
		prev.addActionListener(this);
		prev.setEnabled(false);
		
		mostra = new JCheckBox("Mostra all'avvio");
		mostra.setSelected(this.getFlagMostra());
		mostra.addChangeListener(this);
		
		sud.add(mostra);
		sud.add(prev);
		sud.add(next);
		//------------------------------------------
		
		//----------- pannelli laterali -----------
		est = new JPanel();
		est.setPreferredSize(new Dimension(10, 10));
		ovest = new JPanel();
		ovest.setPreferredSize(new Dimension(10, 10));
		//-----------------------------------------
		
		Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10,10));
		contents.add(nord, BorderLayout.NORTH);
		contents.add(centro, BorderLayout.CENTER);
		contents.add(sud,BorderLayout.SOUTH);
		contents.add(est,BorderLayout.EAST);
		contents.add(ovest,BorderLayout.WEST);
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
	
	protected void carica(URL url)
	{
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
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	public URL getUrlRelativo(String nomeFile) throws MalformedURLException
	{
		URL url = getClass().getResource(percorso+nomeFile);
		
		return url;
	}
	
	public boolean getFlagMostra()
	{
		 try {
				FileReader fr=new FileReader(Main.helpFlag);

				 char flagHelp=(char) fr.read();
				 if(flagHelp == '1')
					 return true;
				 else
					 return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof JCheckBox)
		{
			JCheckBox temp = (JCheckBox)e.getSource();
		    FileWriter w;
		    try {
				w=new FileWriter(Main.helpFlag);
	
				if(temp.isSelected())
				{
					w.write('1');
				}
				else
				{
					w.write('0');
				}
			    w.flush();
		    } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof JButton)
		{
			JButton temp = (JButton)e.getSource();
			if(temp == next && cur_pag < tot_pag-1)
			{
				try {
					prev.setEnabled(true);
					cur_pag++;
					URL url2 = this.getUrlRelativo(cur_pag+".html");
					this.carica(url2);
					if(cur_pag == tot_pag-1)
						next.setEnabled(false);
					
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(temp == prev && cur_pag > 0)
				{
				try {
					next.setEnabled(true);
					cur_pag--;
					URL url2 = this.getUrlRelativo(cur_pag+".html");
					this.carica(url2);
					if(cur_pag == 0)
						prev.setEnabled(false);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
		}
	}
}
