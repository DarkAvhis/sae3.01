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
public class BlocClasse {
        // Ajout d'un champ pour le type spécifique (interface, record, abstract class)
        private String typeSpecifique = null;

        public void setTypeSpecifique(String type) {
            this.typeSpecifique = type;
        }

        public String getTypeSpecifique() {
            return this.typeSpecifique;
        }
    private boolean modeComplet = false;
    private String nom;

    private int x;
    private int y;
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

    private static final Color COULEUR_FOND = new Color(255, 255, 255);
    private static final Color COULEUR_FOND_EXTERNE = new Color(235, 235, 235);
    private static final Color COULEUR_BORDURE = new Color(0, 0, 0);
    private static final Color COULEUR_ENTETE = new Color(0, 0, 0);
    private static final Color COULEUR_ENTETE_EXTERNE = new Color(140, 140, 140);

    public BlocClasse(
            String nom,
            int x,
            int y,
            List<String> attributs,
            List<String> methodes) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.attributsAffichage = attributs;
        this.methodesAffichage = methodes;

        this.estInterface = false;
        this.estSuperClasse = false;
        this.estSelectionne = false;
        this.estExterne = false; // Initialisation du nouveau champ

        // Calcul précis de la largeur du nom et du sous-titre avec la police réelle
        java.awt.Font fontNom = new Font("Arial", Font.BOLD, 12);
        java.awt.Font fontSousTitre = new Font("Arial", Font.ITALIC, 11);
        java.awt.Canvas c = new java.awt.Canvas();
        java.awt.FontMetrics fmNom = c.getFontMetrics(fontNom);
        java.awt.FontMetrics fmSousTitre = c.getFontMetrics(fontSousTitre);

        int largeurNom = fmNom.stringWidth(nom);
        int largeurSousTitre = 0;
        if (typeSpecifique != null && !typeSpecifique.isEmpty()) {
            largeurSousTitre = fmSousTitre.stringWidth("<<" + typeSpecifique + ">>");
        } else if (estInterface) {
            largeurSousTitre = fmSousTitre.stringWidth("<<interface>>");
        }

        int maxLgAttributs = attributs.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0) * 8;
        int maxLgMethodes = methodes.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0) * 8;

        int maxLgEntete = Math.max(largeurNom, largeurSousTitre);

        this.largeur = Math.max(
            200,
            PADDING * 2 + Math.max(maxLgEntete, Math.max(maxLgAttributs, maxLgMethodes)));

        this.hauteur = HAUTEUR_ENTETE
                + (attributs.size() + methodes.size()) * HAUTEUR_LIGNE
                + PADDING * 4;
    }

    public BlocClasse(String nom, int x, int y) {
        this(nom, x, y, new ArrayList<>(), new ArrayList<>());
    }

    public boolean isModeComplet() {
        return modeComplet;
    }

    public void setModeComplet(boolean complet) {
        this.modeComplet = complet;
        recalculerDimensions();
    }

    /**
     * Limite l'affichage des paramètres d'une méthode à 2 paramètres.
     * Si plus de 2 paramètres, affiche les 2 premiers suivis de "..."
     */
    private String limiterParametres(String methode) {
        int idxParenthese = methode.indexOf('(');
        if (idxParenthese < 0)
            return methode;

        int idxFermante = methode.lastIndexOf(')');
        if (idxFermante < 0)
            return methode;

        String avant = methode.substring(0, idxParenthese + 1);
        String params = methode.substring(idxParenthese + 1, idxFermante);
        String apres = methode.substring(idxFermante);

        if (params.trim().isEmpty())
            return methode;

        String[] paramsTab = params.split(",");
        if (paramsTab.length <= 2)
            return methode;

        return avant + paramsTab[0].trim() + ", " + paramsTab[1].trim() + ", ..." + apres;
    }

   public void dessiner(Graphics2D g) 
   {
        dessinerFondEtBordure(g);
        int currentY = dessinerNom(g);
        
        // Appel des méthodes de dessin internes

        currentY = dessinerAttributs(g, currentY);
        currentY = dessinerSeparateur(g, currentY);
        currentY = dessinerMethodes(g, currentY);

    }
    // Affiche le fond et la bordure du bloc
    private void dessinerFondEtBordure(Graphics2D g) 
    {
        // Choix de la couleur de fond selon que la classe est externe ou non
        g.setColor(estExterne ? COULEUR_FOND_EXTERNE : COULEUR_FOND);
        g.fillRect(x, y, largeur, hauteur);
        
        // Dessin de la bordure (en bleu si sélectionnée)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, largeur, hauteur);

        

    }

    // Affiche le nom de la classe et retourne la position Y pour la suite du dessin
