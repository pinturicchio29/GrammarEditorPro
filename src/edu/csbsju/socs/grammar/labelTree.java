package edu.csbsju.socs.grammar;

public class labelTree 
{
	private TreePanelToggleButton toggle;
	private String testo;
	private int x, y;
	
	public labelTree(String s, int id, int x, int y)
	{
		this.testo = s;
		toggle = new TreePanelToggleButton(s, id);
		this.x = x;
		this.y = y;
	}
	
	public labelTree(String s, int x, int y)
	{
		this.testo = s;
		toggle = null;
		this.x = x;
		this.y = y;
	}

	public TreePanelToggleButton getToggle() {
		return toggle;
	}

	public void setToggle(TreePanelToggleButton toggle) {
		this.toggle = toggle;
	}

	public String getTesto() {
		return testo;
	}

	public void setTesto(String testo) {
		this.testo = testo;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
