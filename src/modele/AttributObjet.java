package modele;

/**
 * Représente un attribut dans une classe UML.
 * Contient son nom, son type, sa visibilité, et s'il est statique ou non.
 */
public class AttributObjet
{
	/** Visibilité de l'attribut (public, private, protected). */
	private String portee; 
    /** Type de l'attribut (int, String, ClasseObjet, etc.). */
	private String type;
    /** Nom de l'attribut. */
	private String nom;
    /** Portée de l'attribut (statique / instance). */
	private String visibilite;
	/** Indique si l'attribut est statique. */
	private boolean estStatique;

	
	/**
     * Constructeur complet d'un attribut UML.
     *
     * @param nom         Nom de l'attribut
     * @param portee      Portée (ex. : "instance" ou "static")
     * @param type        Type de l'attribut
     * @param visibilite  Visibilité (public/private/protected)
     * @param estStatique true si l'attribut est statique
     */
	public AttributObjet(String nom, String portee, String type, String visibilite, boolean estStatique) 
	{
		this.nom		 = nom;
		this.portee      = portee;
		this.type        = type;
		this.visibilite  = visibilite;
		this.estStatique = estStatique;
	}

	/**
     * Constructeur simplifié qui crée un attribut non statique.
     *
     * @param nom         Nom de l'attribut
     * @param portee      Portée (ex. : "instance")
     * @param type        Type de l'attribut
     * @param visibilite  Visibilité
     */
	public AttributObjet(String nom, String portee, String type, String visibilite) 
	{
		this(nom, portee, type, visibilite, false);
	}

    /** @return la visibilité (public/private/protected) */	
	public String getVisibilite() { return visibilite ;}

    /** @return la portée (instance/static) */
	public String getPortee() 	  {	return portee     ;}

    /** @return le nom de l'attribut */
	public String getNom()	      {	return nom        ;}

	/** @return le type de l'attribut */
	public String getType() 	  {	return type       ;}


    /** @return true si l'attribut est statique */
    public boolean estStatique () { return estStatique ;}


	/** @param portee nouvelle portée */
	public void setPortee     (String portee    ) {this.portee      = portee     ;}

	/** @param visibilite nouvelle visibilité */
	public void setVisibilite (String visibilite) {this.visibilite  = visibilite ;}

    /** @param type nouveau type */
	public void setType       (String type      ) {this.type        = type       ;}

    /** @param nom nouveau nom */
	public void setNom        (String nom       ) {this.nom         = nom        ;}

	/** @param statique indique si l'attribut doit devenir statique */
	public void setStatique   (boolean statique ) {this.estStatique = statique   ;}
}