private int dessinerNom(Graphics2D g) 
{
    boolean avecTypeSpecifique = typeSpecifique != null && !typeSpecifique.isEmpty();

    int yEntete = y - (avecTypeSpecifique ? 5 : 0);
    int hauteurEnteteReelle = HAUTEUR_ENTETE + (avecTypeSpecifique ? 5 : 0);

    g.setColor(Color.BLACK);

    // ---- NOM ----
    g.setFont(new Font("Arial", Font.BOLD, 12));
    FontMetrics fmNom = g.getFontMetrics();
    int nomX = x + (largeur - fmNom.stringWidth(nom)) / 2;

    int nomY;
    if (avecTypeSpecifique) {
       nomY = y + HAUTEUR_ENTETE - (HAUTEUR_ENTETE - fmNom.getAscent()) / 2;
    } else {
        nomY = y + HAUTEUR_ENTETE - (HAUTEUR_ENTETE - fmNom.getAscent()) / 2;
    }

    g.drawString(nom, nomX, nomY);

    int dernierTexteY = nomY; // servira pour le séparateur

    // ---- TYPE ----
    if (avecTypeSpecifique) {
        String sousTitre = "<<" + typeSpecifique + ">>";
        g.setFont(new Font("Arial", Font.ITALIC, 11));
        FontMetrics fmSous = g.getFontMetrics();
        int sousX = x + (largeur - fmSous.stringWidth(sousTitre)) / 2;
        int sousY = nomY + fmSous.getHeight();
        g.setColor(Color.BLACK);
        g.drawString(sousTitre, sousX, sousY);
        dernierTexteY = sousY;
    }

    // ---- SÉPARATEUR ----
    int separateurY = dernierTexteY + 4; // petit espace sous le texte
    g.drawLine(x, separateurY, x + largeur, separateurY);

    // Retour Y pour les attributs
    return separateurY + PADDING;
}


    private int dessinerAttributs(Graphics2D g, int currentY) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        int maxAttributs = modeComplet ? Integer.MAX_VALUE : 3;
        int iAtt = 0;
        
        for (String att : attributsAffichage) {
            if (iAtt >= maxAttributs) break;
            currentY += HAUTEUR_LIGNE;
            
            boolean estStatique = att.contains("{static}");
            String libelle = att.replace("{static}", "").trim();
            
            g.drawString(libelle, x + PADDING, currentY);
            
            if (estStatique) {
                FontMetrics fm = g.getFontMetrics();
                g.drawLine(x + PADDING, currentY + 2, x + PADDING + fm.stringWidth(libelle), currentY + 2);
            }
            iAtt++;
        }
        
        if (!modeComplet && attributsAffichage.size() > maxAttributs) {
            currentY += HAUTEUR_LIGNE;
            g.drawString("...", x + PADDING, currentY);
        }
        return currentY;
    }

    // Affiche le séparateur et retourne la position Y courante
    private int dessinerSeparateur(Graphics2D g, int currentY) {
        currentY += PADDING / 2;
        g.setColor(COULEUR_BORDURE);
        g.drawLine(x, currentY, x + largeur, currentY);
        currentY += PADDING;
        return currentY;
    }

    // Affiche les méthodes et retourne la position Y finale
    private int dessinerMethodes(Graphics2D g, int currentY) 
    {
        int maxMethodes = modeComplet ? Integer.MAX_VALUE : 3;
        int iMet = 0;
        
        for (String met : methodesAffichage) {
            if (iMet >= maxMethodes) break;
            currentY += HAUTEUR_LIGNE;
            
            boolean estStatique = met.contains("{static}");
            String libelle = met.replace("{static}", "").trim();
            
            g.drawString(libelle, x + PADDING, currentY);
            
            if (estStatique) {
                FontMetrics fm = g.getFontMetrics();
                g.drawLine(x + PADDING, currentY + 2, x + PADDING + fm.stringWidth(libelle), currentY + 2);
            }
            iMet++;
        }
        
        if (!modeComplet && methodesAffichage.size() > maxMethodes) {
            currentY += HAUTEUR_LIGNE;
            g.drawString("...", x + PADDING, currentY);
        }
        return currentY;
    }

    public boolean contient(int px, int py) {
        return px >= x && px <= x + largeur
                && py >= y && py <= y + hauteur;
    }

    public String getNom() {
        return nom;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) 
    {
        this.largeur = largeur;
    }

    public int getHauteur()
    {
        return hauteur;
    }

    public boolean estInterface() {
        return estInterface;
    }

    public boolean estSelectionne() {
        return estSelectionne;
    }

    public boolean estExterne() {
        return estExterne;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setInterface(boolean estInterface) {
        this.estInterface = estInterface;
    }

    public void setSelectionne(boolean selectionne) {
        this.estSelectionne = selectionne;
    }

    public void setExterne(boolean externe) {
        this.estExterne = externe;
    }

    public void setAttributs(List<String> attributs) {
        this.attributsAffichage = attributs;
        recalculerDimensions();
    }

    public void setMethodes(List<String> methodes) {
        this.methodesAffichage = methodes;
        recalculerDimensions();
    }

    private void recalculerDimensions() {
        int maxLongueur = nom != null ? nom.length() : 0;

        for (String att : attributsAffichage) {
            if (att == null) {
                continue;
            }

            int longueur = att.replace("{static}", "").trim().length();

            maxLongueur = Math.max(maxLongueur, longueur);
        }

        for (String met : methodesAffichage) {
            if (met == null) {
                continue;
            }

            int longueur = met.replace("{static}", "").trim().length();

            maxLongueur = Math.max(maxLongueur, longueur);
        }

        this.largeur = Math.max(
                200,
                PADDING * 2 + maxLongueur * 8);

        // Calculer hauteur en fonction du mode
        int nbLignesAtt, nbLignesMet;
        if (modeComplet) {
            nbLignesAtt = attributsAffichage.size();
            nbLignesMet = methodesAffichage.size();
        } else {
            // Mode condensé : max 3 attributs + ligne "..." si nécessaire
            int nbAttAffiches = Math.min(attributsAffichage.size(), 3);
            nbLignesAtt = nbAttAffiches + (attributsAffichage.size() > 3 ? 1 : 0);

            // Mode condensé : max 3 méthodes + ligne "..." si nécessaire
            int nbMetAffichees = Math.min(methodesAffichage.size(), 3);
            nbLignesMet = nbMetAffichees + (methodesAffichage.size() > 3 ? 1 : 0);
        }

        this.hauteur = HAUTEUR_ENTETE
                + (nbLignesAtt + nbLignesMet) * HAUTEUR_LIGNE
                + PADDING * 4;
    }
}
