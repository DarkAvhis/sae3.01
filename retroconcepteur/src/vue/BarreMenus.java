package src.vue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import src.Controleur;
import src.modele.Sauvegarde;

/**
 * Barre de menus de l'application.
 * 
 * Gère les menus Fichier, Édition, Affichage et Aide avec leurs actions
 * respectives.
 * Implémente ActionListener pour traiter les événements des items de menu.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class BarreMenus extends JMenuBar implements ActionListener
{

    // Références aux Classes pour les reconnaître dans actionPerformed
    private JMenuItem nouvelleClasse     ;
    private JMenuItem ouvrirClasse       ;
    private JMenuItem sauvegarderClasse  ;
    private JMenuItem quitterClasse      ;

    private JMenuItem annulerClasse      ;
    private JMenuItem retablirClasse     ;
    private JMenuItem supprimerClasse    ;

    private JMenuItem alignerClasse;
    private JMenuItem optimiserClasse;
    private JMenuItem itemExporter;

    private JMenuItem aProposClasse      ;

    private PanneauProjets panneauProjets;
    private Controleur     controleur    ;

    /**
     * Constructeur de la barre de menus.
     * 
     * Crée et configure tous les menus de l'application.
     * 
     * @param controleur     Le contrôleur principal de l'application
     * @param panneauProjets Le panneau de projets pour ajouter des projets ouverts
     */
    public BarreMenus(Controleur controleur, PanneauProjets panneauProjets) 
    {
        this.controleur = controleur;
        this.panneauProjets = panneauProjets;

        this.add(creerMenuFichier());
        this.add(creerMenuEdition());
        this.add(creerMenuAffichage());
        this.add(creerMenuAide());
    }

    /**
     * Crée le menu Fichier.
     * 
     * Contient les actions : Nouveau projet, Ouvrir projet, Sauvegarder positions,
     * Quitter.
     * 
     * @return Le menu Fichier configuré
     */
    public JMenu creerMenuFichier() 
    {
        JMenu menu = new JMenu("Fichier");

        this.nouvelleClasse    = new JMenuItem("Nouveau projet");
        this.ouvrirClasse      = new JMenuItem("Ouvrir projet");
        this.sauvegarderClasse = new JMenuItem("Sauvegarder positions");
        this.quitterClasse     = new JMenuItem("Quitter");

        this.nouvelleClasse   .addActionListener(this);
        this.ouvrirClasse     .addActionListener(this);
        this.sauvegarderClasse.addActionListener(this);
        this.quitterClasse    .addActionListener(this);

        menu.add(nouvelleClasse)   ;
        menu.add(ouvrirClasse)     ;
        menu.addSeparator()        ;
        menu.add(sauvegarderClasse);
        menu.add(quitterClasse)    ;

        return menu;
    }

    /**
     * Crée le menu Édition.
     * 
     * Contient les actions : Annuler, Rétablir, Supprimer.
     * 
     * @return Le menu Édition configuré
     */
    public JMenu creerMenuEdition() 
    {
        JMenu menu = new JMenu("Édition");

        this.annulerClasse   = new JMenuItem("Annuler")  ;
        this.retablirClasse  = new JMenuItem("Rétablir") ;
        this.supprimerClasse = new JMenuItem("Supprimer");

        annulerClasse  .addActionListener(this);
        retablirClasse .addActionListener(this);
        supprimerClasse.addActionListener(this);

        menu.add(annulerClasse);
        menu.add(retablirClasse);
        menu.addSeparator();
        menu.add(supprimerClasse);

        return menu;
    }

    /**
     * Crée le menu Affichage.
     * 
     * Contient les actions : Aligner les symboles, Optimiser les positions.
     * 
     * @return Le menu Affichage configuré
     */
    public JMenu creerMenuAffichage() 
    {
        JMenu menu = new JMenu("Affichage");

        this.alignerClasse   = new JMenuItem("Aligner les symboles");
        this.optimiserClasse = new JMenuItem("Optimiser les positions");
        this.itemExporter    = new JMenuItem("Exporter le diagramme (PNG)");

        alignerClasse  .addActionListener(this);
        optimiserClasse.addActionListener(this);
        itemExporter   .addActionListener(this);

        menu.add(alignerClasse)  ;
        menu.add(optimiserClasse);
        menu.addSeparator();
        menu.add(itemExporter);

        return menu;
    }

    /**
     * Crée le menu Aide.
     * 
     * Contient l'action : À propos.
     * 
     * @return Le menu Aide configuré
     */
    public JMenu creerMenuAide() 
    {
        JMenu menu = new JMenu("Aide");

        this.aProposClasse = new JMenuItem("À propos");
        aProposClasse.addActionListener(this);

        menu.add(aProposClasse);

        return menu;
    }

    /**
     * Traite les événements des items de menu.
     * 
     * Redirige vers la méthode d'action appropriée selon la source de l'événement.
     * 
     * @param e L'événement déclenché par un item de menu
     */
    public void actionPerformed(ActionEvent e) 
    {
        Object src = e.getSource();

        if      (src == nouvelleClasse   ) actionNouveauProjet();
        else if (src == ouvrirClasse     ) actionOuvrirProjet ();
        else if (src == sauvegarderClasse) actionSauvegarder  ();
        else if (src == quitterClasse    ) System.exit(0);
        else if (src == annulerClasse    ) actionAnnuler      ();
        else if (src == retablirClasse   ) actionRetablir     ();
        else if (src == supprimerClasse  ) actionSupprimer    ();
        else if (src == alignerClasse    ) actionAligner      ();
        else if (src == optimiserClasse  ) actionOptimiser    ();
        else if (src == aProposClasse    ) actionAPropos      ();
        else if (src == itemExporter     ) actionExporter     ();   
    }

    /**
     * Action pour créer un nouveau projet.
     * 
     * @note Fonctionnalité en cours de développement
     */
    public void actionNouveauProjet() 
    {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini");
    }

    /**
     * Action pour exporter le diagramme au format PNG.
     * 
     * Permet d'exporter le diagramme sous le nom "diagramme.png"
     */
    public void actionExporter()
    {
        controleur.exporterDiagramme("diagramme.png");
    }

    /**
     * Action pour ouvrir un projet existant.
     * 
     * Affiche un sélecteur de dossiers, analyse le dossier sélectionné
     * et ajoute le projet au panneau de projets.
     */
    public void actionOuvrirProjet() 
    {
        JFileChooser chooser = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int retour = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));

        if (retour == JFileChooser.APPROVE_OPTION) 
        {
            File dossier = chooser.getSelectedFile();

            if (dossier.exists() && dossier.isDirectory()) 
            {
                try
                {
                    this.controleur.analyserEtAfficherDiagramme(dossier.getAbsolutePath());
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                            "Dossier chargé : " + dossier.getName());

                    // ajoute dans panneauProjet le projet ouvert
                    this.panneauProjets.ajouterProjet(dossier.getAbsolutePath());
                } 
                catch (Exception ex) 
                {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                            "Erreur lors de l'ouverture du dossier :\n" + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } 
            else
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Impossible d'ouvrir ce dossier.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Action pour sauvegarder les positions des blocs.
     */
    public void actionSauvegarder() 
    {
        // Récupère le dossier du projet ouvert dans l'UI (si présent)
        String projet = this.controleur.getCheminProjetActuel();
        String dossierCible = null;

        if (projet == null || projet.isEmpty())
        {
            int rep = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                    "Aucun projet ouvert. Voulez-vous choisir un dossier à sauvegarder ?",
                    "Sauvegarder", JOptionPane.YES_NO_OPTION);
            if (rep != JOptionPane.YES_OPTION) return;

            JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int retour = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
            if (retour != JFileChooser.APPROVE_OPTION) return;

            dossierCible = chooser.getSelectedFile().getAbsolutePath();
        }
        else
        {
            dossierCible = projet;
        }

        File sortie = new File(dossierCible, "DiagrammeUML.txt");
        controleur.sauvegarde(dossierCible, sortie.getAbsolutePath());

        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(this),
            "Diagramme sauvegardé dans " + sortie.getAbsolutePath(),
            "Sauvegarde réussie",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void actionAnnuler() 
    {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini");
    }

    public void actionRetablir() 
    {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini");
    }

    public void actionSupprimer() 
    {

        int rep = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Voulez-vous vraiment supprimer la classe sélectionnée ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (rep == JOptionPane.YES_OPTION) 
        {
            this.controleur.supprimerClasseSelectionnee();
        }

    }

    public void actionAligner() 
    {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini");
    }

    /**
     * Action pour optimiser la disposition des blocs dans le diagramme.
     */
    public void actionOptimiser() 
    {
        // Récupérer le panneau du diagramme via le contrôleur
        this.controleur.optimiserDisposition();
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Positions optimisées !", "Succès",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Action pour afficher les informations À propos de l'application.
     */
    public void actionAPropos() 
    {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "Modélisation UML - Générateur de Diagrammes\n" +
                        "par Quentin MORVAN,\nValentin LEROY,\nCelim CHAOU,\nEnzo DUMONT,\nAriunbayar BUYANBADRAKH,\nYassine EL MAADI",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
