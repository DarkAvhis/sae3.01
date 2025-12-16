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
 * dans un rectangle avec un en-tête coloré. Supporte l'affichage des
 * modificateurs
 * (static) et la sélection interactive.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class BlocClasse
{
    private String nom;

    private int x      ;
    private int y      ;
    private int largeur;
    private int hauteur;

    private boolean estInterface  ;
    private boolean estSelectionne;

    // Champs pour stocker les détails complets et limités
    private List<String> attributsComplets;
    private List<String> methodesCompletes;
    private List<String> attributsAffichage;  // version limitée ou complète selon le mode
    private List<String> methodesAffichage ;  // version limitée ou complète selon le mode
    private boolean enModeCondense = true;    // true = affichage condensé, false = affichage complet

    // Constantes
    private static final int PADDING        = 10;
    private static final int HAUTEUR_ENTETE = 30;
    private static final int HAUTEUR_LIGNE  = 20;
    private static final int MAX_ATTRIBUTS_AFFICHAGE = 3;
    private static final int MAX_METHODES_AFFICHAGE = 3;
    private static final int MAX_PARAMS = 2;

    private static final Color COULEUR_FOND    = new Color(230, 240, 250);
    private static final Color COULEUR_BORDURE = new Color(0  , 0  , 0  );
    private static final Color COULEUR_ENTETE  = new Color(100, 150, 200);

    /**
     * Constructeur principal d'un bloc classe.
     * 
     * Crée un bloc graphique avec calcul automatique des dimensions
     * en fonction du contenu (attributs et méthodes).
     * 
     * @param nom       Nom de la classe
     * @param x         Position X du bloc
     * @param y         Position Y du bloc
     * @param attributs Liste des attributs formatés pour l'affichage
     * @param methodes  Liste des méthodes formatées pour l'affichage
     */
    public BlocClasse(String nom, int x, int y, List<String> attributs, List<String> methodes) 
    {
        this.nom = nom;
        this.x   = x  ;
        this.y   = y  ;
        this.attributsComplets = new ArrayList<>(attributs);
        this.methodesCompletes = new ArrayList<>(methodes);
        
        // Initialiser l'affichage condensé
        mettreAJourAffichageCondense();

        this.estInterface   = false;
        this.estSelectionne = false;
        
        recalculerDimensions();
    }

    /**
     * Constructeur simplifié d'un bloc classe sans contenu.
     * 
     * Crée un bloc vide (sans attributs ni méthodes).
     * 
     * @param nom Nom de la classe
     * @param x   Position X du bloc
     * @param y   Position Y du bloc
     */
    public BlocClasse(String nom, int x, int y) 
    {
        this(nom, x, y, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Dessine le bloc classe dans le contexte graphique.
     * 
     * Affiche le rectangle du bloc avec son en-tête, ses attributs et ses méthodes.
     * Les éléments statiques sont soulignés selon la notation UML.
     * 
     * @param g Le contexte graphique 2D pour le dessin
     */
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

            boolean estStatique = att.contains("{static}");
            String libelle = att.replace(" {static}", "").replace("{static} ", "");

            g.drawString(libelle, x + PADDING, currentY);

            if (estStatique) 
            {
                FontMetrics fmLigne = g.getFontMetrics();

                int underlineY  = currentY + 2;
                int underlineX2 = x + PADDING + fmLigne.stringWidth(libelle);
                g.drawLine(x + PADDING, underlineY, underlineX2, underlineY);
            }
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

            boolean estStatique = met.contains("{static}");
            String libelle      = met.replace(" {static}", "").replace("{static} ", "");

            g.drawString(libelle, x + PADDING, currentY);

            if (estStatique) 
            {
                FontMetrics fmLigne = g.getFontMetrics();
                int underlineY  = currentY + 2;
                int underlineX2 = x + PADDING + fmLigne.stringWidth(libelle);
                g.drawLine(x + PADDING, underlineY, underlineX2, underlineY);
            }
        }

        // 6. Gestion Interface (si besoin)
        if (estInterface) 
        {
            // ... (logique de dessin <<interface>> conservée)
        }
    }

    /**
     * Vérifie si un point est contenu dans le bloc.
     * 
     * Utilisé pour la détection des clics souris sur le bloc.
     * 
     * @param px Coordonnée X du point
     * @param py Coordonnée Y du point
     * @return true si le point est dans le bloc, false sinon
     */
    public boolean contient(int px, int py) 
    {
        return px >= x && px <= x + largeur && 
               py >= y && py <= y + hauteur   ;
    }

    // Getters et Setters
    public String  getNom        () {    return this.nom            ;  }
    public int     getX          () {    return this.x              ;  }
    public int     getY          () {    return this.y              ;  }
    public int     getLargeur    () {    return this.largeur        ;  }
    public int     getHauteur    () {    return this.hauteur        ;  }
    public boolean estInterface  () {    return this.estInterface   ;  }
    public boolean estSelectionne() {    return this.estSelectionne ;  }


    public void setX(int x) {    this.x = x;    }
    public void setY(int y) {    this.y = y;   }
    public void setInterface(boolean estInterface) {    this.estInterface = estInterface;    }
    public void setSelectionne(boolean selectionne) {    this.estSelectionne = selectionne;    }

    public void setAttributs(List<String> attributs) 
    {
        this.attributsComplets = new ArrayList<>(attributs);
        mettreAJourAffichageCondense();
        recalculerDimensions();
    }

    public void setMethodes(List<String> methodes) 
    {
        this.methodesCompletes = new ArrayList<>(methodes);
        mettreAJourAffichageCondense();
        recalculerDimensions();
    }

    /**
     * Limite l'affichage des attributs à 3 et ajoute "..." si nécessaire.
     * Limite aussi les paramètres des méthodes à 2.
     */
    private void mettreAJourAffichageCondense()
    {
        this.attributsAffichage = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_ATTRIBUTS_AFFICHAGE, attributsComplets.size()); i++) {
            this.attributsAffichage.add(attributsComplets.get(i));
        }
        if (attributsComplets.size() > MAX_ATTRIBUTS_AFFICHAGE) {
            this.attributsAffichage.add("...");
        }

        this.methodesAffichage = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_METHODES_AFFICHAGE, methodesCompletes.size()); i++) {
            String methode = methodesCompletes.get(i);
            // Limiter les paramètres à 2
            String methodeLimitee = limiterParametres(methode);
            this.methodesAffichage.add(methodeLimitee);
        }
        if (methodesCompletes.size() > MAX_METHODES_AFFICHAGE) {
            this.methodesAffichage.add("...");
        }
    }

    /**
     * Limite les paramètres d'une méthode à MAX_PARAMS et ajoute "..." si nécessaire.
     * Format: "visibility staticFlag nom(param1, param2, ...)returnType"
     */
    private String limiterParametres(String methode)
    {
        // Chercher les parenthèses pour identifier les paramètres
        int openParen = methode.indexOf('(');
        int closeParen = methode.lastIndexOf(')');
        
        if (openParen == -1 || closeParen == -1) {
            return methode; // Format anormal, retourner tel quel
        }
        
        String avant = methode.substring(0, openParen + 1);
        String params = methode.substring(openParen + 1, closeParen);
        String apres = methode.substring(closeParen);
        
        if (params.trim().isEmpty()) {
            return methode;
        }
        
        String[] paramsArray = params.split(",\\s*");
        if (paramsArray.length > MAX_PARAMS) {
            StringBuilder limited = new StringBuilder();
            for (int i = 0; i < MAX_PARAMS; i++) {
                if (i > 0) limited.append(", ");
                limited.append(paramsArray[i]);
            }
            limited.append(", ...");
            return avant + limited.toString() + apres;
        }
        
        return methode;
    }

    /**
     * Bascule entre le mode condensé et le mode complet.
     */
    public void basculerMode()
    {
        this.enModeCondense = !this.enModeCondense;
        if (this.enModeCondense) {
            mettreAJourAffichageCondense();
        } else {
            this.attributsAffichage = new ArrayList<>(attributsComplets);
            this.methodesAffichage = new ArrayList<>(methodesCompletes);
        }
        recalculerDimensions();
    }

    public boolean estEnModeCondense() {
        return this.enModeCondense;
    }

    public List<String> getAttributsComplets() {
        return new ArrayList<>(attributsComplets);
    }

    public List<String> getMethodesCompletes() {
        return new ArrayList<>(methodesCompletes);
    }

    private void recalculerDimensions() 
    {

        // ---- LARGEUR ----
        int maxLongueur = 0;

        // 1️⃣ Nom de la classe
        if (nom != null) {
            maxLongueur = nom.length();
        }

        // 2️⃣ Attributs
        for (String att : attributsAffichage) {
            if (att == null) continue;

            String texte = att.replace("{static}", "").trim();
            int longueur = texte.length();

            if (longueur > maxLongueur) {
                maxLongueur = longueur;
            }
        }

        // 3️⃣ Méthodes
        for (String met : methodesAffichage) {
            if (met == null) continue;

            String texte = met.replace("{static}", "").trim();
            int longueur = texte.length();

            if (longueur > maxLongueur) {
                maxLongueur = longueur;
            }
        }

        // Conversion caractères → pixels (approximation)
        this.largeur = Math.max(
                200,
                PADDING * 2 + maxLongueur * 8
        );

        // ---- HAUTEUR ----
        this.hauteur = HAUTEUR_ENTETE
                + (attributsAffichage.size() + methodesAffichage.size()) * HAUTEUR_LIGNE
                + PADDING * 4;
    }
}