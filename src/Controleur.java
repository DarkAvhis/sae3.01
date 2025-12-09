import modele.AnalyseurUML;
import vue.FenetrePrincipale;

public class Controleur 
{
	private FenetrePrincipale fenetre ; 
	private AnalyseurUML      analyse;

	public Controleur()
	{
		this.fenetre = new FenetrePrincipale(); 
		this.analyse = new AnalyseurUML();
		this.fenetre.setVisible(true);
	}

	public static void main(String[] args) 
	{
		new Controleur(); 
	}
}
