package modele;

import java.util.HashMap;

public class MethodeObjet 
{

	private String nom;
	private String visibilite;
	private String retourType;
	private HashMap<String, String> parametres; 
	private boolean statique;

	public MethodeObjet(String nom, HashMap<String, String> parametres, String retourType, String visibilite, boolean estStatique) 
	{
		this.nom = nom;
		this.parametres = parametres;
		this.retourType = retourType;
		this.visibilite = visibilite;
		this.statique = estStatique;
	}

	public MethodeObjet( String nom, HashMap<String, String> parametres,String visibilite, boolean estStatique )
	{
		this.nom = nom;
		this.parametres = parametres;
		this.visibilite = visibilite;
		this.statique = estStatique;
	}

	public MethodeObjet(String nom, HashMap<String, String> parametres, String retourType, String visibilite) 
	{
		this(nom, parametres, retourType, visibilite, false);
	}

	public MethodeObjet( String nom, HashMap<String, String> parametres,String visibilite )
	{
		this(nom, parametres, null, visibilite, false);
	}


	public String                  getNom       () {	return nom        ; }
	public String                  getVisibilite() {	return visibilite ; }
	public String                  getRetourType() {	return retourType ; }
	public HashMap<String, String> getParametres() {    return parametres ; }
	public boolean                 estStatique  () {    return statique   ; }


	public void setNom       (String                  nom       ) {  this.nom        = nom        ;  }
	public void setVisibilite(String                  visibilite) {  this.visibilite = visibilite ;  }
	public void setRetourType(String                  retourType) {  this.retourType = retourType ;  }
	public void setParametres(HashMap<String, String> parametres) {  this.parametres = parametres ;  }
	public void setStatique  (boolean                 statique  ) {  this.statique   = statique   ;  }
}