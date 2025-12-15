package src.vue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import src.Controleur;

/**
 * Fenêtre principale de l'application de génération de diagrammes UML.
 * 
 * Cette classe gère l'interface graphique principale comprenant :
 * - Un panneau de projets à gauche pour la navigation
 * - Un panneau de diagramme au centre pour l'affichage du diagramme UML
 * - Une barre de menus pour les actions utilisateur
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class FenetrePrincipale extends JFrame
{
	private Controleur controleur;
	private PanneauProjets panneauProjets;
	private PanneauDiagramme panneauDiagramme;

	private BarreMenus barreMenus;

	/**
	 * Constructeur de la fenêtre principale.
	 * 
	 * Initialise l'interface graphique avec un panneau de projets, un panneau de
	 * diagramme
	 * et une barre de menus. Configure la fenêtre en plein écran.
	 * 
	 * @param controleur Le contrôleur principal de l'application
	 */
	public FenetrePrincipale(Controleur controleur) 
	{
		this.controleur = controleur;

		Dimension tailleEcran = Toolkit.getDefaultToolkit().getScreenSize();

		this.setTitle("Générateur de diagramme UML");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(tailleEcran.width, tailleEcran.height);
		this.setLocationRelativeTo(null);
		this.setResizable(true);

		this.panneauDiagramme = new PanneauDiagramme(controleur);
		this.panneauProjets = new PanneauProjets(this, controleur, this.panneauDiagramme);
		this.barreMenus = new BarreMenus(controleur, panneauProjets);//

		JScrollPane scrollDiagramme = new JScrollPane(this.panneauDiagramme);

		this.setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				true,
				this.panneauProjets,
				scrollDiagramme // <-- CORRECTION : Utilisation du JScrollPane
		);
		splitPane.setDividerLocation(250);
		splitPane.setOneTouchExpandable(true);

		this.add(splitPane, BorderLayout.CENTER);

		this.setJMenuBar(barreMenus);//
	}

	/**
	 * Charge et affiche un projet Java dans le diagramme.
	 * 
	 * Délègue l'analyse et l'affichage au contrôleur.
	 * 
	 * @param cheminProjet Chemin absolu vers le dossier du projet à charger
	 */
	public void chargerProjet(String cheminProjet) 
	{
		this.controleur.analyserEtAfficherDiagramme(cheminProjet);
	}

	/**
	 * Retourne le panneau de diagramme de la fenêtre.
	 * 
	 * @return Le panneau de diagramme permettant d'accéder aux blocs et liaisons
	 *         affichés
	 */
	public PanneauDiagramme getPanneauDiagramme() 
	{
		return this.panneauDiagramme;
	}

	public PanneauProjets getPanneauProjets() 
	{
		return this.panneauProjets;
	}
}