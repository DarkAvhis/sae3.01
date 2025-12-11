package testFinal;
public class Disque {
    // ------------------------------------------------
    // Attributs (variables d'instance)
    // ------------------------------------------------ // Le centre du disque est un objet Point
    private double rayon;
    private Point[] points;

    // ------------------------------------------------
    // Constructeur
    // ------------------------------------------------
    /**
     * Crée un nouveau disque.
     * @param centre Le point central du disque.
     * @param rayon Le rayon du disque.
     */
    public Disque(Point centre, double rayon) {
        this.centre = centre;
        this.rayon = rayon;
    }

    // ------------------------------------------------
    // Méthodes de calcul
    // ------------------------------------------------
    /**
     * Calcule l'aire (surface) du disque.
     * La formule est Aire = π * rayon².
     * @return L'aire du disque.
     */
    public double calculerAire() {
        // Utilisation de Math.PI pour π et Math.pow pour le carré
        return Math.PI * Math.pow(rayon, 2);
    }

    /**
     * Calcule le périmètre (circonférence) du disque.
     * La formule est Périmètre = 2 * π * rayon.
     * @return Le périmètre du disque.
     */
    public double calculerPerimetre() {
        return 2 * Math.PI * rayon;
    }

    // ------------------------------------------------
    // Méthodes pour modifier les coordonnées du centre
    // ------------------------------------------------
    /**
     * Modifie la coordonnée x du centre du disque.
     * @param x La nouvelle valeur de x pour le centre.
     */
    public void setX(int x) {
        // Délègue la modification à l'objet Point 'centre'
        this.centre.setX(x);
    }

    /**
     * Modifie la coordonnée y du centre du disque.
     * @param y La nouvelle valeur de y pour le centre.
     */
    public void setY(int y) {
        // Délègue la modification à l'objet Point 'centre'
        this.centre.setY(y);
    }

    // ------------------------------------------------
    // Autres Getters/Setters (pour le rayon et le centre)
    // ------------------------------------------------
    
    /**
     * Retourne le rayon du disque.
     * @return Le rayon.
     */
    public double getRayon() {
        return rayon;
    }

    /**
     * Modifie le rayon du disque.
     * @param rayon Le nouveau rayon.
     */
    public void setRayon(double rayon) {
        this.rayon = rayon;
    }
    
    /**
     * Retourne l'objet Point représentant le centre du disque.
     * @return Le centre.
     */
    public Point getCentre() {
        return centre;
    }

    /**
     * Modifie le centre du disque.
     * @param centre Le nouvel objet Point pour le centre.
     */
    public void setCentre(Point centre) {
        this.centre = centre;
    }
}