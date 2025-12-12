package src.modele;

/**
 * Représente un attribut dans une classe UML.
 * 
 * Un attribut possède un nom, un type, une visibilité (public, private,
 * protected)
 * et peut être marqué comme statique ou final.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class AttributObjet
{
	private String portee;
	private String type;
	private String nom;
	private String visibilite;
	private boolean statique;
	private boolean finale; // NOUVEAU : pour gérer 'final'

	/**
	 * Constructeur complet d'un attribut.
	 * 
	 * @param nom        Nom de l'attribut
	 * @param portee     Portée de l'attribut ("instance" ou "static")
	 * @param type       Type de l'attribut (int, String, etc.)
	 * @param visibilite Visibilité de l'attribut ("public", "private", "protected")
	 * @param statique   true si l'attribut est statique, false sinon
	 * @param finale     true si l'attribut est final, false sinon
	 */
	public AttributObjet(String nom, String portee, String type, String visibilite, boolean statique, boolean finale) 
	{
		this.nom = nom;
		this.portee = portee;
		this.type = type;
		this.visibilite = visibilite;
		this.statique = statique;
		this.finale = finale;
	}

	/**
	 * Constructeur simplifié d'un attribut.
	 * 
	 * Crée un attribut non statique et non final par défaut.
	 * 
	 * @param nom        Nom de l'attribut
	 * @param portee     Portée de l'attribut
	 * @param type       Type de l'attribut
	 * @param visibilite Visibilité de l'attribut
	 */
	public AttributObjet(String nom, String portee, String type, String visibilite) 
	{
		this(nom, portee, type, visibilite, false, false);
	}

	/*-------------------------------------- */
	/* Les Accesseurs */
	/*-------------------------------------- */
	public String getVisibilite() 
	{
		return visibilite;
	}

	public String getPortee() 
	{
		return portee;
	}

	public String getNom() 
	{
		return nom;
	}

	public String getType() 
	{
		return type;
	}

	public boolean estStatique() 
	{
		return statique;
	}

	public boolean estFinale() 
	{
		return finale;
	}

	/*-------------------------------------- */
	/* Modificateurs */
	/*-------------------------------------- */
	public void setPortee(String portee) 
	{
		this.portee = portee;
	}

	public void setVisibilite(String visibilite) 
{
		this.visibilite = visibilite;
	}

	public void setType(String type) 
{
		this.type = type;
	}

	public void setNom(String nom) 
{
		this.nom = nom;
	}

	public void setStatique(boolean statique) 
{
		this.statique = statique;
	}

	public void setFinale(boolean finale) 
{
		this.finale = finale;
	}
}
