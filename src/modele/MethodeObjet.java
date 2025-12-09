package modele;

import java.util.HashMap;



public class MethodeObjet 
{

	private String nom;
	private String visibilite;
	private String retourType;
	private HashMap<String, String> parametres; // Nom et Type

    public MethodeObjet(String nom, HashMap<String, String> parametres, String retourType, String visibilite) 
    {
        this.nom = nom;
        this.parametres = parametres;
        this.retourType = retourType;
        this.visibilite = visibilite;
    }

    public MethodeObjet( String nom, HashMap<String, String> parametres,String visibilite )
    {
        this.nom = nom;
        this.parametres = parametres;
        this.visibilite = visibilite;
    }

    public String getNom() {return nom;}

    public void setNom(String nom) {this.nom = nom;}

    public String getVisibilite() {return visibilite;}

    public void setVisibilite(String visibilite) {this.visibilite = visibilite;}

    public String getRetourType() {return retourType;}

    public void setRetourType(String retourType) {this.retourType = retourType;}

    public HashMap<String, String> getParametres() 
    {
        return parametres;
    }

    public void setParametres(HashMap<String, String> parametres) {this.parametres = parametres;}

}
