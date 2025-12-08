import java.util.List;
import java.util.ArrayList;

public class Methode 
{
	private String nom;
	private String typeRetour;
	private String visibilite;

	private List<Argument> listeArguments;

	public Methode(String nom, String typeRetour, String visibilite, List<Argument> listeArguments) 
	{
		this.nom        = nom;
		this.typeRetour = typeRetour;
		this.visibilite = visibilite;
		this.listeArguments = new ArrayList<Argument>();
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
	
	public List<Argument> getListeArguments() 
	{
		return this.listeArguments;
	}

	public String toString() 
	{
		return "Methode    : " + String.format("%10s", nom       ) + ", " + 
		       "Visibilite : " + String.format("%10s", visibilite) + ", " +
		       "TypeRetour : " + String.format("%10s", typeRetour) + ", " +
			   "Parametres : " + this.listeArguments.toString();
	}
}