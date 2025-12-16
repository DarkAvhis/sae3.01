package testFinal;

import java.util.List;
import java.util.ArrayList;

public abstract class Forme 
{
	private String couleur;

	private List<Forme>	formes;
	
	public Forme( String couleur )
	{
		this.couleur = couleur;
		this.formes = new ArrayList<Forme>();
	}

	public String getCouleur() 
	{
		return couleur;
	}

	public List<Forme> getFormes()
	{
		return this.formes;
	}

	public void setCouleur(String couleur) 
	{
		this.couleur = couleur;
	}

	public void ajouterForme(Forme f)
	{
		this.formes.add(f);
	}

	public abstract double aire();
}
