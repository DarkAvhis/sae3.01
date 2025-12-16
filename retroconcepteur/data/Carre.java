package data;

public class Carre extends Rectangle
{
    

    protected double longueur;
    protected double largeur;

    public Carre(double longueur, double largeur) 
    {
        super(longueur, largeur);
    }

    public double getLongueur()
    {
        return longueur;
    }

    public double getLargeur() 
    {
        return largeur;
    }

    // Setters
    public void setLongueur(double longueur) 
    {
        this.longueur = longueur;
    }

    public void setLargeur(double largeur) 
    {
        this.largeur = largeur;
    }

    // Méthode de l’interface ISurface
    @Override
    public double surface() 
    {
        return longueur * largeur;
    }

    // Méthode toString
    public String toString() 
    {
        return "Rectangle [longueur=" + longueur + ", largeur=" + largeur + "]";
    }

    @Override
    public int zero() 
    {
       return 0;
    }

    @Override
    public double calculSurface() 
    {
        return longueur * largeur;
    }
}