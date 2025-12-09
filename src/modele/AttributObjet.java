package modele;
public class AttributObjet {

	private String portee;
	private String type;
	private String nom;
	private String visibilite;

	public AttributObjet(String nom, String portee, String type, String visibilite) {
		this.nom = nom;
		this.portee = portee;
		this.type = type;
		this.visibilite = visibilite;
	}

	
	public void setVisibilite(String visibilite) {
		this.visibilite = visibilite;
	}

	public String getPortee() {
		return portee;
	}

	public void setPortee(String portee) {
		this.portee = portee;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	

	public String getVisibilite() { return visibilite; }





	
}
