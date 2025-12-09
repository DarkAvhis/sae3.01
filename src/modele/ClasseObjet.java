package modele;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Représente une classe UML analysée à partir d'un fichier Java.
 * Contient son nom, sa liste d'attributs et sa liste de méthodes.
 */
public class ClasseObjet 
{
	/*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
	private String nom;
	private ArrayList<AttributObjet> attributs;
	private ArrayList<MethodeObjet>  methodes;

	/*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
	public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom) 
	{
		this.attributs = attributs;
		this.methodes  = methodes;
		this.nom       = nom;
	}

	/*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
	public String                   getNom      () {	return nom       ;	}
	public ArrayList<AttributObjet> getattributs() {	return attributs ;	}
	public ArrayList<MethodeObjet>  getMethodes () {	return methodes  ;	}

	/*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
	public void setNom      (String nom                        ) {	this.nom       = nom       ;	}
	public void setattributs(ArrayList<AttributObjet> attributs) {	this.attributs = attributs ;	}
	public void setmethodes (ArrayList<MethodeObjet>  methodes ) {	this.methodes  = methodes  ;	}
	

	/*-------------------------------------- */
	/* Methode autre                         */
	/*-------------------------------------- */
	public char changementVisibilite( String visibilite ) 
	{
		switch (visibilite) 
		{
			case "private"  : return '-';
			case "public"   : return '+';
			case "protected": return '#';
			default:          return '~';
		}
	}

	public String affichageParametre(HashMap<String, String> parametre)
	{
		String sRet = "";

		if (parametre != null && !parametre.isEmpty())
		{
			sRet += "(";
			for (String key : parametre.keySet())
			{
				sRet += key + ": " + parametre.get(key) + ", ";
			}
			sRet = sRet.substring(0, sRet.length() - 2);
			sRet += ")";
		}
		else
		{
			sRet = "()";
		}
		return sRet;
	}

	public String retourType(String type) 
	{
		if (type == null) return " "; 

		if (type.equals("public") || type.equals("void")) 
		{
			return " "; 
		}
		return " : " + type;
	}

	/*-------------------------------------- */
	/* toString                              */
	/*-------------------------------------- */
	@Override
	public String toString() 
	{
		String sRet = "";

		sRet += "-------------------------------------------------------------------------------------------\n";
		sRet += String.format( "%50s" ,  this.nom ) +              "\n";
		sRet += "-------------------------------------------------------------------------------------------\n";

		for (AttributObjet att : attributs) 
		{
			String staticFlag = att.getStatique() ? " {static}" : "";
			sRet +=  String.format( "%-2c" , changementVisibilite(att.getVisibilite()) )   + 
					 String.format("%-15s" , att.getNom() )  + 
					 String.format("%-15s" , retourType( att.getType() ))  + 
					 String.format("%-10s" , staticFlag ) + "\n" ; 
		}

		sRet += "-------------------------------------------------------------------------------------------\n";

		for( MethodeObjet met : methodes )
		{
			String staticFlag = met.estStatique() ? "{static} " : "";

			sRet += String.format( "%-2c",    changementVisibilite(met.getVisibilite())) + staticFlag +
					String.format( "%-25s" ,  met.getNom()) + 
					String.format( "%-35s" ,  affichageParametre(met.getParametres()))  + 
					String.format( "%-15s",   retourType(met.getRetourType()) ) + "\n" ; 
		}
		sRet += "-------------------------------------------------------------------------------------------\n";
		
		return sRet;
	}
}
