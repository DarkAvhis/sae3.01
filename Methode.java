import java.util.List;
import java.util.ArrayList;

public class Methode 
{
	private String nom;
	private String typeRetour;
	private String visibilite;

	private List<AttributObjet> listeAttributObjets;

	public Methode(String nom, String typeRetour, String visibilite, List<AttributObjet> listeAttributObjets) 
	{
		this.nom        = nom;
		this.typeRetour = typeRetour;
		this.visibilite = visibilite;
		this.listeAttributObjets = new ArrayList<AttributObjet>();
	}

	public String getNom() 
	{
		return this.nom;
	}

	public void setNom(String nom) 
	{
		this.nom = nom;
	}

	public String getTypeRetour() 
	{
		return this.typeRetour;
	}

	public void setTypeRetour(String typeRetour) 
	{
		this.typeRetour = typeRetour;
	}

	public String getVisibilite() 
	{
		return this.visibilite;
	}

	public void setVisibilite(String visibilite) 
	{
		this.visibilite = visibilite;
	}
	
	public List<AttributObjet> getListeAttributObjets() 
	{
		return this.listeAttributObjets;
	}

	public String toString() 
	{
		return "Methode    : " + String.format("%10s", nom       ) + ", " + 
		       "Visibilite : " + String.format("%10s", visibilite) + ", " +
		       "TypeRetour : " + String.format("%10s", typeRetour) + ", " +
			   "Parametres : " + this.listeAttributObjets.toString();
	}
}