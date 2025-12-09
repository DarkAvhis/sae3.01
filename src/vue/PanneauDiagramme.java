package vue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PanneauDiagramme extends JPanel 
{

    private List<BlocClasse> blocsClasses;
    private String cheminProjetCourant;

    public PanneauDiagramme() {
        this.blocsClasses = new ArrayList<>();
        this.cheminProjetCourant = null;

        this.setLayout(null);
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createTitledBorder("Diagramme UML"));

        // Ajouter des listeners pour les interactions
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

            // Parcourir de la fin vers le début pour sélectionner le bloc le plus "en
            // avant"
            for (int i = blocsClasses.size() - 1; i >= 0; i--) 
            {
                BlocClasse bloc = blocsClasses.get(i);

                // Supposons que 'contient(x, y)' est une méthode de BlocClasse
                if (bloc.contient(e.getX(), e.getY())) 
                {
                    blocSelectionne = bloc;
                    offsetX = e.getX() - bloc.getX();
                    offsetY = e.getY() - bloc.getY();

                    // Désélectionner tous les autres blocs
                    for (BlocClasse b : blocsClasses) 
                    {
                        b.setSelectionne(false);
                    }

                    // Sélectionner le bloc actuel
                    bloc.setSelectionne(true);
                    repaint();
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
                
                // Redessiner pour voir le déplacement
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) 
        {
            // Réinitialiser les variables de sélection
            blocSelectionne = null;
            offsetX = 0;
            offsetY = 0;
        }
    }

    // Nouvelle méthode pour installer les listeners
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

        // Dessiner les blocs
        for (BlocClasse bloc : blocsClasses) 
        {
            bloc.dessiner(g2d);
        }

        // Dessiner les liaisons
        dessinerLiaisons(g2d);
    }

    private void dessinerLiaisons(Graphics2D g2d) 
    {

    }

    public List<BlocClasse> getBlocsClasses() 
    {
        return blocsClasses;
    }

    public String getCheminProjetCourant() 
    {
        return cheminProjetCourant;
    }
}
