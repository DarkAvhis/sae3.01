package modele;

import java.util.ArrayList;
import java.util.HashMap;

public class ClasseObjet 
{

	private String nom;
	private ArrayList<AttributObjet> attributs;
	private ArrayList<MethodeObjet> methodes;

	public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom) 
	{
		this.attributs = attributs;
		this.methodes = methodes;
		this.nom = nom;
	}

	public String                   getNom      () {	return nom       ;	}
	public ArrayList<AttributObjet> getattributs() {	return attributs ;	}
	public ArrayList<MethodeObjet>  getMethodes () {	return methodes  ;	}

	public void setNom      (String nom                        ) {	this.nom       = nom       ;	}
	public void setattributs(ArrayList<AttributObjet> attributs) {	this.attributs = attributs ;	}
	public void setmethodes (ArrayList<MethodeObjet>  methodes ) {	this.methodes  = methodes  ;	}
	

	public char changementVisibilite( String visibilite ) 
	{
		switch (visibilite) 
		{
			case "private":   return '-';
			case "public":    return '+';
			case "protected": return '#';
			default:          return '~';
		}
	}

	public String changementPortee(String portee)
	{
		switch (portee)
		{
			case "static" : return "static";
			default: return " ";
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
		if (type == null) 
		{
			return " "; 
		}
		if (type.equals("public") || type.equals("void")) 
		{
			return " "; 
		}
		return " : " + type;
	}

	
	@Override
	public String toString() 
	{
		String sRet = "";

		sRet += "-------------------------------------------------------------------------------------------\n";
		sRet += String.format( "%50s" ,  this.nom ) +              "\n";
		sRet += "-------------------------------------------------------------------------------------------\n";

		// AFFICHAGE ATTRIBUTS
		for (AttributObjet att : attributs) 
		{
			String staticFlag = att.isStatique() ? " {static}" : "";
			sRet += changementVisibilite(att.getVisibilite())  + 
					" " + att.getNom() + " : " + att.getType() + 
					staticFlag + "\n" ; 
		}

		sRet += "-------------------------------------------------------------------------------------------\n";

		// AFFICHAGE METHODES
		for( MethodeObjet met : methodes )
		{
			String staticFlag = met.isStatique() ? "{static} " : "";

			sRet += String.format( "%-2c",    changementVisibilite(met.getVisibilite())) +
					staticFlag +
					String.format( "%-25s" ,  met.getNom()) + 
					String.format( "%-35s" ,  affichageParametre(met.getParametres()))  + 
					String.format( "%-15s",   retourType(met.getRetourType()) ) + "\n" ; 
		}
		sRet += "-------------------------------------------------------------------------------------------\n";

		return sRet ; 
	}
	
}
