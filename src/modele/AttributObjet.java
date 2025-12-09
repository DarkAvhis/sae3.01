package modele;

import java.util.ArrayList;

public class AttributObjet
{
	
	private String portee; 
	private String type;
	private String nom;
	private String visibilite;
	private boolean statique;

	
	public AttributObjet(String nom, String portee, String type, String visibilite, boolean estStatique) 
	{
		this.nom = nom;
		this.portee = portee;
		this.type = type;
		this.visibilite = visibilite;
		this.statique = estStatique;
	}

	public AttributObjet(String nom, String portee, String type, String visibilite) 
	{
		this(nom, portee, type, visibilite, false);
	}

	
	public String getVisibilite() { return visibilite   ;	}
	public String getPortee    () {	return portee       ;	}
	public String getNom       () {	return nom          ;	}
	public String getType      () {	return type         ;	}
    public boolean isStatique () { return statique     ;	}


	public void setPortee     (String portee     ) {	this.portee     = portee     ;	}
	public void setVisibilite (String visibilite ) {	this.visibilite = visibilite ;	}
	public void setType       (String type       ) {	this.type       = type       ;	}
	public void setNom        (String nom        ) {	this.nom        = nom        ;	}
	public void setStatique   (boolean statique  ) {	this.statique   = statique   ;	}
}