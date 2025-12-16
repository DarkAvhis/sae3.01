package data;

public abstract class Rectangle implements ISurface 
{
    protected double longueur;
    protected double largeur;

    // Constructeur
    public Rectangle(double longueur, double largeur) 
    {
        this.longueur = longueur;
        this.largeur = largeur;
    }

    // Getters
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
}