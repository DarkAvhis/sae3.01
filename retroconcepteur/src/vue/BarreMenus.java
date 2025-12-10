package vue;

import java.awt.event.*;
import javax.swing.*;

import src.Controleur;

public class BarreMenus extends JMenuBar implements ActionListener
{

    // Références aux items pour les reconnaître dans actionPerformed
    private JMenuItem nouvelleItem;
    private JMenuItem ouvrirItem;
    private JMenuItem exporterItem;
    private JMenuItem sauvegarderItem;
    private JMenuItem quitterItem;

    private JMenuItem annulerItem;
    private JMenuItem retablirItem;
    private JMenuItem supprimerItem;

    private JCheckBoxMenuItem afficherAttributsItem;
    private JCheckBoxMenuItem afficherMethodesItem;
    private JMenuItem alignerItem;
    private JMenuItem optimiserItem;

    private JMenuItem aProposItem;

    public BarreMenus(Controleur controleur) 
    {
        
        this.add(creerMenuFichier());
        this.add(creerMenuEdition());
        this.add(creerMenuAffichage());
        this.add(creerMenuAide());
    }

    private JMenu creerMenuFichier() 
    {
        JMenu menu = new JMenu("Fichier");

        this.nouvelleItem   = new JMenuItem("Nouveau projet");
        this.ouvrirItem     = new JMenuItem("Ouvrir projet");
        this.exporterItem   = new JMenuItem("Exporter en image");
        this.sauvegarderItem= new JMenuItem("Sauvegarder positions");
        this.quitterItem    = new JMenuItem("Quitter");

        this.nouvelleItem.addActionListener(this);
        this.ouvrirItem.addActionListener(this);
        this.exporterItem.addActionListener(this);
        this.sauvegarderItem.addActionListener(this);
        this.quitterItem.addActionListener(this);

        menu.add(exporterItem);
        menu.addSeparator();
        menu.add(quitterItem);

        return menu;
    }

    private JMenu creerMenuEdition() 
    {
        JMenu menu = new JMenu("Édition");

        this.annulerItem   = new JMenuItem("Annuler");
        this.retablirItem  = new JMenuItem("Rétablir");
        this.supprimerItem = new JMenuItem("Supprimer");

        annulerItem.addActionListener(this);
        retablirItem.addActionListener(this);
        supprimerItem.addActionListener(this);

        menu.add(annulerItem);
        menu.add(retablirItem);
        menu.addSeparator();
        menu.add(supprimerItem);

        return menu;
    }

    private JMenu creerMenuAffichage() 
    {
        JMenu menu = new JMenu("Affichage");

        this.afficherAttributsItem = new JCheckBoxMenuItem("Afficher attributs", false);
        this.afficherMethodesItem  = new JCheckBoxMenuItem("Afficher méthodes", false);
        this.alignerItem           = new JMenuItem("Aligner les symboles");
        this.optimiserItem         = new JMenuItem("Optimiser les positions");

        afficherAttributsItem.addActionListener(this);
        afficherMethodesItem.addActionListener(this);
        alignerItem.addActionListener(this);
        optimiserItem.addActionListener(this);

        menu.add(afficherAttributsItem);
        menu.add(afficherMethodesItem);
        menu.addSeparator();
        menu.add(alignerItem);
        menu.add(optimiserItem);

        return menu;
    }

    private JMenu creerMenuAide() 
    {
        JMenu menu = new JMenu("Aide");

        this.aProposItem = new JMenuItem("À propos");
        aProposItem.addActionListener(this);

        menu.add(aProposItem);

        return menu;
    }

 
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        
            if (src == nouvelleItem)        actionNouveauProjet();
            else if (src == ouvrirItem)     actionOuvrirProjet();
            else if (src == exporterItem)   actionExporter();
            else if (src == sauvegarderItem)actionSauvegarder();
            else if (src == quitterItem)    System.exit(0);

            else if (src == annulerItem)    actionAnnuler();
            else if (src == retablirItem)   actionRetablir();
            else if (src == supprimerItem)  actionSupprimer();

            else if (src == afficherAttributsItem) actionAffichageAttributs();
            else if (src == afficherMethodesItem)  actionAffichageMethodes();
            else if (src == alignerItem)           actionAligner();
            else if (src == optimiserItem)         actionOptimiser();
            else if (src == aProposItem)    actionAPropos();

    }


    private void actionNouveauProjet() { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionOuvrirProjet()  { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionExporter()      { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionSauvegarder()   { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionAnnuler()       { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionRetablir()      { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionSupprimer()     { JOptionPane.showMessageDialog(null, "Pas fini"); }

    private void actionAffichageAttributs() { /* À implémenter */ }
    private void actionAffichageMethodes()  { /* À implémenter */ }

    private void actionAligner()    { JOptionPane.showMessageDialog(null, "Pas fini"); }
    private void actionOptimiser()  { JOptionPane.showMessageDialog(null, "Pas fini"); }

    private void actionAPropos()
    {
        JOptionPane.showMessageDialog(null,
            "Modélisation UML - Générateur de Diagrammes\n" +
            "par Quentin MORVAN,\nValentin LEROY,\nCelim CHAOU,\nEnzo DUMONT,\nAriunbayar BUYANBADRAKH,\nYassine EL MAADI",
            "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
