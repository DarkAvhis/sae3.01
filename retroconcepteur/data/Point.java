package data;

public class Point 
{
    // ------------------------------------------------
    // Attributs (variables d'instance)
    // ------------------------------------------------
    private String nom; // Ajout de 'nom' tel qu'indiqué dans le constructeur
    private int x;
    private int y;

    // ------------------------------------------------
    // Constructeur
    // ------------------------------------------------
    /**
     * Crée un nouveau point avec un nom et des coordonnées spécifiées.
     * @param nom Le nom du point.
     * @param x La coordonnée x du point.
     * @param y La coordonnée y du point.
     */
    public Point(String nom, int x, int y) 
    {
        this.nom = nom;
        this.x = x;
        this.y = y;
    }

    // ------------------------------------------------
    // Méthodes d'accès (Getters)
    // ------------------------------------------------
    /**
     * Retourne la coordonnée x du point.
     * @return La valeur de x.
     */
    public int getX() 
    {
        return x;
    }

    /**
     * Retourne la coordonnée y du point.
     * @return La valeur de y.
     */
    public int getY() 
    {
        return y;
    }
    
    /**
     * Retourne le nom du point.
     * @return Le nom du point.
     */
    public String getNom() 
    {
        return nom;
    }

    // ------------------------------------------------
    // Méthodes de modification (Setters)
    // ------------------------------------------------
    /**
     * Modifie la coordonnée x du point.
     * @param x La nouvelle valeur de x.
     */
    public void setX(int x) 
    {
        this.x = x;
    }

    /**
     * Modifie la coordonnée y du point.
     * @param y La nouvelle valeur de y.
     */
    public void setY(int y) 
    {
        this.y = y;
    }
    
    /**
     * Modifie le nom du point.
     * @param nom Le nouveau nom du point.
     */
    public void setNom(String nom) 
    {
        this.nom = nom;
    }
}