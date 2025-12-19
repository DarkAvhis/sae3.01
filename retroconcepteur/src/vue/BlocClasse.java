// File: vue/BlocClasse.java
package vue;

import java.awt.BasicStroke;
import java.awt.Canvas;
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
    // Ajout d'un champ pour le type spécifique (interface, record, abstract class)
    private String  typeSpecifique = null;
    private boolean modeComplet    = false;

    private String  nom           ;
    private int     x             ;
    private int     y             ;
    private int     largeur       ;
    private int     hauteur       ;
    private boolean estInterface  ;
    private boolean estSelectionne;
    private boolean estExterne    ; // NOUVEAU: Champ pour marquer une classe comme externe

    private List<String> attributsAffichage;
    private List<String> methodesAffichage ;

    private static final int PADDING        = 10;
    private static final int HAUTEUR_ENTETE = 30;
    private static final int HAUTEUR_LIGNE  = 20;

    private static final Color COULEUR_FOND         = new Color(255, 255, 255);
    private static final Color COULEUR_FOND_EXTERNE = new Color(235, 235, 235);
    private static final Color COULEUR_BORDURE      = new Color(0, 0, 0);

    public BlocClasse(String nom, int x, int y, List<String> attributs, List<String> methodes)
    {
        this.nom = nom;
        this.x   = x  ;
        this.y   = y  ;

        this.attributsAffichage = attributs;
        this.methodesAffichage  = methodes ;

        this.estInterface   = false;
        this.estSelectionne = false;
        this.estExterne     = false; // Initialisation du nouveau champ

        // Calcul précis de la largeur du nom et du sous-titre avec la police réelle
        Font fontNom            = new Font("Arial", Font.BOLD  , 12);
        Font fontSousTitre      = new Font("Arial", Font.ITALIC, 11);
        Canvas c                = new Canvas();
        FontMetrics fmNom       = c.getFontMetrics(fontNom      );
        FontMetrics fmSousTitre = c.getFontMetrics(fontSousTitre);

        int largeurNom       = fmNom.stringWidth(this.nom);
        int largeurSousTitre = 0;
        
        if (this.typeSpecifique != null && !this.typeSpecifique.isEmpty()) 
            largeurSousTitre = fmSousTitre.stringWidth("<<" + this.typeSpecifique + ">>");

        else if (this.estInterface) 
            largeurSousTitre = fmSousTitre.stringWidth("<<interface>>");

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
            BlocClasse.PADDING * 2 + Math.max(maxLgEntete, Math.max(maxLgAttributs, maxLgMethodes)));

        this.hauteur = BlocClasse.HAUTEUR_ENTETE
                + (attributs.size() + methodes.size()) * BlocClasse.HAUTEUR_LIGNE
                + BlocClasse.PADDING * 4;

        recalculerDimensions();
    }

    public BlocClasse(String nom, int x, int y)
    {
        this(nom, x, y, new ArrayList<>(), new ArrayList<>());
    }

    public String getNom           () {  return this.nom           ; }
    public int    getX             () {  return this.x             ; }
    public int    getY             () {  return this.y             ; }
    public int    getLargeur       () {  return this.largeur       ; }
    public int    getHauteur       () {  return this.hauteur       ; }
    public String getTypeSpecifique() {  return this.typeSpecifique; }

    public void setLargeur       (int     largeur     ) {  this.largeur        = largeur     ; }
    public void setX             (int     x           ) {  this.x              = x           ; }
    public void setY             (int     y           ) {  this.y              = y           ; }
    public void setInterface     (boolean estInterface) {  this.estInterface   = estInterface; }
    public void setSelectionne   (boolean selectionne ) {  this.estSelectionne = selectionne ; }
    public void setExterne       (boolean externe     ) {  this.estExterne     = externe     ; }
    public void setTypeSpecifique(String  type        ) {  this.typeSpecifique = type        ; }

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

    public void setModeComplet(boolean complet)
    {
        this.modeComplet = complet;
        recalculerDimensions();
    }

    public boolean estInterface  () {  return this.estInterface  ;  }
    public boolean estSelectionne() {  return this.estSelectionne;  }
    public boolean estExterne    () {  return this.estExterne    ;  }
    public boolean isModeComplet () {  return this.modeComplet   ;  }
    
    public void dessiner(Graphics2D g)
    {
        dessinerFondEtBordure(g);
        int currentY = dessinerNom(g);
        
        // Appel des méthodes de dessin internes

        currentY = dessinerAttributs (g, currentY);
        currentY = dessinerSeparateur(g, currentY);
        currentY = dessinerMethodes  (g, currentY);

    }

    public boolean contient(int px, int py)
    {
        return px >= this.x && px <= this.x + this.largeur && py >= this.y && py <= this.y + this.hauteur;
    }

    /**
     * Limite l'affichage des paramètres d'une méthode à 2 paramètres.
     * Si plus de 2 paramètres, affiche les 2 premiers suivis de "..."
     */
    private String limiterParametres(String methode)
    {
        int idxParenthese = methode.indexOf('(');

        if (idxParenthese < 0) return methode;

        int idxFermante = methode.lastIndexOf(')');

        if (idxFermante < 0) return methode;

        String avant  = methode.substring(0, idxParenthese + 1);
        String params = methode.substring(idxParenthese + 1, idxFermante);
        String apres  = methode.substring(idxFermante);

        if (params.trim().isEmpty()) return methode;

        String[] paramsTab = params.split(",");

        if (paramsTab.length <= 2) return methode;

        return avant + paramsTab[0].trim() + ", " + paramsTab[1].trim() + ", ..." + apres;
    }


    // Affiche le fond et la bordure du bloc
    private void dessinerFondEtBordure(Graphics2D g)
    {
        // Choix de la couleur de fond selon que la classe est externe ou non
        g.setColor(this.estExterne ? BlocClasse.COULEUR_FOND_EXTERNE : BlocClasse.COULEUR_FOND);
        g.fillRect(this.x, this.y, this.largeur, this.hauteur);
        
        // Dessin de la bordure (en bleu si sélectionnée)
        g.setColor (Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawRect (this.x, this.y, this.largeur, this.hauteur);
    }

    private int dessinerNom(Graphics2D g) 
    {
        // On considère qu'il y a un type si le string est rempli OU si le flag estInterface est vrai
        String labelType = null;

        if (this.typeSpecifique != null && !this.typeSpecifique.isEmpty()) 
            labelType = "<<" + this.typeSpecifique + ">>";

        else if (this.estInterface) 
            labelType = "<<interface>>";

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fmNom = g.getFontMetrics();
        int nomY = this.y + BlocClasse.HAUTEUR_ENTETE - (BlocClasse.HAUTEUR_ENTETE - fmNom.getAscent()) / 2;
        g.drawString(this.nom, this.x + (this.largeur - fmNom.stringWidth(this.nom)) / 2, nomY);

        int dernierTexteY = nomY;

        if (labelType != null) 
        {
            g.setFont(new Font("Arial", Font.ITALIC, 11));
            FontMetrics fmSous = g.getFontMetrics();
            int sousY = nomY + fmSous.getHeight();
            g.drawString(labelType, this.x + (this.largeur - fmSous.stringWidth(labelType)) / 2, sousY);
            dernierTexteY = sousY;
        }

        int separateurY = dernierTexteY + 4;
        g.drawLine(this.x, separateurY, this.x + this.largeur, separateurY);
        return separateurY + BlocClasse.PADDING;
    }


    private int dessinerAttributs(Graphics2D g, int currentY)
    {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        int maxAttributs = this.modeComplet ? Integer.MAX_VALUE : 3;
        int iAtt = 0;
        
        for (String att : this.attributsAffichage)
        {
            if (iAtt >= maxAttributs) break;
            currentY += BlocClasse.HAUTEUR_LIGNE;
            
            boolean estStatique = att.contains("{static}");
            String libelle = att.replace("{static}", "").trim();
            
            g.drawString(libelle, this.x + BlocClasse.PADDING, currentY);
            
            if (estStatique)
            {
                FontMetrics fm = g.getFontMetrics();
                g.drawLine(this.x + BlocClasse.PADDING, currentY + 2, this.x + BlocClasse.PADDING + fm.stringWidth(libelle), currentY + 2);
            }
            iAtt++;
        }
        
        if (!this.modeComplet && this.attributsAffichage.size() > maxAttributs)
        {
            currentY += BlocClasse.HAUTEUR_LIGNE;
            g.drawString("...", this.x + BlocClasse.PADDING, currentY);
        }
        return currentY;
    }

    // Affiche le séparateur et retourne la position Y courante
    private int dessinerSeparateur(Graphics2D g, int currentY)
    {
        currentY += BlocClasse.PADDING / 2;
        g.setColor(BlocClasse.COULEUR_BORDURE);
        g.drawLine(this.x, currentY, this.x + this.largeur, currentY);
        currentY += BlocClasse.PADDING;
        return currentY;
    }

    // Affiche les méthodes et retourne la position Y finale
    private int dessinerMethodes(Graphics2D g, int currentY) 
    {
        int maxMethodes = this.modeComplet ? Integer.MAX_VALUE : 3;
        int iMet = 0;
        
        for (String met : this.methodesAffichage) 
        {
            if (iMet >= maxMethodes) break;
            currentY += BlocClasse.HAUTEUR_LIGNE;
            
            boolean estStatique = met.contains("{static}");
            String libelle = met.replace("{static}", "").trim();
            
            // CORRECTION : On ne limite QUE si on n'est PAS en mode complet
            String libelleFinal = this.modeComplet ? libelle : limiterParametres(libelle);
            
            g.drawString(libelleFinal, this.x + BlocClasse.PADDING, currentY);
            
            if (estStatique) 
            {
                FontMetrics fm = g.getFontMetrics();
                g.drawLine(this.x + BlocClasse.PADDING, currentY + 2, this.x + BlocClasse.PADDING + fm.stringWidth(libelleFinal), currentY + 2);
            }
            iMet++;
        }
        
        if (!this.modeComplet && this.methodesAffichage.size() > maxMethodes)
        {
            currentY += BlocClasse.HAUTEUR_LIGNE;
            g.drawString("...", this.x + BlocClasse.PADDING, currentY);
        }
        return currentY;
    }

    private void recalculerDimensions()
    {
        int maxLongueur = this.nom != null ? this.nom.length() : 0;

        for (String att : this.attributsAffichage)
        {
            if (att == null) continue;

            int longueur = att.replace("{static}", "").trim().length();

            maxLongueur = Math.max(maxLongueur, longueur);
        }

        for (String met : this.methodesAffichage)
        {
            if (met == null) continue;

            String s = met.replace("{static}", "").trim();
            
            // On calcule la largeur basée sur ce qui sera RÉELLEMENT affiché
            String aMesurer = this.modeComplet ? s : limiterParametres(s);
            maxLongueur = Math.max(maxLongueur, aMesurer.length());
        }

        this.largeur = Math.max(
                200,
                BlocClasse.PADDING * 2 + maxLongueur * 8);

        // Calculer hauteur en fonction du mode
        int nbLignesAtt, nbLignesMet;
        if (this.modeComplet)
        {
            nbLignesAtt = this.attributsAffichage.size();
            nbLignesMet = this.methodesAffichage .size();
        }
        else
        {
            // Mode condensé : max 3 attributs + ligne "..." si nécessaire
            int nbAttAffiches = Math.min(this.attributsAffichage.size(), 3);
            nbLignesAtt = nbAttAffiches + (this.attributsAffichage.size() > 3 ? 1 : 0);

            // Mode condensé : max 3 méthodes + ligne "..." si nécessaire
            int nbMetAffichees = Math.min(this.methodesAffichage.size(), 3);
            nbLignesMet = nbMetAffichees + (this.methodesAffichage.size() > 3 ? 1 : 0);
        }

        this.hauteur = BlocClasse.HAUTEUR_ENTETE
                + (nbLignesAtt + nbLignesMet) * BlocClasse.HAUTEUR_LIGNE
                + BlocClasse.PADDING * 4;
    }
}