package testFinal;

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

    public int getX     (){return x  ;}
    public int getY     (){return y  ;}
    public String getNom(){return nom;}

    // ------------------------------------------------
    // Méthodes de modification (Setters)
    // ------------------------------------------------

    public void setX  (int x)     {this.x = x    ;}
    public void setY  (int y)     {this.y = y    ;}
    public void setNom(String nom){this.nom = nom;}
}
