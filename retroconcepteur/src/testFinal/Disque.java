// Fichier : testFinal/Disque.java
package testFinal;

public class Disque  implements ISurface
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
        this.centre = centre;
        this.rayon = rayon;
    }
    
    public int zero()
    {
        return 0;
    }

    private class TestInterne
    {

    }
}
