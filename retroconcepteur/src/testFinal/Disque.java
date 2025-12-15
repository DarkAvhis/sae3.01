// Fichier : testFinal/Disque.java
package testFinal;

public class Disque extends Forme implements ISurface
{
    // ------------------------------------------------
    // Attributs (variables d'instance)
    // ------------------------------------------------ // Le centre du disque est un objet Point
    private Point[] centre;
    private double rayon; 

    // ------------------------------------------------
    // Constructeur
    // ------------------------------------------------
    /**
     * Crée un nouveau disque.
     * @param centre Le point central du disque.
     * @param rayon Le rayon du disque.
     */
    public Disque(Point[] centre, double rayon) 
    {
        super("rouge"); 
        this.centre = centre;
        this.rayon = rayon;
    }
    
    public double aire() 
    {
        return Math.PI * rayon * rayon;
    }

    public double surface() 
    {
        return aire();
    }

    @Override
    public int zero() 
    {
        return 0;
    }
}
