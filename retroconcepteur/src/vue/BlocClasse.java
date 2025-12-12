package src.vue;

import java.awt.*;

import java.util.List;
import java.util.ArrayList;

public class BlocClasse 
{
    private String  nom;
    private int     x;
    private int     y;
    private int     largeur;
    private int     hauteur;
    private boolean estInterface;
    private boolean estSelectionne;

    // Nouveaux champs pour stocker les détails
    private List<String> attributsAffichage;
    private List<String> methodesAffichage;

    // Constantes 
    private static final int   PADDING         = 10;
    private static final int   HAUTEUR_ENTETE  = 30;
    private static final int   HAUTEUR_LIGNE   = 20; // Nouvelle constante pour la hauteur d'une ligne de texte
    private static final Color COULEUR_FOND    = new Color(230, 240, 250);
    private static final Color COULEUR_BORDURE = new Color(0, 0, 0);
    private static final Color COULEUR_ENTETE  = new Color(100, 150, 200);

    // Mise à jour du constructeur
    public BlocClasse(String nom, int x, int y, List<String> attributs, List<String> methodes) 
    {
        this.nom                = nom;
        this.x                  = x;
        this.y                  = y;
        this.attributsAffichage = attributs;
        this.methodesAffichage  = methodes;

        this.estInterface   = false;
        this.estSelectionne = false;
        
        // Calculer la taille initiale minimale
        int maxLgNom       = nom.length() * 8; // Estimation
        int maxLgAttributs = attributs.stream().mapToInt(String::length).max().orElse(0) * 8;
        int maxLgMethodes  = methodes .stream().mapToInt(String::length).max().orElse(0) * 8;
        
        // Calcul de la largeur : min(max) ou 200
        this.largeur = Math.max(200, PADDING * 2 + Math.max(maxLgNom, Math.max(maxLgAttributs, maxLgMethodes)));

        // Calcul de la hauteur : Entête + (Attributs + Méthodes) * HAUTEUR_LIGNE + PADDINGS
        this.hauteur = HAUTEUR_ENTETE + 
                       (attributs.size() + methodes.size()) * HAUTEUR_LIGNE + 
                       PADDING * 4; // Ajuster les paddings internes
    }
    
    // Ancien constructeur à conserver (avec des listes vides) ou à retirer
    public BlocClasse(String nom, int x, int y) 
    {
        this(nom, x, y, new ArrayList<>(), new ArrayList<>());
    }

    public void dessiner(Graphics2D g) 
    {
        // 1. Fond et Bord
        g.setColor(COULEUR_FOND);
        g.fillRect(x, y, largeur, hauteur);

        g.setColor(estSelectionne ? Color.BLUE : COULEUR_BORDURE);
        g.setStroke(new BasicStroke(estSelectionne ? 2 : 1));
        g.drawRect(x, y, largeur, hauteur);

        // 2. Entête (Nom de la classe)
        g.setColor(COULEUR_ENTETE);
        g.fillRect(x, y, largeur, HAUTEUR_ENTETE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (largeur - fm.stringWidth(nom)) / 2;
        int textY = y + HAUTEUR_ENTETE - (HAUTEUR_ENTETE - fm.getAscent()) / 2;
        g.drawString(nom, textX, textY);

        // 3. Dessin des Attributs
        int currentY = y + HAUTEUR_ENTETE + PADDING; // Démarrer après l'entête
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        for (String att : attributsAffichage) 
        {
            currentY += HAUTEUR_LIGNE;
            g.drawString(att, x + PADDING, currentY);
        }
        
        // 4. Ligne de séparation (Attributs / Méthodes)
        currentY += PADDING / 2;
        g.setColor(COULEUR_BORDURE);
        g.drawLine(x, currentY, x + largeur, currentY);
        
        // 5. Dessin des Méthodes
        currentY += PADDING;
        
        for (String met : methodesAffichage) 
        {
            currentY += HAUTEUR_LIGNE;
            g.drawString(met, x + PADDING, currentY);
        }

        // 6. Gestion Interface (si besoin)
        if (estInterface) 
        {
            // ... (logique de dessin <<interface>> conservée)
        }
    }

    public boolean contient(int px, int py) 
    {
        return px >= x 
        && px <= x + largeur 
        && py >= y 
        && py <= y + hauteur;
    }

    // Getters et Setters
    public String getNom    () { return this.nom    ; }
    public int    getX      () { return this.x      ; }
    public int    getY      () { return this.y      ; }
    public int    getLargeur() { return this.largeur; }
    public int    getHauteur() { return this.hauteur; }

    public void setX          (int x               ) { this.x              = x           ; }
    public void setY          (int y               ) { this.y              = y           ; }
    public void setInterface  (boolean estInterface) { this.estInterface   = estInterface; }
    public void setSelectionne(boolean selectionne ) { this.estSelectionne = selectionne ; }

    public boolean estInterface  ()  { return this.estInterface  ; }
    public boolean estSelectionne()  { return this.estSelectionne; }
}