package edu.csbsju.socs.grammar;

public class state {
	private Grammar.Element[] elements;
	private Grammar.Element leftSimbol;
	private int indice;
	private String strElements;
	
	public state(Grammar.Element[] e, int i, Grammar.Element left)
	{
		setElements(e);
		setIndice(i);
		this.setLeftSimbol(left);
	}

	public Grammar.Element[] getElements() {
		return elements;
	}

	public void setElements(Grammar.Element[] elements) {
		this.elements = elements;
	}

	public int getIndice() {
		return indice;
	}

	public void setIndice(int indice) {
		this.indice = indice;
	}
	
	//html
	public String toString()
	{
		String temp = "";
		if(leftSimbol instanceof Grammar.Atom)
			 temp = "<html>"+leftSimbol.getName();
		else
			 temp = "<html><span style='color:red'>"+leftSimbol.getName()+"</span>";
		if (elements != null && !(leftSimbol instanceof Grammar.Atom)){
			temp+="<span style='color:blue'>-></span><span style='color:gray'>";
		for(int i = 0; i < elements.length; i++) {
			Grammar.Element elemento = elements[i];

			if(i==this.indice)
			{
				this.setStrElements(elemento.getName());
				temp+=elemento.getName();
			}
			else
			{
				this.setStrElements(elemento.getName());
				temp+= elemento.getName();
			}
		}}
		return temp+"</span>"+"</html>";
	}
	
	//string
	public String toString2()
	{
		String temp = "";
		if (elements != null){
			temp+=" -> ";
		for(int i = 0; i < elements.length; i++) {
			Grammar.Element elemento = elements[i];
			if(i==this.indice)
			{
				temp+=GrammarParser.purificaStr(elemento.getName());
			}
			else
			{
				temp+= GrammarParser.purificaStr(elemento.getName());
			}
		}}
		return temp;
	}
	
	public String toHtml()
	{
		String temp = "";
		for(int i = 0; i < elements.length; i++) {
			Grammar.Element elemento = elements[i];
			if(i==this.indice)
			{
				temp+="<span style='color:red'>("+GrammarParser.purificaStr(elemento.getName())+") </span>";
			}
			else
			{
				temp+= elemento.getName();
			}
		}
		return temp;
	}

	public Grammar.Element getLeftSimbol() {
		return leftSimbol;
	}

	public void setLeftSimbol(Grammar.Element leftSimbol) {
		this.leftSimbol = leftSimbol;
	}

	public String getStrElements() {
		return strElements;
	}

	public void setStrElements(String strElements) {
		this.strElements = strElements;
	}
}
