// Fichier : testFinal/Disque.java
package testFinal;

public class Disque  implements ISurface
{
    // ------------------------------------------------
    // Attributs (variables d'instance)
    // ------------------------------------------------ // Le centre du disque est un objet Point
    private Point[] centre; // <-- ATTENTION : AJOUTÉ. C'est l'attribut d'association qui sera MASQUÉ par toString()
    private double rayon; // <-- ATTENTION : RESTAURÉ. C'est l'attribut qui sera AFFICHÉ.

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

    // ... (le reste du fichier est inchangé)
}