// File: vue/BlocClasse.java
package vue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Représentation graphique d'une classe UML.
 *
 * Un BlocClasse affiche le nom de la classe, ses attributs et ses méthodes
 * dans un rectangle avec un en-tête coloré.
 *
 * @author Quentin MORVAN,
 *         Valentin LEROY,
 *         Celim CHAOU,
 *         Enzo DUMONT,
 *         Ariunbayar BUYANBADRAKH,
 *         Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class BlocClasse
{
    private String nom;

    private int x      ;
    private int y      ;
    private int largeur;
    private int hauteur;
    private boolean estInterface;
    private boolean estSuperClasse;
    private boolean estSelectionne;
    private boolean estExterne; // NOUVEAU: Champ pour marquer une classe comme externe

    private List<String> attributsAffichage;
    private List<String> methodesAffichage ;

    private static final int PADDING = 10;
    private static final int HAUTEUR_ENTETE = 30;
    private static final int HAUTEUR_LIGNE = 20;

    private static final Color COULEUR_FOND =
        new Color(230, 240, 250);
    private static final Color COULEUR_FOND_EXTERNE =
        new Color(235, 235, 235);
    private static final Color COULEUR_BORDURE =
        new Color(0, 0, 0);
    private static final Color COULEUR_ENTETE =
        new Color(100, 150, 200);
    private static final Color COULEUR_ENTETE_EXTERNE =
        new Color(140, 140, 140);

    public BlocClasse(
        String nom,
        int x,
        int y,
        List<String> attributs,
        List<String> methodes
    )
    {
        this.nom = nom;
        this.x   = x  ;
        this.y   = y  ;
        this.attributsAffichage = attributs;
        this.methodesAffichage  = methodes;

        this.estInterface = false;
        this.estSuperClasse = false;
        this.estSelectionne = false;
        this.estExterne = false; // Initialisation du nouveau champ

        int maxLgNom = nom.length() * 8;
        int maxLgAttributs =
            attributs.stream()
                     .mapToInt(String::length)
                     .max()
                     .orElse(0) * 8;
        int maxLgMethodes =
            methodes.stream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(0) * 8;

        this.largeur = Math.max(
            200,
            PADDING * 2 + Math.max(
                maxLgNom,
                Math.max(maxLgAttributs, maxLgMethodes)
            )
        );

        this.hauteur =
            HAUTEUR_ENTETE
            + (attributs.size() + methodes.size()) * HAUTEUR_LIGNE
            + PADDING * 4;
    }

    public BlocClasse(String nom, int x, int y)
    {
        this(nom, x, y, new ArrayList<>(), new ArrayList<>());
    }

    public void dessiner(Graphics2D g)
    {
        g.setColor(estExterne ? COULEUR_FOND_EXTERNE : COULEUR_FOND);
        g.fillRect(x, y, largeur, hauteur);

        g.setColor(estSelectionne ? Color.BLUE : COULEUR_BORDURE);
        g.setStroke(new BasicStroke(estSelectionne ? 2 : 1));
        g.drawRect(x, y, largeur, hauteur);

        g.setColor(estExterne ? COULEUR_ENTETE_EXTERNE : COULEUR_ENTETE);
        g.fillRect(x, y, largeur, HAUTEUR_ENTETE);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));

        FontMetrics fm = g.getFontMetrics();
        int textX = x + (largeur - fm.stringWidth(nom)) / 2;
        int textY =
            y + HAUTEUR_ENTETE
            - (HAUTEUR_ENTETE - fm.getAscent()) / 2;

        g.drawString(nom, textX, textY);

        int currentY = y + HAUTEUR_ENTETE + PADDING;
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        for (String att : attributsAffichage)
        {
            currentY += HAUTEUR_LIGNE;

            boolean estStatique = att.contains("{static}");
            String libelle =
                att.replace(" {static}", "")
                   .replace("{static} ", "");

            g.drawString(libelle, x + PADDING, currentY);

            if (estStatique)
            {
                FontMetrics fmLigne = g.getFontMetrics();
                int underlineY = currentY + 2;
                int underlineX2 =
                    x + PADDING + fmLigne.stringWidth(libelle);

                g.drawLine(
                    x + PADDING,
                    underlineY,
                    underlineX2,
                    underlineY
                );
            }
        }

        currentY += PADDING / 2;
        g.setColor(COULEUR_BORDURE);
        g.drawLine(x, currentY, x + largeur, currentY);

        currentY += PADDING;

        for (String met : methodesAffichage)
        {
            currentY += HAUTEUR_LIGNE;

            boolean estStatique = met.contains("{static}");
            String libelle =
                met.replace(" {static}", "")
                   .replace("{static} ", "");

            g.drawString(libelle, x + PADDING, currentY);

            if (estStatique)
            {
                FontMetrics fmLigne = g.getFontMetrics();
                int underlineY = currentY + 2;
                int underlineX2 =
                    x + PADDING + fmLigne.stringWidth(libelle);

                g.drawLine(
                    x + PADDING,
                    underlineY,
                    underlineX2,
                    underlineY
                );
            }
        }
    }

    public boolean contient(int px, int py)
    {
        return px >= x && px <= x + largeur
            && py >= y && py <= y + hauteur;
    }

    public String getNom()
    {
        return nom;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getLargeur()
    {
        return largeur;
    }

    public int getHauteur()
    {
        return hauteur;
    }

    public boolean estInterface()
    {
        return estInterface;
    }

    public boolean estSelectionne()
    {
        return estSelectionne;
    }

    public boolean estExterne()
    {
        return estExterne;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setInterface(boolean estInterface)
    {
        this.estInterface = estInterface;
    }

    public void setSelectionne(boolean selectionne)
    {
        this.estSelectionne = selectionne;
    }

    public void setExterne(boolean externe)
    {
        this.estExterne = externe;
    }

    public void setAttributs(List<String> attributs)
    {
        this.attributsAffichage = attributs;
        recalculerDimensions();
    }

    public void setMethodes(List<String> methodes)
    {
        this.methodesAffichage = methodes;
        recalculerDimensions();
    }

    private void recalculerDimensions()
    {
        int maxLongueur = nom != null ? nom.length() : 0;

        for (String att : attributsAffichage)
        {
            if (att == null)
            {
                continue;
            }

            int longueur =
                att.replace("{static}", "").trim().length();

            maxLongueur = Math.max(maxLongueur, longueur);
        }

        for (String met : methodesAffichage)
        {
            if (met == null)
            {
                continue;
            }

            int longueur =
                met.replace("{static}", "").trim().length();

            maxLongueur = Math.max(maxLongueur, longueur);
        }

        this.largeur = Math.max(
            200,
            PADDING * 2 + maxLongueur * 8
        );

        this.hauteur =
            HAUTEUR_ENTETE
            + (attributsAffichage.size()
               + methodesAffichage.size()) * HAUTEUR_LIGNE
            + PADDING * 4;
    }
}
