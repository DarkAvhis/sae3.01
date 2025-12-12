package src.modele;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Représente une classe UML analysée à partir d'un fichier Java.
 * Contient son nom, sa liste d'attributs et sa liste de méthodes.
 */
public class ClasseObjet 
{
	/*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */

	/** Nom de la classe analysée. */
	private String nom;

	/** Liste des attributs appartenant à la classe. */
	private ArrayList<AttributObjet> attributs;

	/** Liste des méthodes appartenant à la classe. */
	private ArrayList<MethodeObjet>  methodes;
	private String specifique ;

	/*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
	public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom ,String specifique) 
	{
		this.attributs  = attributs;
		this.methodes   = methodes;
		this.nom        = nom;
		this.specifique = specifique;
	}

	/*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */

	/**
	 * Retourne le nom de la classe.
	 *
	 * @return nom de la classe
	 */
	public String getNom() { return nom; }

	/**
	 * Retourne la liste des attributs de la classe.
	 *
	 * @return liste des attributs
	 */
	public ArrayList<AttributObjet> getattributs() { return attributs; }

	/**
	 * Retourne la liste des méthodes de la classe.
	 *
	 * @return liste des méthodes
	 */
	public ArrayList<MethodeObjet> getMethodes() { return methodes; }

	/*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */

	/**
	 * Modifie le nom de la classe.
	 *
	 * @param nom nouveau nom
	 */
	public void setNom(String nom) { this.nom = nom; }

	/**
	 * Modifie la liste des attributs de la classe.
	 *
	 * @param attributs nouvelle liste d'attributs
	 */
	public void setattributs(ArrayList<AttributObjet> attributs) { this.attributs = attributs; }

	/**
	 * Modifie la liste des méthodes de la classe.
	 *
	 * @param methodes nouvelle liste de méthodes
	 */
	public void setmethodes(ArrayList<MethodeObjet> methodes) { this.methodes = methodes; }


	/*-------------------------------------- */
	/* Methode autre                         */
	/*-------------------------------------- */

	/**
	 * Convertit une visibilité Java en symbole UML.
	 *
	 * @param visibilite "public", "private", "protected" ou autre
	 * @return symbole UML correspondant (+, -, #, ~)
	 */
	public char changementVisibilite(String visibilite) 
	{
		switch (visibilite) 
		{
			case "private"  : return '-';
			case "public"   : return '+';
			case "protected": return '#';
			default:          return '~';
		}
	}

	/**
	 * Formate les paramètres d'une méthode selon la syntaxe UML.
	 *
	 * @param parametre hashmap contenant nomParam → typeParam
	 * @return chaîne formatée des paramètres (exemple : "(x: int, y: String)")
	 */
	public String affichageParametre(HashMap<String, String> parametre)
	{
		String sRet = "";

		if (parametre != null && !parametre.isEmpty())
		{
			sRet += "(";
			for (String key : parametre.keySet())
			{
				sRet += key + ": " + parametre.get(key) + ", ";
			}
			sRet = sRet.substring(0, sRet.length() - 2);
			sRet += ")";
		}
		else
		{
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
	public String retourType(String type) 
	{
		if (type == null) return " "; 

		if (type.equals("public") || type.equals("void")) 
		{
			return " "; 
		}
		return " : " + type;
	}

	/*-------------------------------------- */
	/* toString                              */
	/*-------------------------------------- */

	/**
	 * Génère une représentation textuelle de la classe sous forme UML.
	 *
	 * @return chaîne formatée représentant la classe (attributs + méthodes)
	 */
	public String toString() 
	{
		// Codes ANSI
		final String ANSI_SOUSTITRE = "\u001B[4m";
		final String ANSI_RESET     = "\u001B[0m";
		
		// Liste des types de référence standards à NE PAS masquer (String, Integer, etc.)
		final List<String> typesNonAssociation = Arrays.asList(
			"String", "Integer", "Double", "Boolean", "Character", "Long", "Float", "Short", "Byte"
		);
		
		String sRet = "";

		// Affichage du Stéréotype et du Nom (<<interface>>, <<record>>, etc.)
		if ( !this.specifique.equals("") ) 
		{
			sRet += "-------------------------------------------------------------------------------------------\n";
			sRet += String.format( "%53s" ,  "<<" + this.specifique + ">>") +              "\n";
			sRet += String.format( "%50s" ,  this.nom ) +              "\n";
			sRet += "-------------------------------------------------------------------------------------------\n";
		}
		else
		{
			sRet += "-------------------------------------------------------------------------------------------\n";
			sRet += String.format( "%50s" ,  this.nom ) +              "\n";
			sRet += "-------------------------------------------------------------------------------------------\n";
		}

		// --- AFFICHAGE DES ATTRIBUTS ---
		for (AttributObjet att : attributs) 
		{
			String typeAttribut = att.getType().trim();

			// LOGIQUE DE MASQUAGE AFFINÉE (pour masquer les attributs d'association)
			
			// 1. Vérifier si le type commence par une Majuscule (potentiellement une classe utilisateur)
			boolean commenceParMaj = !typeAttribut.isEmpty() && 
									  Character.isUpperCase(typeAttribut.charAt(0));
			
			// 2. Définir si c'est un attribut d'association (à masquer)
			boolean estAssociation = commenceParMaj	 && !typeAttribut.contains("<") && 
									!typesNonAssociation.contains(typeAttribut); // Pas un type standard (String)
			
			if (estAssociation) 
			{
				// Si c'est un attribut d'association (simple objet OU tableau d'objets), on le masque.
				continue; 
			}
			// FIN LOGIQUE DE MASQUAGE AFFINÉE
			
			String finalFlag  = att.estFinale()   ? " {gelé}" : "";
			
			// 1. Appliquer le formatage au nom SANS les codes ANSI
			String nomBaseFormatte = String.format("%-15s" , att.getNom() ); 

			// 2. Ajouter le soulignement ANSI SI c'est statique (UML)
			String nomFormatte;
			if (att.estStatique()) 
			{
				nomFormatte = String.format("%-10s",ANSI_SOUSTITRE + nomBaseFormatte + ANSI_RESET);
			} 
			else 
			{
				nomFormatte = nomBaseFormatte;
			}
			
			sRet += String.format( "%-2c" , changementVisibilite(att.getVisibilite()) ) + nomFormatte  + 
					String.format("%-15s" , retourType( att.getType() ))  + 
					String.format("%-10s" , finalFlag) + "\n" ; 
		}

		sRet += "-------------------------------------------------------------------------------------------\n";

		// --- AFFICHAGE DES METHODES ---
		for (MethodeObjet met : methodes)
		{
			// Application du soulignement aux méthodes statiques
			String nomMethodeBrut 	   = met.getNom();
			String methodeBaseFormatte = String.format("%-25s", nomMethodeBrut);

			String nomMethodeFormatte;
			if (met.estStatique()) { nomMethodeFormatte = ANSI_SOUSTITRE + methodeBaseFormatte + ANSI_RESET;} 
			else 				   { nomMethodeFormatte = methodeBaseFormatte							   ;}
			
			String visibiliteUML = String.format("%-2c", changementVisibilite(met.getVisibilite()));
			
			sRet += visibiliteUML + nomMethodeFormatte + 
					String.format("%-35s", affichageParametre(met.getParametres())) + 
					String.format("%-15s", retourType(met.getRetourType())) + "\n"; 
		}

		sRet += "-------------------------------------------------------------------------------------------\n";
		return sRet;
	}


}
