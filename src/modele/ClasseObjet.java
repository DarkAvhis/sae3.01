package modele;

import java.util.ArrayList;
import java.util.HashMap;

public class ClasseObjet {

	private String nom;
	private ArrayList<AttributObjet> attributs;
	private ArrayList<MethodeObjet> methodes;

	public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom) {
		this.attributs = attributs;
		this.methodes = methodes;
		this.nom = nom;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public ArrayList<AttributObjet> getattributs() {
		return attributs;
	}

	public void setattributs(ArrayList<AttributObjet> attributs) {
		this.attributs = attributs;
	}

	public ArrayList<MethodeObjet> getMethodes() {
		return methodes;
	}

	public void setmethodes(ArrayList<MethodeObjet> methodes) {
		this.methodes = methodes;
	}

	public String pran( HashMap<String , String > parametre )
	{
		String sRet = "" ; 

		if( parametre != null)
		{
			sRet += "(" ; 
			for ( String key : parametre.keySet())
			{
				sRet += key + parametre.get(key) ; 
			}
			sRet += ")" ; 
		}
		else 
		{
			sRet = "()" ; 
		}
		return sRet ; 
	}
	
	@Override
	public String toString() 
	{
		String sRet = "";

		sRet += "------------------------------------------------\n";
		sRet += String.format( "%20s" ,  this.nom ) +    "\n";
		sRet += "------------------------------------------------\n";

		for (AttributObjet att : attributs) {
			sRet += att.getVisibilite().getLibelle() + " " + att.getNom() + " " + ":" + att.getType() + "\n" ; 
		}

		sRet += "------------------------------------------------\n";

		for( MethodeObjet met : methodes )
		{
			sRet += String.format( "%-2s", met.getVisibilite().getLibelle()) +
					String.format( "%-10s" ,  met.getNom()) + 
					String.format( "%-20s" , pran(met.getParametres())) + 
					String.format( "%-10s",met.getRetourType()) + "\n" ; 
		}
		sRet += "------------------------------------------------\n";

		return sRet ; 
	}
	
	
}
