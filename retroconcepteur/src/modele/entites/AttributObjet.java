package src.modele.entites;

/**
 * Représente un attribut dans une classe UML.
 * Contient son nom, son type, sa visibilité, s'il est statique et s'il est final.
 */
public class AttributObjet
{
	private String  portee; 
	private String  type;
	private String  nom;
	private String  visibilite;
	private boolean statique;
    private boolean finale;  

	/**
     * Constructeur complet.
     */
	public AttributObjet(String nom, String portee, String type, String visibilite, boolean statique, boolean finale) 
	{
		this.nom		 = nom;
		this.portee      = portee;
		this.type        = type;
		this.visibilite  = visibilite;
		this.statique    = statique;
        this.finale      = finale;
	}

	/**
     * Constructeur simplifié (non statique, non final).
     */
	public AttributObjet(String nom, String portee, String type, String visibilite) 
	{
		this(nom, portee, type, visibilite, false, false);
	}

	/*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
	public String  getVisibilite() {    return this.visibilite   ;	}
	public String  getPortee    () {	return this.portee       ;	}
	public String  getNom       () {	return this.nom          ;	}
	public String  getType      () {	return this.type         ;	}
    public boolean estStatique  () {    return this.statique     ;	}
    public boolean estFinale    () {    return this.finale       ;   } 

	/*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
	public void setPortee     (String portee     ) {	this.portee     = portee     ;	}
	public void setVisibilite (String visibilite ) {	this.visibilite = visibilite ;	}
	public void setType       (String type       ) {	this.type       = type       ;	}
	public void setNom        (String nom        ) {	this.nom        = nom        ;	}
	public void setStatique   (boolean statique  ) {	this.statique   = statique   ;	}
    public void setFinale     (boolean finale    ) {    this.finale     = finale     ;   } 
}