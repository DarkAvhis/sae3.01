package vue;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau de droite affichant le diagramme UML.
 * Permet de visualiser et manipuler les classes et interfaces du projet.
 */
public class PanneauDiagramme extends JPanel {

    private List<BlocClasse> blocsClasses;
    private String cheminProjetCourant;
    private Point pointDernier;

    public PanneauDiagramme() {
        this.blocsClasses = new ArrayList<>();
        this.cheminProjetCourant = null;

        setLayout(null); // Layout libre pour placer les blocs où on veut
        setBackground(new Color(255, 255, 255));
        setBorder(BorderFactory.createTitledBorder("Diagramme UML"));

        // Ajouter des listeners pour les interactions
        ajouterListenersInteraction();
    }

    /**
     * Charge un projet et affiche ses classes.
     */
    public void chargerProjet(String cheminProjet) {
        this.cheminProjetCourant = cheminProjet;
        blocsClasses.clear();

        File dossier = new File(cheminProjet);
        if (!dossier.exists() || !dossier.isDirectory()) {
            repaint();
            return;
        }

        // Charger les fichiers .java du projet
        File[] fichiersJava = dossier.listFiles((dir, name) -> name.endsWith(".java"));

        if (fichiersJava != null) {
            int x = 50;
            int y = 50;

            for (File fichier : fichiersJava) {
                // Créer un bloc pour chaque classe
                BlocClasse bloc = new BlocClasse(
                    fichier.getName().replace(".java", ""),
                    x, y
                );
                blocsClasses.add(bloc);

                x += 250;
                if (x > getWidth() - 250) {
                    x = 50;
                    y += 150;
                }
            }
        }

        repaint();
    }

    /**
     * Ajoute les listeners pour l'interaction avec les blocs.
     */
    private void ajouterListenersInteraction() {
        // À implémenter : drag & drop, sélection, etc.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dessiner les blocs
        for (BlocClasse bloc : blocsClasses) {
            bloc.dessiner(g2d);
        }

        // Dessiner les liens (à implémenter)
        dessinerLiens(g2d);
    }

    /**
     * Dessine les liens entre les classes.
     * À implémenter selon les associations du modèle.
     */
    private void dessinerLiens(Graphics2D g2d) {
        // Placeholder pour les liens
    }

    public List<BlocClasse> getBlocsClasses() {
        return blocsClasses;
    }

    public String getCheminProjetCourant() {
        return cheminProjetCourant;
    }
}
