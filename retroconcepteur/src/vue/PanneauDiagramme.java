package vue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import src.Controleur;

public class PanneauDiagramme extends JPanel 
{
    private List<BlocClasse> blocsClasses;
    private List<LiaisonVue> liaisonsVue;

    public PanneauDiagramme(Controleur controleur)
    {
        this.blocsClasses = new ArrayList<>();
        this.liaisonsVue = new ArrayList<>(); 
        
        // Définit une taille préférée pour que le JScrollPane fonctionne
        this.setPreferredSize(new java.awt.Dimension(2000, 2000));

        this.setLayout(null);
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createTitledBorder("Diagramme UML"));

        this.ajouterListenersInteraction();
    }

    public List<LiaisonVue> getLiaisonsVue() 
    {
        return liaisonsVue;
    }

    public void setLiaisonsVue(List<LiaisonVue> liaisonsVue) 
    {
        this.liaisonsVue = liaisonsVue;
        this.repaint();
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

        for (LiaisonVue liaison : liaisonsVue)
        {
            // Trouver les BlocsClasse correspondants par nom

            Optional<BlocClasse> blocOrig = blocsClasses.stream()
                .filter(b -> b.getNom().equals(liaison.getNomClasseOrig()))
                .findFirst();
            
            Optional<BlocClasse> blocDest = blocsClasses.stream()
                .filter(b -> b.getNom().equals(liaison.getNomClasseDest()))
                .findFirst();

            if (blocOrig.isPresent() && blocDest.isPresent())
            {
                // p1 = Origine de la flèche (Fille)
                Point p1 = calculerPointConnexion(blocOrig.get(), blocDest.get()); 
                // p2 = Destination de la flèche (Mère/Interface)
                Point p2 = calculerPointConnexion(blocDest.get(), blocOrig.get()); 
                
                g2d.setColor(Color.BLACK);
                
                Stroke oldStroke = g2d.getStroke();
                
                switch (liaison.getType())
                {
                    case HERITAGE:
                        dessinerFlecheHeritage(g2d, p1, p2, false); // Ligne pleine
                        break;
                    
                    case IMPLEMENTATION:
                        dessinerFlecheHeritage(g2d, p1, p2, true); // Ligne pointillée
                        break;
                        
                    case ASSOCIATION_UNIDI:
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                        dessinerFlecheSimple(g2d, p1, p2); 
                        break;
                        
                    case ASSOCIATION_BIDI:
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                        break;
                }
                
                g2d.setStroke(oldStroke); 
            }
        }
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
    
    /**
     * Calcule le point sur la bordure du blocOrigine le plus proche du centre du blocCible.
     */
    private Point calculerPointConnexion(BlocClasse blocOrigine, BlocClasse blocCible)
    {
        int x1 = blocOrigine.getX();
        int y1 = blocOrigine.getY();
        int w1 = blocOrigine.getLargeur();
        int h1 = blocOrigine.getHauteur();
        
        // Centres
        int cX1 = x1 + w1 / 2;
        int cY1 = y1 + h1 / 2;
        int cX2 = blocCible.getX() + blocCible.getLargeur() / 2;
        int cY2 = blocCible.getY() + blocCible.getHauteur() / 2;
        
        // Angle entre les centres
        double angle = Math.atan2(cY2 - cY1, cX2 - cX1);
        double angleDeg = Math.toDegrees(angle);
        if (angleDeg < 0) angleDeg += 360;

        // Détermination du côté touché
        
        // Droite (0 / 360)
        if ((angleDeg >= 315 && angleDeg <= 360) || (angleDeg >= 0 && angleDeg < 45))
        {
            return new Point(x1 + w1, cY1);
        }
        // Bas (90)
        else if (angleDeg >= 45 && angleDeg < 135)
        {
            return new Point(cX1, y1 + h1);
        }
        // Gauche (180)
        else if (angleDeg >= 135 && angleDeg < 225)
        {
            return new Point(x1, cY1);
        }
        // Haut (270)
        else if (angleDeg >= 225 && angleDeg < 315)
        {
            return new Point(cX1, y1);
        }
        
        // Cas de repli (centre)
        return new Point(cX1, cY1);
    }


    public void afficherDiagramme(List<BlocClasse> blocs) 
    {
        this.blocsClasses = blocs;
        this.repaint(); 
    }

    public List<BlocClasse> getBlocsClasses() {    return blocsClasses;   }

    public BlocClasse getBlocsClasseSelectionnee()
    {
        for (BlocClasse bloc : blocsClasses)
        {
            if (bloc.estSelectionne())
                return bloc;
        }
        return null; 
    }    

    public void setBlocsClasses(List<BlocClasse> blocsClasses) 
    {
        this.blocsClasses = blocsClasses;
        this.repaint(); 
    }
}