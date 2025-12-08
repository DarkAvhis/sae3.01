package vue;

import java.awt.*;

public class BlocClasse {

    private String nom;
    private int x;
    private int y;
    private int largeur;
    private int hauteur;
    private boolean estInterface;
    private boolean estSelectionne;

    // Constantes de style
    private static final int PADDING = 10;
    private static final int HAUTEUR_ENTETE = 30;
    private static final int HAUTEUR_LIGNE = 20;
    private static final Color COULEUR_FOND = new Color(230, 240, 250);
    private static final Color COULEUR_BORDURE = new Color(0, 0, 0);
    private static final Color COULEUR_ENTETE = new Color(100, 150, 200);

    public BlocClasse(String nom, int x, int y) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.largeur = 200;
        this.hauteur = 150;
        this.estInterface = false;
        this.estSelectionne = false;
    }

    public void dessiner(Graphics2D g) {
        // Fond du bloc
        g.setColor(COULEUR_FOND);
        g.fillRect(x, y, largeur, hauteur);

        // Bordure
        g.setColor(estSelectionne ? Color.BLUE : COULEUR_BORDURE);
        g.setStroke(new BasicStroke(estSelectionne ? 2 : 1));
        g.drawRect(x, y, largeur, hauteur);

        // En-tête (nom de la classe)
        g.setColor(COULEUR_ENTETE);
        g.fillRect(x, y, largeur, HAUTEUR_ENTETE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (largeur - fm.stringWidth(nom)) / 2;
        int textY = y + HAUTEUR_ENTETE - (HAUTEUR_ENTETE - fm.getAscent()) / 2;
        g.drawString(nom, textX, textY);

        // Texte "<<interface>>" si c'est une interface
        if (estInterface) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.ITALIC, 10));
            String interfaceText = "<<interface>>";
            FontMetrics fm2 = g.getFontMetrics();
            int textX2 = x + (largeur - fm2.stringWidth(interfaceText)) / 2;
            g.drawString(interfaceText, textX2, y + HAUTEUR_ENTETE + 15);
        }

        // Lignes de séparation (attributs et méthodes)
        g.setColor(COULEUR_BORDURE);
        int ligneY = y + HAUTEUR_ENTETE + 40;
        g.drawLine(x, ligneY, x + largeur, ligneY);

        // Placeholder pour attributs et méthodes
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("Attributs", x + PADDING, y + HAUTEUR_ENTETE + 25);
        g.drawString("Méthodes", x + PADDING, ligneY + 20);
    }

    /**
     * Vérifie si le point donné est dans le bloc.
     */
    public boolean contient(int px, int py) {
        return px >= x && px <= x + largeur && py >= y && py <= y + hauteur;
    }

    /**
     * Déplace le bloc à une nouvelle position.
     */
    public void deplacer(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public boolean estInterface() {
        return estInterface;
    }

    public void setInterface(boolean estInterface) {
        this.estInterface = estInterface;
    }

    public boolean estSelectionne() {
        return estSelectionne;
    }

    public void setSelectionne(boolean selectionne) {
        estSelectionne = selectionne;
    }
}
