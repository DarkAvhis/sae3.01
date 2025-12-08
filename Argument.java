public class Argument 
{
	private static int numArgument;

	private String nom;
	private String type;
	private String visibilite;
	private String portee;
	private int nbArgument;

	public Argument(String nom, String type, String visibilite, String portee) 
	{
		this.nom        = nom;
		this.type       = type;
		this.visibilite = visibilite;
		this.portee     = portee;

		this.nbArgument = ++numArgument;
	}

	public String getNom()  
	{
		return this.nom;
	}

	public void setNom(String nom) 
	{
		this.nom = nom;
	}

	public String getType() 
	{
		return this.type;
	}

	public void setType(String type) 
	{
		this.type = type;
	}

	public String getVisibilite() 
	{
		return this.visibilite;
	}

	public void setVisibilite(String visibilite) 
	{
		this.visibilite = visibilite;
	}

	public String getPortee() 
	{
		return this.portee;
	}

	public void setPortee(String portee) 
	{
		this.portee = portee;
	}	

	public String toString() 
	{
		return "Parametre   "   + String.format("%2d", this.nbArgument) + ", " + 
				"Nom        : " + String.format("%10s", this.nom      ) + ", " +
				"Type       : " + String.format("%10s", this.type     ) + ", " + 
				"Visibilité : " + String.format("%10s",this.visibilite) + ", " +
				"Portée     : " + String.format("%10s",this.portee    );
	}
}