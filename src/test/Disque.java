package test;

public class Disque 
{
	// 0    1        2
	private double rayon;
	private Point centre;
	private boolean actif;
	private String couleur;
	private static int compteur ;

	public Disque(double rayon, Point centre) 
	{
		this.rayon  = rayon;
		this.centre = centre;
		this.actif = true;
		this.couleur = "Noir";
		compteur++;
	}

	public double getRayon() { return this.rayon; }
	public Point getCentre() { return this.centre;}

	public void setRayon(double rayon)  { this.rayon  = rayon ;}
	public void setCentre(Point centre) { this.centre = centre;}
	

	public double calculerAire()      
	{ 
		return Math.PI * Math.pow(this.rayon, 2);
	}

	public double calculerPerimetre() 
	{ 
		return 2 * Math.PI * this.rayon;           
	}

	// Méthodes supplémentaires avec différents types
	public synchronized void setActif(boolean actif)
	{
		this.actif = actif;
	}

	public synchronized boolean isActif()
	{
		return this.actif;
	}

	public String getCouleur()
	{
		return this.couleur;
	}

	public void setCouleur(String couleur)
	{
		this.couleur = couleur;
	}

	public static int getCompteur()
	{
		return compteur;
	}

	public static synchronized void reinitialiserCompteur()
	{
		compteur = 0;
	}

	public int comparerRayon(Disque autre)
	{
		if (this.rayon > autre.rayon) return 1;
		if (this.rayon < autre.rayon) return -1;
		return 0;
	}

	public boolean estIdentique(Disque autre)
	{
		return this.rayon == autre.rayon && this.centre.equals(autre.centre);
	}

	public Disque agrandir(double facteur)
	{
		return new Disque(this.rayon * facteur, this.centre);
	}

	public Disque diminuer(double pourcentage)
	{
		return new Disque(this.rayon * (1 - pourcentage / 100), this.centre);
	}

	public static Disque creerDisqueUnitaire(Point centre)
	{
		return new Disque(1.0, centre);
	}

	public static Disque creerDisqueAleatoire(Point centre)
	{
		return new Disque(Math.random() * 10, centre);
	}

	public char getCategorie()
	{
		if (this.rayon < 2) return 'P';
		if (this.rayon < 5) return 'M';
		return 'G';
	}

	public byte obtenirNiveauRemplissage()
	{
		byte niveau = (byte) Math.min(100, (this.rayon / 10) * 100);
		return niveau;
	}

	public long calculerVolumeSphere()
	{
		double volume = (4.0 / 3.0) * Math.PI * Math.pow(this.rayon, 3);
		return Math.round(volume);
	}

	public float calculerSurfaceSphere()
	{
		return (float) (4 * Math.PI * Math.pow(this.rayon, 2));
	}

	public short obtenirIdCategorie()
	{
		return (short) this.getCategorie();
	}

	public synchronized void afficherInfo()
	{
		System.out.println("Disque - Rayon: " + this.rayon + ", Centre: " + this.centre + 
				           ", Couleur: " + this.couleur + ", Actif: " + this.actif);
	}

	public static synchronized void afficherStatistiques()
	{
		System.out.println("Nombre de disques créés: " + compteur);
	}

	public Object obtenirDescriptif()
	{
		return "Disque[rayon=" + this.rayon + ", centre=" + this.centre + "]";
	}

	public Comparable<Disque> obtenirComparable()
	{
		return new Comparable<Disque>()
		{
			@Override
			public int compareTo(Disque autre)
			{
				return Double.compare(Disque.this.rayon, autre.rayon);
			}
		};
	}

	@Override
	public String toString()
	{
		return "Disque{" +
				"rayon=" + this.rayon +
				", centre=" + this.centre +
				", couleur='" + this.couleur + '\'' +
				", actif=" + this.actif +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Disque disque = (Disque) o;
		return Double.compare(disque.rayon, this.rayon) == 0 && 
		       this.centre.equals(disque.centre);
	}

	@Override
    public int hashCode()
	{
		return Double.hashCode(this.rayon) * 31 + this.centre.hashCode();
	}
}
