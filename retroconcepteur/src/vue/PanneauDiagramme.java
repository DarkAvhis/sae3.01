package vue;

import controleur.Controleur;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JViewport;
import vue.LiaisonVue.TypeLiaison;

/**
 * Panneau d'affichage du diagramme UML.
 * * Ce panneau gère l'affichage graphique des classes (BlocClasse) et de leurs
 * liaisons
 * (héritage, implémentation, associations). Il supporte l'interaction
 * utilisateur
 * pour déplacer les blocs et ajuste dynamiquement sa taille pour le défilement.
 * * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 * BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class PanneauDiagramme extends JPanel implements MouseWheelListener
{
    private List<BlocClasse> blocsClasses;
    private List<LiaisonVue> liaisonsVue;
    private double zoom;

    /**
     * Constructeur du panneau de diagramme.
     * * Initialise le panneau avec une taille minimale et configure les listeners
     * pour l'interaction utilisateur (déplacement des blocs).
     * * @param controleur Le contrôleur principal de l'application
     */
    public PanneauDiagramme(Controleur controleur)
    {
        this.blocsClasses = new ArrayList<>();
        this.liaisonsVue  = new ArrayList<>();
        this.zoom = 1.0;
        // Taille minimale pour que le JScrollPane soit utilisable
        this.setPreferredSize(new java.awt.Dimension(1000, 800));

        this.setLayout(null);
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createTitledBorder("Diagramme UML"));

        this.ajouterListenersInteraction();
        this.addMouseWheelListener(this);

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e.isControlDown() ) 
        { 
            double delta = 0.1;
            if (e.getWheelRotation() < 0)
            {
                setZoom(zoom + delta); // Zoom avant
            } else {
                setZoom(zoom - delta); // Zoom arrière
            }
        }
        else if (e.isShiftDown()) // Shift + molette => décalage horizontal
        {
            
            if (getParent() instanceof JViewport viewport) 
            {
                Point pos = viewport.getViewPosition();
                pos.x += e.getWheelRotation() * 100; // 20 pixels par "tick"
                pos.x = Math.max(0, pos.x);
                pos.x = Math.min(pos.x, getWidth() - viewport.getWidth());
                viewport.setViewPosition(pos);
            }
        }
        else // Molette normale => défilement vertical
        {
            if (getParent() instanceof JViewport viewport) 
            {
                Point pos = viewport.getViewPosition();
                pos.y += e.getWheelRotation() * 100; // 20 pixels par "tick"
                pos.y = Math.max(0, pos.y);
                pos.y = Math.min(pos.y, getHeight() - viewport.getHeight());
                viewport.setViewPosition(pos);
            }
        }
    }


    /*
        nouvelle méthode permettant de netoyer le panneauDiagramme
        et qui est ensuite appeler dans panneauProjet pour que 
        lorque l'on appuie sur le bouton actualiser cela actualise 
        aussi panneauDiagramme
    */
    public void clearDiagram() 
    {
        this.blocsClasses.clear(); // permet de nettoyer les blocs de classe
        this.liaisonsVue .clear(); // permet de nettoyer les liaisons des classes
        this.repaint();
    }

    public double getZoom() {
        return this.zoom;
    }

    public void setZoom(double zoom) {
        // Limiter le zoom entre 0.2x et 5x
        this.zoom = Math.max(0.2, Math.min(zoom, 5.0));
        calculerTailleDynamique(); // Ajuste la taille du panneau selon le zoom
        repaint();
    }


    /**
     * Gestionnaire d'événements souris pour l'interaction avec les blocs.
     * * Permet de sélectionner et déplacer les blocs de classes dans le diagramme.
     */
    private class GereSourisInteraction extends MouseAdapter
    {
        private BlocClasse blocSelectionne = null;
        private int        offsetX         = 0   ;
        private int        offsetY         = 0   ;

        @Override
        public void mousePressed(MouseEvent e)
        {
            blocSelectionne = null;
            int mouseX = (int)(e.getX() / zoom);
            int mouseY = (int)(e.getY() / zoom);
            for (int i = blocsClasses.size() - 1; i >= 0; i--) 
            {
                BlocClasse bloc = blocsClasses.get(i);
                if (bloc.contient(mouseX, mouseY)) 
                {
                    if (e.getButton() == MouseEvent.BUTTON3) 
                    {
                        bloc.setModeComplet(true);
                        bloc.setSelectionne(true);

                        repaint();
                        return;
                    }

                    blocSelectionne = bloc;
                    offsetX = mouseX - bloc.getX();
                    offsetY = mouseY - bloc.getY();
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
                int mouseX = (int)(e.getX() / zoom);
                int mouseY = (int)(e.getY() / zoom);

                blocSelectionne.setX(mouseX - offsetX);
                blocSelectionne.setY(mouseY - offsetY);

                calculerTailleDynamique();
                repaint();
            }
        }
        @Override
        public void mouseReleased(MouseEvent e)
        {
            // Si clic droit relâché, repasser en mode condensé
            if (e.getButton() == MouseEvent.BUTTON3) 
            {
                for (BlocClasse bloc : blocsClasses) 
                {
                    if (bloc.isModeComplet()) 
                    {
                        bloc.setModeComplet(false);
                        repaint();
                    }
                }
            }
            blocSelectionne = null;
            offsetX         = 0   ;
            offsetY         = 0   ;
        }
    }

    private void ajouterListenersInteraction()
    {
        GereSourisInteraction adapter = new GereSourisInteraction();
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }

    /**
     * Calcule et définit la taille préférée du panneau pour le JScrollPane.
     * * Ajuste automatiquement la taille du panneau en fonction des positions
     * et dimensions des blocs pour permettre un défilement adéquat.
     */
    private void calculerTailleDynamique()
    {

        int maxX = 0;
        int maxY = 0;
        final int PADDING = 100;

        for (BlocClasse bloc : blocsClasses) {
            maxX = Math.max(maxX, bloc.getX() + bloc.getLargeur());
            maxY = Math.max(maxY, bloc.getY() + bloc.getHauteur());
        }

        int requiredWidth  = (int)Math.max(maxX * zoom + PADDING, 1000);
        int requiredHeight = (int)Math.max(maxY * zoom + PADDING, 800) ;

        Dimension currentSize = getPreferredSize();
        if (requiredWidth > currentSize.width || requiredHeight > currentSize.height) {
            setPreferredSize(new Dimension(requiredWidth, requiredHeight));
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

        // Appliquer le zoom
        g2d.scale(zoom, zoom);

        dessinerLiaisons(g2d);

        for (BlocClasse bloc : blocsClasses) {
            bloc.dessiner(g2d);
        }
    }

    /**
     * Dessine toutes les liaisons entre les classes.
     * * Affiche les flèches, traits et multiplicités selon le type de liaison
     * (héritage, implémentation, association).
     * * @param g2d Contexte graphique 2D
     */
    private void dessinerLiaisons(Graphics2D g2d)
    {
        if (liaisonsVue == null || blocsClasses == null)
        {
            return;
        }

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
                        if (!liaison.getMultipliciteOrig().isEmpty())
                        {
                            dessinerMultiplicite(g2d, p1, p2, liaison.getMultipliciteOrig(), true);
                        }
                        if (!liaison.getMultipliciteDest().isEmpty())
                        {
                            dessinerMultiplicite(g2d, p1, p2, liaison.getMultipliciteDest(), false);
                        }

                        if (liaison.getType() == TypeLiaison.ASSOCIATION_UNIDI)
                        {
                            dessinerFlecheSimple(g2d, p1, p2);
                        }
                        break;
                }

                g2d.setStroke(oldStroke);
            }
        }
    }

    /**
     * Affiche la multiplicité d'une association.
     * * @param g2d          Contexte graphique 2D
     * @param pStart       Point de départ de la liaison
     * @param pEnd         Point d'arrivée de la liaison
     * @param multiplicity Texte de la multiplicité (ex: "1..*", "0..1")
     * @param isSource     true pour la multiplicité source, false pour la
     * destination
     */
    private void dessinerMultiplicite(Graphics2D g2d, Point pStart, Point pEnd, String multiplicity, boolean isSource)
    {
        Point pAnchor = isSource ? pStart : pEnd;

        int offsetFromAnchor    = 25; // plus loin de la flèche
        int offsetPerpendicular = 16; // plus loin sur le côté

        double angle = Math.atan2(pEnd.y - pStart.y, pEnd.x - pStart.x);
        double perpAngle = angle + Math.PI / 2;

        int xLine = (int) (pAnchor.x + offsetFromAnchor * Math.cos(angle));
        int yLine = (int) (pAnchor.y + offsetFromAnchor * Math.sin(angle));

        int xText = (int) (xLine + offsetPerpendicular * Math.cos(perpAngle));
        int yText = (int) (yLine + offsetPerpendicular * Math.sin(perpAngle));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth  = fm.stringWidth(multiplicity);
        int textHeight = fm.getHeight();

        xText -= (int) (textWidth * 0.5);
        yText += (int) (textHeight * 0.3); // ajuster pour centrer verticalement

        // Dessiner un fond blanc sous la multiplicité pour la rendre toujours lisible
        Color oldColor = g2d.getColor();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(xText - 2, yText - fm.getAscent(), textWidth + 4, textHeight);
        g2d.setColor(oldColor);

        g2d.drawString(multiplicity, xText, yText);
    }

    /**
     * Dessine une flèche d'héritage ou d'implémentation.
     * * Affiche un triangle vide à l'extrémité avec une ligne pleine (héritage)
     * ou pointillée (implémentation).
     * * @param g2d              Contexte graphique 2D
     * @param p1               Point de départ (classe enfant)
     * @param p2               Point d'arrivée (classe parent/interface)
     * @param isImplementation true pour implémentation, false pour héritage
     */
    private void dessinerFlecheHeritage(Graphics2D g2d, Point p1, Point p2, boolean isImplementation)
    {
        int tailleTriangle = 15;

        double angle = Math.atan2(p1.y - p2.y, p1.x - p2.x);

        int x_base = (int) (p2.x + tailleTriangle * Math.cos(angle));
        int y_base = (int) (p2.y + tailleTriangle * Math.sin(angle));

        double demiLargeur = tailleTriangle * 0.4;
        double angleFlanc1 = angle + Math.PI / 2 ;
        double angleFlanc2 = angle - Math.PI / 2 ;

        int x_lat1 = (int) (x_base + demiLargeur * Math.cos(angleFlanc1));
        int y_lat1 = (int) (y_base + demiLargeur * Math.sin(angleFlanc1));

        int x_lat2 = (int) (x_base + demiLargeur * Math.cos(angleFlanc2));
        int y_lat2 = (int) (y_base + demiLargeur * Math.sin(angleFlanc2));

        int[] xPoints = { p2.x, x_lat1, x_lat2 };
        int[] yPoints = { p2.y, y_lat1, y_lat2 };
        Polygon triangle = new Polygon(xPoints, yPoints, 3);

        // 2. Dessin de la ligne (pleine ou pointillée)
        if (isImplementation)
        {
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                    new float[] { 4.0f, 4.0f }, 0.0f));
        }
        else
        {
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

    /**
     * Dessine une flèche simple pour les associations unidirectionnelles.
     * * @param g2d Contexte graphique 2D
     * @param p1  Point de départ
     * @param p2  Point d'arrivée
     */
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
     * Calcule le point de connexion optimal sur le bord d'un bloc.
     * * Détermine le côté du bloc (haut, bas, gauche, droite) le plus proche
     * de la cible pour tracer la liaison.
     * * @param blocOrigine Bloc de départ
     * @param blocCible   Bloc ciblé
     * @return Point de connexion sur le bord du bloc origine
     */
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
        if (angleDeg < 0)
        {
            angleDeg += 360;
        }

        // Détermination du côté touché
        if ((angleDeg >= 315 && angleDeg <= 360) || (angleDeg >= 0 && angleDeg < 45))
        {
            return new Point(x1 + w1, cY1);
        }
        else if (angleDeg >= 45  && angleDeg < 135){return new Point(cX1, y1 + h1);}
        else if (angleDeg >= 135 && angleDeg < 225){return new Point(x1 , cY1    );}
        else if (angleDeg >= 225 && angleDeg < 315){return new Point(cX1, y1     );}

        return new Point(cX1, cY1);
    }

    // --- GETTERS & SETTERS UTILISÉS PAR LE CONTRÔLEUR ---

    /**
     * Affiche une liste de blocs dans le diagramme.
     * * @param blocs Liste des blocs à afficher
     */
    public void afficherDiagramme(List<BlocClasse> blocs)
    {
        this.blocsClasses = blocs;
        // Le repaint est maintenant géré par setLiaisonsVue
    }

    public List<LiaisonVue> getLiaisonsVue(){return liaisonsVue;}

    public void setLiaisonsVue(List<LiaisonVue> liaisonsVue)
    {
        this.liaisonsVue = liaisonsVue;
        this.repaint();
    }

    public List<BlocClasse> getBlocsClasses(){return blocsClasses;}

    public BlocClasse getBlocsClasseSelectionnee()
    {
        for (BlocClasse bloc : blocsClasses)
        {
            if (bloc.estSelectionne())
            {
                return bloc;
            }
        }
        return null;
    }

    /**
     * Définit la liste des blocs à afficher et recalcule la taille du panneau.
     * * @param blocsVue Liste des blocs de classes à afficher
     */
    public void setBlocsClasses(List<BlocClasse> blocsVue)
    {
        this.blocsClasses = blocsVue;
        // Met à jour la taille du panneau pour le JScrollPane
        calculerTailleDynamique();
    }

    /**
     * Optimise la disposition des blocs en utilisant un algorithme hiérarchique.
     * * Les blocs sont arrangés en couches pour minimiser les croisements de
     * liaisons.
     * Met à jour la taille du panneau et red essine le diagramme.
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
     * * Organisation en lignes et colonnes avec espacement uniforme.
     */
    public void disposerEnGrille()
    {
        if (blocsClasses.isEmpty())
        {
            return;
        }

        OptimisateurDisposition.appliquerLayoutGrille(blocsClasses);
        calculerTailleDynamique();
        repaint();
    }

    /**
     * Dispose les blocs en cercle autour du premier bloc.
     * * Utile pour visualiser les relations centrées sur une classe principale.
     */
    public void disposerEnCirculaire()
    {
        if (blocsClasses.isEmpty())
        {
            return;
        }

        OptimisateurDisposition.appliquerLayoutCirculaire(blocsClasses, 0);
        calculerTailleDynamique();
        repaint();
    }
}