package vue;

import controleur.Controleur;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

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
    private JMenuItem ouvrirClasse       ;
    private JMenuItem sauvegarderClasse  ;
    private JMenuItem quitterClasse      ;

    private JMenuItem alignerClasse;
    private JMenuItem optimiserClasse;
    private JMenuItem itemExporter;
    private JMenuItem supprimerClasse    ;

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

       
        this.ouvrirClasse      = new JMenuItem("Ouvrir projet");
        this.sauvegarderClasse = new JMenuItem("Sauvegarder au format txt");
        this.itemExporter    = new JMenuItem("Exporter le diagramme (PNG)");
        this.quitterClasse     = new JMenuItem("Quitter");

      
        this.ouvrirClasse     .addActionListener(this);
        this.sauvegarderClasse.addActionListener(this);
        this.itemExporter     .addActionListener(this);
        this.quitterClasse    .addActionListener(this);

        menu.add(ouvrirClasse)     ;
        menu.addSeparator()        ;
        menu.add(sauvegarderClasse);
        menu.add(itemExporter);
        menu.addSeparator();
        menu.add(quitterClasse)    ;

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
        this.supprimerClasse = new JMenuItem("Supprimer");
        

        alignerClasse  .addActionListener(this);
        optimiserClasse.addActionListener(this);
        supprimerClasse.addActionListener(this);

        menu.add(alignerClasse)  ;
        menu.add(optimiserClasse);
        menu.addSeparator();
        menu.add(supprimerClasse);
       

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

        if (src == ouvrirClasse     ) actionOuvrirProjet ();
        if (src == sauvegarderClasse) actionSauvegarder  ();
        if (src == supprimerClasse  ) actionSupprimer    ();
        if (src == alignerClasse    ) actionAligner      ();
        if (src == optimiserClasse  ) actionOptimiser    ();
        if (src == aProposClasse    ) actionAPropos      ();
        if (src == itemExporter     ) actionExporter     ();   
        if (src == quitterClasse    ) System.exit(0);
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
