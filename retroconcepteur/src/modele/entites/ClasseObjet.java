package modele.entites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Représente une classe UML analysée à partir d'un fichier Java.
 * Contient son nom, sa liste d'attributs et sa liste de méthodes.
 */
public class ClasseObjet {
	/*-------------------------------------- */
	/* Attributs */
	/*-------------------------------------- */

	private String nom;
	private ArrayList<AttributObjet> attributs;
	private ArrayList<MethodeObjet> methodes;
	private String specifique;

	private static final String ANSI_SOUSTITRE = "\u001B[4m";
	private static final String ANSI_GRAS = "\u001B[1m";
	private static final String ANSI_RESET = "\u001B[0m";

	/*-------------------------------------- */
	/* Constructeur */
	/*-------------------------------------- */
	public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom,
			String specifique) {
		this.attributs = attributs;
		this.methodes = methodes;
		this.nom = nom;
		this.specifique = specifique;
	}

	/*-------------------------------------- */
	/* Les Accesseurs */
	/*-------------------------------------- */
	public String getNom() {
		return this.nom;
	}

	public ArrayList<AttributObjet> getattributs() {
		return this.attributs;
	}

	public ArrayList<MethodeObjet> getMethodes() {
		return this.methodes;
	}

	public String getSpecifique() {
		return this.specifique;
	}

	/*-------------------------------------- */
	/* Modificateurs */
	/*-------------------------------------- */
	public void setNom(String nom) {
		this.nom = nom;
	}

	public void setattributs(ArrayList<AttributObjet> attributs) {
		this.attributs = attributs;
	}

	public void setmethodes(ArrayList<MethodeObjet> methodes) {
		this.methodes = methodes;
	}

	/*-------------------------------------- */
	/* Methode autre */
	/*-------------------------------------- */

	/**
	 * Convertit une visibilité Java en symbole UML.
	 *
	 * @param visibilite "public", "private", "protected" ou autre
	 * @return symbole UML correspondant (+, -, #, ~)
	 */
	public char changementVisibilite(String visibilite) {
		switch (visibilite) {
			case "private":
				return '-';
			case "public":
				return '+';
			case "protected":
				return '#';
			default:
				return '~';
		}
	}

	/**
	 * Formate les paramètres d'une méthode selon la syntaxe UML.
	 *
	 * @param parametre hashmap contenant nomParam → typeParam
	 * @return chaîne formatée des paramètres (exemple : "(x: int, y: String)")
	 */
	public String affichageParametre(HashMap<String, String> parametre) {
		String sRet = "";

		if (parametre != null && !parametre.isEmpty()) {
			sRet += "(";
			for (String key : parametre.keySet())
				sRet += key + ": " + parametre.get(key) + ", ";

			sRet = sRet.substring(0, sRet.length() - 2);
			sRet += ")";
		} else {
			sRet = "()";
		}

		return sRet;
	}
	

	/**
	 * Retourne la syntaxe UML pour un type de retour.
	 *
	 * @param type type de retour Java
	 * @return ": type" ou chaîne vide si void
	 */
	public String retourType(String type) {
		if (type == null)
			return " ";

		if (type.equals("public") || type.equals("void"))
			return " ";

		return " : " + type;
	}

	/*-------------------------------------- */
	/* toString */
	/*-------------------------------------- */

	/**
	 * Génère une représentation textuelle de la classe sous forme UML.
	 *
	 * @return chaîne formatée représentant la classe (attributs + méthodes)
	 */
	public String toString() {

		// Liste des types de référence standards à ne pas masquer (String,
		// Integer,etc...)
		final List<String> typesNonAssociation = Arrays.asList(
				"String", "Integer", "Double", "Boolean", "Character", "Long", "Float", "Short", "Byte");

		String sRet = "";

		if (!this.specifique.equals("")) {
			sRet += "-------------------------------------------------------------------------------------------\n";
			sRet += String.format("%53s", ClasseObjet.ANSI_GRAS + "<<" + this.specifique + ">>") + "\n";
			sRet += String.format("%50s", this.nom + ClasseObjet.ANSI_RESET) + "\n";
			sRet += "-------------------------------------------------------------------------------------------\n";
		} else {
			sRet += "-------------------------------------------------------------------------------------------\n";
			sRet += String.format("%50s", ClasseObjet.ANSI_GRAS + this.nom + ClasseObjet.ANSI_RESET) + "\n";
			sRet += "-------------------------------------------------------------------------------------------\n";
		}

		// --- AFFICHAGE DES ATTRIBUTS ---
		for (AttributObjet att : attributs) {
			String typeAttribut = att.getType().trim();

			// 1. Vérifier si le type commence par une Majuscule (potentiellement une classe
			// utilisateur)
			boolean commenceParMaj = !typeAttribut.isEmpty() && Character.isUpperCase(typeAttribut.charAt(0));

			// 2. Définir si c'est un attribut d'association (à masquer)
			boolean estAssociation = commenceParMaj && !typeAttribut.contains("<") &&
					!typesNonAssociation.contains(typeAttribut); // Pas un type standard (String)

			// Si c'est un attribut d'association (simple objet OU tableau d'objets), on le
			// masque.
			if (estAssociation) {
				continue;
			}

			String finalFlag = att.estFinale() ? " {gelé}" : "";

			String nomBaseFormatte = String.format("%-15s", att.getNom());

			String nomFormatte;
			if (att.estStatique()) {
				nomFormatte = String.format("%-10s",
						ClasseObjet.ANSI_SOUSTITRE + nomBaseFormatte + ClasseObjet.ANSI_RESET);
			} else {
				nomFormatte = nomBaseFormatte;
			}

			sRet += String.format("%-2c", changementVisibilite(att.getVisibilite())) + nomFormatte +
					String.format("%-15s", retourType(att.getType())) +
					String.format("%-10s", finalFlag) + "\n";
		}

		sRet += "-------------------------------------------------------------------------------------------\n";

		// --- AFFICHAGE DES METHODES ---
		for (MethodeObjet met : methodes) {
			// Application du soulignement aux méthodes statiques
			String nomMethodeBrut = met.getNom();
			String methodeBaseFormatte = String.format("%-25s", nomMethodeBrut);

			String nomMethodeFormatte;
			if (met.estStatique()) {
				nomMethodeFormatte = ClasseObjet.ANSI_SOUSTITRE + methodeBaseFormatte + ClasseObjet.ANSI_RESET;
			} else {
				nomMethodeFormatte = methodeBaseFormatte;
			}

			String visibiliteUML = String.format("%-2c", changementVisibilite(met.getVisibilite()));

			sRet += visibiliteUML + nomMethodeFormatte +
					String.format("%-35s", affichageParametre(met.getParametres())) +
					String.format("%-15s", retourType(met.getRetourType())) + "\n";
		}

		sRet += "-------------------------------------------------------------------------------------------\n";
		sRet += "\n\n\n";
		return sRet;
	}
}