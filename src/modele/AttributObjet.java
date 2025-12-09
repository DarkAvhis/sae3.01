package modele;

/**
 * Représente un attribut dans une classe UML.
 * Contient son nom, son type, sa visibilité, et s'il est statique ou non.
 */
public class AttributObjet
{
	/*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
	private String  portee; 
	private String  type;
	private String  nom;
	private String  visibilite;
	private boolean statique;

	/*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
	public AttributObjet(String nom, String portee, String type, String visibilite, boolean statique) 
	{
		this.nom		 = nom;
		this.portee      = portee;
		this.type        = type;
		this.visibilite  = visibilite;
		this.statique    = statique;
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

	/*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
	public String  getVisibilite() {    return visibilite   ;	}
	public String  getPortee    () {	return portee       ;	}
	public String  getNom       () {	return nom          ;	}
	public String  getType      () {	return type         ;	}
    public boolean estStatique  () {    return statique     ;	}

	/*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
	public void setPortee     (String portee     ) {	this.portee     = portee     ;	}
	public void setVisibilite (String visibilite ) {	this.visibilite = visibilite ;	}
	public void setType       (String type       ) {	this.type       = type       ;	}
	public void setNom        (String nom        ) {	this.nom        = nom        ;	}
	public void setStatique   (boolean statique  ) {	this.statique   = statique   ;	}
}