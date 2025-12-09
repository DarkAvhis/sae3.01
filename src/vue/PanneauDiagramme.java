package vue;

<<<<<<< HEAD
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
=======
import java.awt.*;
>>>>>>> ebf89f46e9ca1d2ddb42b6a5ce00d39e81e57b14
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PanneauDiagramme extends JPanel 
{

    private List<BlocClasse> blocsClasses;
    private String cheminProjetCourant;

    public PanneauDiagramme() {
        this.blocsClasses = new ArrayList<>();
        this.cheminProjetCourant = null;

<<<<<<< HEAD
        this.setLayout(null);
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createTitledBorder("Diagramme UML"));
=======
        setLayout(null); // pour placer les blocs où on veut
        setBackground(new Color(255, 255, 255));
        setBorder(BorderFactory.createTitledBorder("Diagramme UML"));
>>>>>>> ebf89f46e9ca1d2ddb42b6a5ce00d39e81e57b14

        // Ajouter des listeners pour les interactions
        this.ajouterListenersInteraction();
    }

    public void chargerProjet(String cheminProjet) {
        this.cheminProjetCourant = cheminProjet;
        blocsClasses.clear();

        File dossier = new File(cheminProjet);
        if (!dossier.exists() || !dossier.isDirectory()) {
            repaint();
            return;
        }

        // Charger les .java du projet
        File[] fichiersJava = dossier.listFiles((dir, name) -> name.endsWith(".java"));

        if (fichiersJava != null) 
        {
            int x = 50;
            int y = 50;

            for (File fichier : fichiersJava) 
            {
                BlocClasse bloc = new BlocClasse(
                        fichier.getName().replace(".java", ""),
                        x, y);
                blocsClasses.add(bloc);

                x += 250;
                if (x > getWidth() - 250) 
                {
                    x = 50;
                    y += 200;
                }
            }
        }
        repaint();
    }

    private class GereSourisInteraction extends MouseAdapter 
    {
        private BlocClasse blocSelectionne = null;
        private int offsetX = 0;
        private int offsetY = 0;

        @Override
        public void mousePressed(MouseEvent e) 
        {
            // Supposons que 'blocsClasses' est un champ de la classe englobante
            // et qu'on y accède directement.
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
                    // Supposons que 'repaint()' est une méthode de la classe englobante (e.g.
                    // JPanel)
                    repaint();

                    // On a trouvé un bloc, on peut s'arrêter
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

            // Optionnel : si vous voulez sauvegarder la position du bloc relâché
            // Vous pourriez appeler une méthode du contrôleur ici.
            // Exemple : PanelGraphe.this.ctrl.sauvegarderEtatAvantModification();
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
