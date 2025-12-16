package modele.entites;


import java.util.HashMap;

/**
 * Représente une méthode d'une classe UML.
 * 
 * Chaque méthode possède :
 * 
 * nom : le nom de la méthode
 * visibilite : public, private ou protected
 * retourType : type de retour (null ou void si aucune valeur)
 * parametres : liste des paramètres sous forme de HashMap (nom -> type)
 * statique : indique si la méthode est statique
 */
public class MethodeObjet 
{
    /*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
	private String nom;
	private String visibilite;
	private String retourType;

	private HashMap<String, String> parametres; 

	private boolean estStatique;

    /*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
	public MethodeObjet(String nom, HashMap<String, String> parametres, String retourType, String visibilite, boolean estStatique) 
	{
		this.nom         = nom;
		this.parametres  = parametres;
		this.retourType  = retourType;
		this.visibilite  = visibilite;
		this.estStatique = estStatique;
	}

	public MethodeObjet( String nom, HashMap<String, String> parametres,String visibilite, boolean estStatique )
	{
		this.nom         = nom;
		this.parametres  = parametres;
		this.visibilite  = visibilite;
		this.estStatique = estStatique;
	}

    /*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    public String                  getNom       () { return this.nom         ;  }
    public String                  getVisibilite() { return this.visibilite  ;  }
    public String                  getRetourType() { return this.retourType  ;  }
    public HashMap<String, String> getParametres() { return this.parametres  ;  }
    public boolean                 estStatique  () { return this.estStatique ;  }

    /*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
    public void setNom       (String                  nom        ) { this.nom         = nom         ;  }
    public void setVisibilite(String                  visibilite ) { this.visibilite  = visibilite  ;  }
    public void setRetourType(String                  retourType ) { this.retourType  = retourType  ;  }
    public void setParametres(HashMap<String, String> parametres ) { this.parametres  = parametres  ;  }
    public void setStatique  (boolean                 estStatique) { this.estStatique = estStatique ;  }
} 