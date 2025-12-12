package src.vue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension; // Import nécessaire pour Dimension

import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import src.Controleur;
import src.vue.LiaisonVue.TypeLiaison;

public class PanneauDiagramme extends JPanel 
{
    private List<BlocClasse> blocsClasses;
    private List<LiaisonVue> liaisonsVue;

    public PanneauDiagramme(Controleur controleur)
    {
        this.blocsClasses = new ArrayList<>();
        this.liaisonsVue = new ArrayList<>(); 
        
        // Taille minimale pour que le JScrollPane soit utilisable
        this.setPreferredSize(new java.awt.Dimension(1000, 800)); 

        this.setLayout(null);
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createTitledBorder("Diagramme UML"));

        this.ajouterListenersInteraction();
    }


    private class GereSourisInteraction extends MouseAdapter 
    {
        private BlocClasse blocSelectionne = null;
        private int offsetX = 0;
        private int offsetY = 0;

        @Override
        public void mousePressed(MouseEvent e) 
        {
            blocSelectionne = null;

            for (int i = blocsClasses.size() - 1; i >= 0; i--) 
            {
                BlocClasse bloc = blocsClasses.get(i);

                if (bloc.contient(e.getX(), e.getY())) 
                {
                    blocSelectionne = bloc;
                    offsetX = e.getX() - bloc.getX();
                    offsetY = e.getY() - bloc.getY();

                    for (BlocClasse b : blocsClasses) 
                    {
                        b.setSelectionne(false);
                    }

                    bloc.setSelectionne(true);
                    repaint(); 
                    
                    break;
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) 
        {
            if (blocSelectionne != null) 
            {
                blocSelectionne.setX(e.getX() - offsetX);
                blocSelectionne.setY(e.getY() - offsetY);

                calculerTailleDynamique(); // NOUVEAU : Recalcule la taille après déplacement
                repaint(); 
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) 
        {
            blocSelectionne = null;
            offsetX = 0;
            offsetY = 0;
        }
    }

    private void ajouterListenersInteraction() 
    {
        GereSourisInteraction adapter = new GereSourisInteraction();
        this.addMouseListener(adapter); 
        this.addMouseMotionListener(adapter); 
    }

    /**
     * Calcule et définit la taille préférée du panneau pour que le JScrollPane fonctionne,
     * en se basant sur les positions maximales des blocs.
     */
    private void calculerTailleDynamique() 
    {
        int maxX = 0;
        int maxY = 0;
        final int PADDING = 100; // Marge de sécurité

        for (BlocClasse bloc : blocsClasses) 
        {
            // maxX = position du coin supérieur gauche + largeur
            maxX = Math.max(maxX, bloc.getX() + bloc.getLargeur());
            maxY = Math.max(maxY, bloc.getY() + bloc.getHauteur());
        }

        // Maintien d'une taille minimale de 1000x800 pour le panneau vide
        int requiredWidth = Math.max(maxX + PADDING, 1000); 
        int requiredHeight = Math.max(maxY + PADDING, 800); 

        Dimension currentSize = getPreferredSize();

        if (requiredWidth > currentSize.width || requiredHeight > currentSize.height) 
        {
            // Met à jour la taille préférée
            setPreferredSize(new java.awt.Dimension(requiredWidth, requiredHeight));
            
            // Notifie le parent (JScrollPane) que la taille a changé et qu'il doit réévaluer les barres
            revalidate(); 
        }
    }

    // --- LOGIQUE DE DESSIN ---

    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dessinerLiaisons(g2d);

        for (BlocClasse bloc : blocsClasses) 
        {
            bloc.dessiner(g2d);
        }
    }

    private void dessinerLiaisons(Graphics2D g2d) 
    {
        if (liaisonsVue == null || blocsClasses == null) return;

        g2d.setFont(new Font("Arial", Font.PLAIN, 10)); // Police pour les multiplicités

        for (LiaisonVue liaison : liaisonsVue)
        {
            Optional<BlocClasse> blocOrig = blocsClasses.stream()
                .filter(b -> b.getNom().equals(liaison.getNomClasseOrig()))
                .findFirst();
            
            Optional<BlocClasse> blocDest = blocsClasses.stream()
                .filter(b -> b.getNom().equals(liaison.getNomClasseDest()))
                .findFirst();

            if (blocOrig.isPresent() && blocDest.isPresent())
            {
                Point p1 = calculerPointConnexion(blocOrig.get(), blocDest.get()); 
                Point p2 = calculerPointConnexion(blocDest.get(), blocOrig.get()); 
                
                g2d.setColor(Color.BLACK);
                Stroke oldStroke = g2d.getStroke();
                
                switch (liaison.getType())
                {
                    case HERITAGE:
                        dessinerFlecheHeritage(g2d, p1, p2, false); 
                        break;
                    
                    case IMPLEMENTATION:
                        dessinerFlecheHeritage(g2d, p1, p2, true); 
                        break;
                        
                    case ASSOCIATION_UNIDI:
                    case ASSOCIATION_BIDI:
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                        
                        // Multiplicités (seulement pour les associations)
                        if (!liaison.getMultipliciteOrig().isEmpty()) {
                            drawMultiplicity(g2d, p1, p2, liaison.getMultipliciteOrig(), true);
                        }
                        if (!liaison.getMultipliciteDest().isEmpty()) {
                            drawMultiplicity(g2d, p1, p2, liaison.getMultipliciteDest(), false);
                        }
                        
                        if (liaison.getType() == TypeLiaison.ASSOCIATION_UNIDI) {
                            dessinerFlecheSimple(g2d, p1, p2); 
                        }
                        break;
                }
                
                g2d.setStroke(oldStroke); 
            }
        }
    }

    private void drawMultiplicity(Graphics2D g2d, Point pStart, Point pEnd, String multiplicity, boolean isSource) {
        
        Point pAnchor = isSource ? pStart : pEnd;
        
        int offsetFromAnchor = 15; 
        int offsetPerpendicular = 10; 
        
        double angle = Math.atan2(pEnd.y - pStart.y, pEnd.x - pStart.x);
        double perpAngle = angle + Math.PI / 2;
        
        int xLine = (int) (pAnchor.x + offsetFromAnchor * Math.cos(angle));
        int yLine = (int) (pAnchor.y + offsetFromAnchor * Math.sin(angle));
        
        int xText = (int) (xLine + offsetPerpendicular * Math.cos(perpAngle));
        int yText = (int) (yLine + offsetPerpendicular * Math.sin(perpAngle));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(multiplicity);
        
        xText -= (int) (textWidth * 0.5); 
        
        g2d.drawString(multiplicity, xText, yText);
    }
    
    private void dessinerFlecheHeritage(Graphics2D g2d, Point p1, Point p2, boolean isImplementation) 
    {
        int tailleTriangle = 15; 
        
        double angle = Math.atan2(p1.y - p2.y, p1.x - p2.x); 
        
        int x_base = (int) (p2.x + tailleTriangle * Math.cos(angle));
        int y_base = (int) (p2.y + tailleTriangle * Math.sin(angle));
        
        double demiLargeur = tailleTriangle * 0.4;
        double angleFlanc1 = angle + Math.PI / 2;
        double angleFlanc2 = angle - Math.PI / 2;

        int x_lat1 = (int) (x_base + demiLargeur * Math.cos(angleFlanc1));
        int y_lat1 = (int) (y_base + demiLargeur * Math.sin(angleFlanc1));

        int x_lat2 = (int) (x_base + demiLargeur * Math.cos(angleFlanc2));
        int y_lat2 = (int) (y_base + demiLargeur * Math.sin(angleFlanc2));
        
        int[] xPoints = {p2.x, x_lat1, x_lat2};
        int[] yPoints = {p2.y, y_lat1, y_lat2};
        Polygon triangle = new Polygon(xPoints, yPoints, 3);
        
        // 2. Dessin de la ligne (pleine ou pointillée)
        if (isImplementation)
        {
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f));
        } else {
            g2d.setStroke(new BasicStroke(1));
        }
        
        g2d.drawLine(p1.x, p1.y, x_base, y_base);
        
        // 3. Dessin du triangle (vide)
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.WHITE); 
        g2d.fill(triangle);
        g2d.setColor(Color.BLACK); 
        g2d.draw(triangle);
    }

    private void dessinerFlecheSimple(Graphics2D g2d, Point p1, Point p2) 
    {
        int tailleFleche = 10;
        
        double angle = Math.atan2(p1.y - p2.y, p1.x - p2.x);
        
        int x3 = (int) (p2.x + tailleFleche * Math.cos(angle - Math.PI / 6));
        int y3 = (int) (p2.y + tailleFleche * Math.sin(angle - Math.PI / 6));
        
        int x4 = (int) (p2.x + tailleFleche * Math.cos(angle + Math.PI / 6));
        int y4 = (int) (p2.y + tailleFleche * Math.sin(angle + Math.PI / 6));

        g2d.drawLine(p2.x, p2.y, x3, y3);
        g2d.drawLine(p2.x, p2.y, x4, y4);
    }
    
    private Point calculerPointConnexion(BlocClasse blocOrigine, BlocClasse blocCible)
    {
        // ... (Logique inchangée pour calculer le point de connexion)
        int x1 = blocOrigine.getX();
        int y1 = blocOrigine.getY();
        int w1 = blocOrigine.getLargeur();
        int h1 = blocOrigine.getHauteur();
        
        int cX1 = x1 + w1 / 2;
        int cY1 = y1 + h1 / 2;
        int cX2 = blocCible.getX() + blocCible.getLargeur() / 2;
        int cY2 = blocCible.getY() + blocCible.getHauteur() / 2;
        
        double angle = Math.atan2(cY2 - cY1, cX2 - cX1);
        double angleDeg = Math.toDegrees(angle);
        if (angleDeg < 0) angleDeg += 360;

        // Détermination du côté touché
        if ((angleDeg >= 315 && angleDeg <= 360) || (angleDeg >= 0 && angleDeg < 45))
        {
            return new Point(x1 + w1, cY1);
        }
        else if (angleDeg >= 45 && angleDeg < 135)
        {
            return new Point(cX1, y1 + h1);
        }
        else if (angleDeg >= 135 && angleDeg < 225)
        {
            return new Point(x1, cY1);
        }
        else if (angleDeg >= 225 && angleDeg < 315)
        {
            return new Point(cX1, y1);
        }
        
        return new Point(cX1, cY1);
    }


    // --- GETTERS & SETTERS UTILISÉS PAR LE CONTRÔLEUR ---

    public void afficherDiagramme(List<BlocClasse> blocs) 
    {
        this.blocsClasses = blocs;
        // Le repaint est maintenant géré par setLiaisonsVue
    }

    public List<LiaisonVue> getLiaisonsVue() { return liaisonsVue; }

    public void setLiaisonsVue(List<LiaisonVue> liaisonsVue) 
    {
        this.liaisonsVue = liaisonsVue;
        this.repaint();
    }

    public List<BlocClasse> getBlocsClasses() { return blocsClasses; }

    public BlocClasse getBlocsClasseSelectionnee()
    {
        for (BlocClasse bloc : blocsClasses)
        {
            if (bloc.estSelectionne())
                return bloc;
        }
        return null; 
    }    

    // NOUVEAU SETTER pour les BlocsClasses (appelle le calcul de taille)
    public void setBlocsClasses(List<BlocClasse> blocsVue) 
    {
        this.blocsClasses = blocsVue;
        // Met à jour la taille du panneau pour le JScrollPane
        calculerTailleDynamique(); 
    }

    /**
     * Optimise la disposition des blocs en utilisant un algorithme hiérarchique.
     * Les blocs sont arrangés en couches pour minimiser les croisements de liaisons.
     */
    public void optimiserDisposition() 
    {
        if (blocsClasses.isEmpty()) 
        {
            return;
        }
        
        // Appliquer l'algorithme hiérarchique (Sugiyama-style)
        OptimisateurDisposition.appliquerLayoutHierarchique(blocsClasses, liaisonsVue);
        
        // Mettre à jour la taille du panneau et redessiner
        calculerTailleDynamique();
        repaint();
    }

    /**
     * Dispose les blocs en grille régulière.
     */
    public void disposerEnGrille() 
    {
        if (blocsClasses.isEmpty()) {
            return;
        }
        
        OptimisateurDisposition.appliquerLayoutGrille(blocsClasses);
        calculerTailleDynamique();
        repaint();
    }

    /**
     * Dispose les blocs en cercle autour du premier bloc.
     */
    public void disposerEnCirculaire() 
    {
        if (blocsClasses.isEmpty()) {
            return;
        }
        
        OptimisateurDisposition.appliquerLayoutCirculaire(blocsClasses, 0);
        calculerTailleDynamique();
        repaint();
    }
    
}