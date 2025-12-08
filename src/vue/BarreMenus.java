package vue;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class BarreMenus extends JMenuBar {

    public BarreMenus() {
        add(creerMenuFichier());
        add(creerMenuEdition());
        add(creerMenuAffichage());
        add(creerMenuAide());
    }

    private JMenu creerMenuFichier() {
        JMenu menu = new JMenu("Fichier");

        JMenuItem nouvelleItem = new JMenuItem("Nouveau projet");
        nouvelleItem.addActionListener(e -> actionNouveauProjet());

        JMenuItem ouvrirItem = new JMenuItem("Ouvrir projet");
        ouvrirItem.addActionListener(e -> actionOuvrirProjet());

        JMenuItem exporterItem = new JMenuItem("Exporter en image");
        exporterItem.addActionListener(e -> actionExporter());

        JMenuItem sauvegarderItem = new JMenuItem("Sauvegarder positions");
        sauvegarderItem.addActionListener(e -> actionSauvegarder());

        JMenuItem quitterItem = new JMenuItem("Quitter");
        quitterItem.addActionListener(e -> System.exit(0));

        menu.add(nouvelleItem);
        menu.add(ouvrirItem);
        menu.addSeparator();
        menu.add(exporterItem);
        menu.add(sauvegarderItem);
        menu.addSeparator();
        menu.add(quitterItem);

        return menu;
    }

    private JMenu creerMenuEdition() {
        JMenu menu = new JMenu("Édition");

        JMenuItem annulerItem = new JMenuItem("Annuler");
        JMenuItem retablirItem = new JMenuItem("Rétablir");
        JMenuItem supprimerItem = new JMenuItem("Supprimer");

        annulerItem.addActionListener(e -> actionAnnuler());
        retablirItem.addActionListener(e -> actionRetablir());
        supprimerItem.addActionListener(e -> actionSupprimer());

        menu.add(annulerItem);
        menu.add(retablirItem);
        menu.addSeparator();
        menu.add(supprimerItem);

        return menu;
    }

    private JMenu creerMenuAffichage() {
        JMenu menu = new JMenu("Affichage");

        JCheckBoxMenuItem afficherAttributsItem = new JCheckBoxMenuItem("Afficher attributs", true);
        JCheckBoxMenuItem afficherMethodesItem = new JCheckBoxMenuItem("Afficher méthodes", true);
        JMenuItem alignerItem = new JMenuItem("Aligner les symboles");
        JMenuItem optimiserItem = new JMenuItem("Optimiser les positions");

        afficherAttributsItem.addActionListener(e -> actionAffichageAttributs());
        afficherMethodesItem.addActionListener(e -> actionAffichageMethodes());
        alignerItem.addActionListener(e -> actionAligner());
        optimiserItem.addActionListener(e -> actionOptimiser());

        menu.add(afficherAttributsItem);
        menu.add(afficherMethodesItem);
        menu.addSeparator();
        menu.add(alignerItem);
        menu.add(optimiserItem);

        return menu;
    }

    private JMenu creerMenuAide() {
        JMenu menu = new JMenu("Aide");

        JMenuItem aProposItem = new JMenuItem("À propos");
        aProposItem.addActionListener(e -> actionAPropos());

        menu.add(aProposItem);

        return menu;
    }

    // Méthodes d'action (à implémenter)
    private void actionNouveauProjet() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Nouveau projet");
    }

    private void actionOuvrirProjet() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Ouvrir projet");
    }

    private void actionExporter() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Exporter en image");
    }

    private void actionSauvegarder() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Sauvegarder positions");
    }

    private void actionAnnuler() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Annuler");
    }

    private void actionRetablir() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Rétablir");
    }

    private void actionSupprimer() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Supprimer");
    }

    private void actionAffichageAttributs() {
        // À implémenter
    }

    private void actionAffichageMethodes() {
        // À implémenter
    }

    private void actionAligner() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Aligner les symboles");
    }

    private void actionOptimiser() {
        JOptionPane.showMessageDialog(null, "Fonctionnalité à implémenter : Optimiser les positions");
    }

    private void actionAPropos() {
        JOptionPane.showMessageDialog(null,
            "Modélisation UML - Générateur de Diagrammes\n" +
            "Version 1.0\n" +
            "Génération de diagrammes UML à partir de classes Java",
            "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
