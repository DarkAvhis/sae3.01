package vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import controleur.Controleur;

/**
 * Barre de menus de l'application.
 *
 * Gère les menus Fichier, Affichage et Aide avec leurs actions respectives.
 * Implémente ActionListener pour traiter les événements des items de menu.
 *
 * @author Quentin MORVAN,
 *         Valentin LEROY,
 *         Celim CHAOU,
 *         Enzo DUMONT,
 *         Ariunbayar BUYANBADRAKH,
 *         Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class BarreMenus extends JMenuBar implements ActionListener {
    // Références aux items de menu
    private JMenuItem ouvrirClasse;
    private JMenuItem sauvegarderClasse;
    private JMenuItem quitterClasse;

    private JMenuItem alignerClasse;
    private JMenuItem optimiserClasse;
    private JMenuItem itemExporter;
    private JMenuItem supprimerClasse;
    private JCheckBoxMenuItem afficherExternes;

    private JMenuItem aProposClasse;

    private PanneauProjets panneauProjets;
    private Controleur controleur;

    /**
     * Constructeur de la barre de menus.
     *
     * @param controleur     contrôleur principal
     * @param panneauProjets panneau des projets ouverts
     */
    public BarreMenus(Controleur controleur, PanneauProjets panneauProjets) {
        this.controleur = controleur;
        this.panneauProjets = panneauProjets;

        this.add(creerMenuFichier());
        this.add(creerMenuAffichage());
        this.add(creerMenuAide());
    }

    public JMenu creerMenuFichier() {
        JMenu menu = new JMenu("Fichier");

        this.ouvrirClasse = new JMenuItem("Ouvrir projet");
        this.sauvegarderClasse = new JMenuItem("Sauvegarder au format txt");
        this.itemExporter = new JMenuItem("Exporter le diagramme (PNG)");
        JMenuItem itemExporterJSON = new JMenuItem("Exporter le diagramme (JSON)");
        this.quitterClasse = new JMenuItem("Quitter");

        ouvrirClasse.addActionListener(this);
        sauvegarderClasse.addActionListener(this);
        itemExporter.addActionListener(this);
        itemExporterJSON.addActionListener(this);
        quitterClasse.addActionListener(this);

        menu.add(ouvrirClasse);
        menu.addSeparator();
        menu.add(sauvegarderClasse);
        menu.add(itemExporter);
        menu.add(itemExporterJSON);
        menu.addSeparator();
        menu.add(quitterClasse);

        return menu;
    }

    public JMenu creerMenuAffichage() {
        JMenu menu = new JMenu("Affichage");

        this.alignerClasse = new JMenuItem("Aligner les symboles");
        this.optimiserClasse = new JMenuItem("Optimiser les positions");
        this.supprimerClasse = new JMenuItem("Supprimer");
        this.afficherExternes = new JCheckBoxMenuItem("Afficher les classes externes", true);

        alignerClasse.addActionListener(this);
        optimiserClasse.addActionListener(this);
        supprimerClasse.addActionListener(this);
        afficherExternes.addActionListener(this);

        menu.add(alignerClasse);
        menu.add(optimiserClasse);
        menu.addSeparator();
        menu.add(supprimerClasse);
        menu.addSeparator();
        menu.add(afficherExternes);

        return menu;
    }

    public JMenu creerMenuAide() {
        JMenu menu = new JMenu("Aide");

        this.aProposClasse = new JMenuItem("À propos");
        aProposClasse.addActionListener(this);

        menu.add(aProposClasse);

        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == ouvrirClasse) {
            actionOuvrirProjet();
        }
        if (src == sauvegarderClasse) {
            actionSauvegarder();
        }
        if (src == supprimerClasse) {
            actionSupprimer();
        }
        if (src == alignerClasse) {
            actionAligner();
        }
        if (src == optimiserClasse) {
            actionOptimiser();
        }
        if (src == aProposClasse) {
            actionAPropos();
        }
        if (src == itemExporter) {
            actionExporter();
        }
        if (src instanceof JMenuItem && ((JMenuItem) src).getText().contains("JSON")) {
            actionExporterJSON();
        }
        if (src == afficherExternes) {
            actionToggleExternes();
        }
        if (src == quitterClasse) {
            System.exit(0);
        }
    }

    public void actionExporter() {
        controleur.exporterDiagramme();
    }

    public void actionExporterJSON() {
        controleur.exporterDiagrammeJSON();
    }

    public void actionOuvrirProjet() {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int retour = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));

        if (retour == JFileChooser.APPROVE_OPTION) {
            File dossier = chooser.getSelectedFile();

            if (dossier.exists() && dossier.isDirectory()) {
                try {
                    controleur.analyserEtAfficherDiagramme(
                            dossier.getAbsolutePath());

                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(this),
                            "Dossier chargé : " + dossier.getName());

                    panneauProjets.ajouterProjet(dossier.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(this),
                            "Erreur lors de l'ouverture du dossier :\n"
                                    + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Impossible d'ouvrir ce dossier.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void actionSauvegarder() {
        String projet = controleur.getCheminProjetActuel();
        String dossierCible;

        if (projet == null || projet.isEmpty()) {
            int rep = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Aucun projet ouvert. Voulez-vous choisir un dossier ?",
                    "Sauvegarder",
                    JOptionPane.YES_NO_OPTION);

            if (rep != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (chooser.showOpenDialog(
                    SwingUtilities.getWindowAncestor(this)) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            dossierCible = chooser.getSelectedFile().getAbsolutePath();
        } else {
            dossierCible = projet;
        }

        File sortie = new File(dossierCible, "DiagrammeUML.txt");
        controleur.sauvegarde(dossierCible, sortie.getAbsolutePath());

        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Diagramme sauvegardé dans " + sortie.getAbsolutePath(),
                "Sauvegarde réussie",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void actionSupprimer() {
        int rep = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Voulez-vous vraiment supprimer la classe sélectionnée ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (rep == JOptionPane.YES_OPTION) {
            controleur.supprimerClasseSelectionnee();
        }
    }

    public void actionAligner() {
        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Pas fini");
    }

    public void actionOptimiser() {
        controleur.optimiserDisposition();

        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Positions optimisées !",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void actionToggleExternes() {
        boolean afficher = afficherExternes.isSelected();
        controleur.setAfficherClassesExternes(afficher);
    }

    public void actionAPropos() {
        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Modélisation UML - Générateur de Diagrammes\n"
                        + "par Quentin MORVAN,\n"
                        + "Valentin LEROY,\n"
                        + "Celim CHAOU,\n"
                        + "Enzo DUMONT,\n"
                        + "Ariunbayar BUYANBADRAKH,\n"
                        + "Yassine EL MAADI",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
