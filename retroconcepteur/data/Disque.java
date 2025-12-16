// Fichier : testFinal/Disque.java
package data;

public class Disque implements ISurface
{
    // ------------------------------------------------
    // Attributs
    // ------------------------------------------------

    private Point[] centre;
    private double rayon;
    private static int compteurDisques = 0;
    private final double PI_LOCAL = 3.14159;
    public static final double PI = 3.14159;
    private boolean actif = true;
    protected String nom;

    /** Exemple : attribut privé sans association. */
    private int idInterne;

    // ------------------------------------------------
    // Constructeurs
    // ------------------------------------------------
    public Disque(Point[] centre, double rayon)
    {
        this.centre = centre;
        this.rayon = rayon;
        this.nom = "SansNom";
        this.idInterne = ++compteurDisques;
    }

    // Surcharge
    public Disque(double rayon)
    {
        this.centre = new Point[]{ new Point("",0,0) };
        this.rayon  = rayon;
        this.nom    = "Origine";
        this.idInterne = ++compteurDisques;
    }
    
    public double getRayon()
    {
        return this.rayon;
    }

    public void setRayon(double rayon)
    {
        this.rayon = rayon;
    }

    public boolean estActif()
    {
        return actif;
    }

    public void activer()
    {
        this.actif = true;
    }

    public void desactiver()
    {
        this.actif = false;
    }

    /** Exemple de méthode retournant un objet → affichée. */
    public Point getCentrePrincipal()
    {
        return centre[0];
    }

    /** Méthode finale → affichée avec {gelé} dans ton UML. */
    public final double aire()
    {
        return PI * rayon * rayon;
    }

    /** Implémentation d'interface. */
    @Override
    public double calculSurface()
    {
        return aire();
    }
 
    protected void renommer(String nouveauNom)
    {
        this.nom = nouveauNom;
    }

    private double diametre()
    {
        return rayon * 2.0;
    }

    private String debugInfos()
    {
        return "[DEBUG] r=" + rayon + ", actif=" + actif;
    }

    public static int getCompteur()
    {
        return compteurDisques;
    }

    public static void resetCompteur()
    {
        compteurDisques = 0;
    }

    public double calculerDistance(Point p)
    {
        Point c   = centre[0];
        double dx = p.getX() - c.getX();
        double dy = p.getY() - c.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    public double calculerDistance(double x, double y)
    {
        Point  c  = centre[0];
        double dx = x - c.getX();
        double dy = y - c.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    @Override
    public int zero() 
    {
        return 0;
    }

    @Override
    public double surface() 
    {
        return aire();
    }
}