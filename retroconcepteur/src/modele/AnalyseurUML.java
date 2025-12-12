package src.modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * Analyseur syntaxique de fichiers Java pour la génération de diagrammes UML.
 * 
 * Cette classe implémente un parseur manuel (sans utiliser String.split()) pour
 * analyser
 * les fichiers Java et extraire les informations nécessaires à la création de
 * diagrammes UML :
 * - Classes, interfaces, enums, records
 * - Attributs (avec visibilité, type, modificateurs static/final)
 * - Méthodes (avec visibilité, paramètres, type de retour, modificateurs)
 * - Relations d'héritage (extends)
 * - Implémentations d'interfaces (implements)
 * - Associations (attributs de type classe)
 * 
 * L'analyseur gère les commentaires, les espaces, les lignes vides,
 * ),les génériques(<T>),les collections(List,Set,etc.)et détecte les
 * multiplicités
 * 
 * @author Quentin MORVAN,Valentin LEROY,Celim CHAOU,Enzo DUMONT,Ariunbayar
 *         BUYANBADRAKH,Yassine EL MAADI*
 * @date 12 décembre 2025
 */

public class AnalyseurUML
{
	private static final int MULT_INDEFINIE = 999999999;

	// Listes pour stocker les relations en attente de résolution
	private ArrayList<String[]> lstIntentionHeritage = new ArrayList<>();
	private ArrayList<String[]> lstInterfaces = new ArrayList<>();

	/**
	 * Réinitialise les listes de relations stockées.
	 * 
	 * Vide les listes d'intentions d'héritage et d'interfaces.
	 * À appeler avant chaque nouvelle analyse pour éviter les doublons.
	 */
	public void resetRelations()
	{
		this.lstIntentionHeritage.clear();
		this.lstInterfaces.clear();
	}

	public ArrayList<String[]> getIntentionsHeritage()
	{
		return lstIntentionHeritage;
	}

	public ArrayList<String[]> getInterfaces()
	{
		return lstInterfaces;
	}

	/**
	 * Analyse un fichier Java et construit l'objet ClasseObjet correspondant.
	 * 
	 * Processus d'analyse ligne par ligne :
	 * 1. Ignore les commentaires et lignes vides
	 * 2. Détecte l'en-tête de classe/interface (avec extends/implements)
	 * 3. Extrait les attributs et méthodes du corps de la classe
	 * 4. Traite les cas particuliers (record, enum, interface)
	 * 
	 * @param chemin Chemin absolu vers le fichier Java à analyser
	 * @return Un objet ClasseObjet représentant la classe analysée, ou null en cas
	 *         d'erreur
	 */
	public ClasseObjet analyserFichierUnique(String chemin)
	{
		File file = new File(chemin);
		String nomFichier = file.getName().replace(".java", "");

		// Listes finales
		ArrayList<AttributObjet> attributs = new ArrayList<>();
		ArrayList<MethodeObjet> methodes = new ArrayList<>();

		// États de l'analyse
		String nomEntite = nomFichier;
		String specifique = "";
		String nomParent = null;
		boolean estInterface = false;
		boolean estRecord = false;
		boolean enTeteTrouve = false;
		boolean commentaireBlocActif = false;
		boolean estHeritier = false;

		ArrayList<String> interfacesDetectees = new ArrayList<>();

		String[] tabSpecifique = { "abstract class", "interface", "enum", "record" };

		try (Scanner sc = new Scanner(file)) {
			while (sc.hasNextLine()) {
				String ligneBrute = sc.nextLine().trim();

				// 1. GESTION DES COMMENTAIRES ET LIGNES VIDES (Logique inchangée)
				if (ligneBrute.isEmpty())
					continue;

				if (commentaireBlocActif)
				{
					if (ligneBrute.contains("*/")) 
					{
						commentaireBlocActif = false;
						if (ligneBrute.endsWith("*/"))
							continue;
						ligneBrute = ligneBrute.substring(ligneBrute.indexOf("*/") + 2).trim();
					} 
					else
					{
						continue;
					}
				}

				if (ligneBrute.startsWith("/*")) 
				{
					if (!ligneBrute.contains("*/")) 
					{
						commentaireBlocActif = true;
						continue;
					}

					else
					{
						int finCom = ligneBrute.lastIndexOf("*/");
						if (finCom + 2 < ligneBrute.length()) 
						{
							ligneBrute = ligneBrute.substring(finCom + 2).trim();
						} 
						else 
						{
							continue;
						}
					}
				}

				if (ligneBrute.startsWith("//"))
					continue;

				if (ligneBrute.contains("//")) 
				{
					ligneBrute = ligneBrute.substring(0, ligneBrute.indexOf("//")).trim();
				}

				if (ligneBrute.startsWith("package") || ligneBrute.startsWith("import"))
					continue;
				
				if (ligneBrute.equals("}"))
					continue;

				// --- 1. ANALYSE DE L'EN-TÊTE ---
				if (!enTeteTrouve && (ligneBrute.contains(" class ") || ligneBrute.contains("interface ")
						|| ligneBrute.contains("record ") || ligneBrute.contains("enum "))) 
				{
					enTeteTrouve = true;

					// Identification du type spécifique (Logique inchangée)
					for (String motCle : tabSpecifique)
					{
						if (ligneBrute.contains(motCle)) 
						{
							specifique = motCle;
						
							if (motCle.contains("interface"))
						
								estInterface = true;
							if (motCle.contains("record"))
								estRecord = true;

							// Récupération du vrai nom (parsing manuel)
							if (estInterface || estRecord)
							{
								int idxDebut = ligneBrute.indexOf(motCle) + motCle.length();
								String suite = ligneBrute.substring(idxDebut).trim();
								String nom = lireNom(suite);
						
								if (!nom.isEmpty())
									nomEntite = nom;
							}
							break;
						}
					}

					// Détection Héritage (extends)
					if (ligneBrute.contains("extends")) 
					{
						estHeritier = true;
						int idxExtends = ligneBrute.indexOf("extends") + 7;
						String suite = ligneBrute.substring(idxExtends).trim();
						// Lire le premier identifiant utile (manuellement)
						String possibleParent = lireNom(suite);
						
						if (!possibleParent.isEmpty())
							nomParent = possibleParent;
					}

					// Détection Implémentation (implements) --- OPTIMISATION ICI ---
					if (ligneBrute.contains("implements")) 
					{
						int idxImpl = ligneBrute.indexOf("implements") + 10;
						String suite = ligneBrute.substring(idxImpl).trim();
						int idxFin = suite.indexOf('{');
					
						if (idxFin != -1)
							suite = suite.substring(0, idxFin);

						// Boucle optimisée pour éviter split() sur la virgule
						int indexDebut = 0;
						int indexVirgule;

						while (indexDebut < suite.length()) 
						{
							indexVirgule = suite.indexOf(',', indexDebut);
							if (indexVirgule == -1)
							{
								indexVirgule = suite.length();
							}

							String interBrute = suite.substring(indexDebut, indexVirgule).trim();

							if (!interBrute.isEmpty()) 
							{
								// Nettoyage des génériques (<) et espaces
								int idxSpace = indexEspace(interBrute);
								int idxChevron = interBrute.indexOf('<');
								int idxFinNom = interBrute.length();

								if (idxSpace != -1)
									idxFinNom = Math.min(idxFinNom, idxSpace);
								if (idxChevron != -1)
									idxFinNom = Math.min(idxFinNom, idxChevron);

								String nomInterface = interBrute.substring(0, idxFinNom).trim();

								if (!nomInterface.isEmpty()) 
								{
									interfacesDetectees.add(nomInterface);
								}
							}

							indexDebut = indexVirgule + 1;

							if (indexVirgule == suite.length())
								break;
						}
					}

					// Cas particulier : RECORD (parsing manuel des paramètres)
					if (estRecord)
					{
						if (ligneBrute.contains("(") && ligneBrute.contains(")")) 
						{
							String args = ligneBrute.substring(ligneBrute.indexOf('(') + 1,
									ligneBrute.lastIndexOf(')'));
							args = args.trim();

							if (!args.isEmpty()) 
							{
								List<String> params = decoupage(args);
								for (String p : params)
								{
									String trimmed = p.trim();
									List<String> tokens = separerMots(trimmed);
									if (tokens.size() >= 2) 
									{
										// type peut être composé (ex: List<String>), on prend tout sauf le dernier token
										String nom = tokens.get(tokens.size() - 1);
										String type = "";
										for (int i = 0; i < tokens.size() - 1; i++) 
										{
											if (i > 0)
												type += ' ';

											type += tokens.get(i);
										}

										attributs.add(new AttributObjet(nom, "instance", type, "private", false, true));
										HashMap<String, String> emptyParams = new HashMap<>();
										methodes.add(new MethodeObjet(nom, emptyParams, type, "public", false));
									}
								}
							}
						}
						continue;
					}

					continue; // Passer la ligne d'en-tête
				}

				// --- 2. ANALYSE DU CORPS (Attributs et Méthodes) ---
				if (enTeteTrouve)
				{
					boolean estStatique = ligneBrute.contains("static");
					boolean estFinal = ligneBrute.contains("final");
					boolean aVisibilite = (ligneBrute.startsWith("public") || ligneBrute.startsWith("private")
							|| ligneBrute.startsWith("protected"));

					// Si Interface : on cherche les constantes ou les méthodes
					if (estInterface)
					{
						// Détection Constantes d'interface (Attributs)
						if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) 
						{
							// Implicitement PUBLIC STATIC FINAL
							extraireAttribut(ligneBrute, true, true, attributs);
						}
						// Détection Méthodes d'interface
						else if (ligneBrute.contains("(") && ligneBrute.contains(")")) 
						{
							extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
						}
					}
					// Si Classe / Abstract / Enum
					else if (!estRecord)
					{
						// Détection Attribut : finit par ';' et PAS de parenthèses '('
						if (aVisibilite && ligneBrute.endsWith(";") && !ligneBrute.contains("(")) 
						{
							extraireAttribut(ligneBrute, estStatique, estFinal, attributs);
						}
						// Détection Méthode : contient '(''
						else if (aVisibilite && ligneBrute.contains("(") && !ligneBrute.contains("class ")) 
						{
							extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
						}
					}
				}
			}
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Fichier non trouvé: " + chemin);
			return null;
		}

		// --- 3. CRÉATION DE L'OBJET FINAL ---

		// Si c'est une interface, on force une liste d'attributs vide (la logique les a déjà retirés sauf s'ils sont des constantes)

		ClasseObjet nouvelleClasse = new ClasseObjet(attributs, methodes, nomEntite, specifique);

		// Enregistrement des relations pour le contrôleur
		if (estHeritier && nomParent != null)
		{
			lstIntentionHeritage.add(new String[] { nomEntite, nomParent });
		}
		for (String iface : interfacesDetectees)
		{
			lstInterfaces.add(new String[] { nomEntite, iface });
		}

		return nouvelleClasse;
	}

	/**
	 * Extrait un attribut depuis une ligne de code Java.
	 * 
	 * Nettoie l'initialisation (= valeur) et les mots-clés (public, static, final)
	 * pour extraire uniquement le type et le nom de l'attribut.
	 * 
	 * @param ligne       Ligne de code contenant la déclaration de l'attribut
	 * @param estStatique true si l'attribut est statique
	 * @param estFinal    true si l'attribut est final
	 * @param attributs   Liste dans laquelle ajouter l'attribut extrait
	 */
	private void extraireAttribut(String ligne, boolean estStatique, boolean estFinal,
			ArrayList<AttributObjet> attributs) 
	{
		// 1. Ignorer l'initialisation (= ...)
		if (ligne.contains("=")) 
		{
			ligne = ligne.substring(0, ligne.indexOf("=")).trim();
		} 
		else 
		{
			ligne = ligne.replace(";", "").trim();
		}

		List<String> parts = separerMots(ligne);

		// 2. Filtrer les mots-clés pour ne garder que Type et Nom
		List<String> motsUtiles = new ArrayList<>();
		for (String p : parts)
		{
			if (!aModifierMotCle(p)) 
			{
				motsUtiles.add(p);
			}
		}

		if (motsUtiles.size() >= 2) 
		{
			String type = motsUtiles.get(motsUtiles.size() - 2);
			String nom = motsUtiles.get(motsUtiles.size() - 1);

			String visibilite = parts.size() > 0 ? parts.get(0) : "";
			
			if (!aVisibilite(visibilite)) 
			{
				visibilite = "public"; // Cas des constantes d'interface ou visibilité omise
			}

			// Utilisation du nouveau constructeur
			attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique,
					estFinal));
		}
	}

	/**
	 * Extrait une méthode ou un constructeur depuis une ligne de code Java.
	 * 
	 * Analyse la signature de la méthode pour extraire :
	 * - Le nom de la méthode
	 * - La visibilité (public, private, protected)
	 * - Le type de retour (void, String, etc. ou null pour un constructeur)
	 * - Les paramètres (nom et type de chaque paramètre)
	 * - Le modificateur static
	 * 
	 * @param ligne       Ligne de code contenant la signature de la méthode
	 * @param estStatique true si la méthode est statique
	 * @param nomClasse   Nom de la classe contenant la méthode (pour détecter les
	 *                    constructeurs)
	 * @param methodes    Liste dans laquelle ajouter la méthode extraite
	 */
	private void extraireMethode(String ligne, boolean estStatique, String nomClasse,
			ArrayList<MethodeObjet> methodes) 
	{
		int idxParenthOuvrante = ligne.indexOf('(');
		int idxParenthFermante = ligne.lastIndexOf(')');

		if (idxParenthOuvrante == -1 || idxParenthFermante == -1)
			return;

		String signature = ligne.substring(0, idxParenthOuvrante).trim();
		List<String> parts = separerMots(signature);

		if (parts.isEmpty())
			return;

		String visibilite = parts.get(0);
		String nomMethode = parts.get(parts.size() - 1);
		String typeRetour = "void";

		if (nomMethode.equals(nomClasse)) 
		{
			typeRetour = null; // Constructeur
		} 
		else 
		{
			List<String> motsUtiles = new ArrayList<>();
			for (String p : parts)
			{
				if (!aMethodeModif(p)) 
				{
					motsUtiles.add(p);
				}
			}

			if (motsUtiles.size() >= 2) 
			{
				typeRetour = motsUtiles.get(motsUtiles.size() - 2);
			}
		}

		// --- EXTRACTION DES PARAMETRES (parsing manuel) ---
		String paramsStr = ligne.substring(idxParenthOuvrante + 1, idxParenthFermante).trim();
		HashMap<String, String> params = new HashMap<>();

		if (!paramsStr.isEmpty()) 
		{
			List<String> paramList = decoupage(paramsStr);

			for (String param : paramList)
			{
				String p = param.trim();
				if (!p.isEmpty()) 
				{
					int spaceIndex = dernierIndexEspace(p);
					if (spaceIndex > 0)
					{
						String pType = p.substring(0, spaceIndex).trim();
						String pNom = p.substring(spaceIndex + 1).trim();
						params.put(pNom, pType);
					}
				}
			}
		}

		methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
	}

	/**
	 * Détecte les associations entre classes en analysant les attributs.
	 * 
	 * Processus en 2 étapes :
	 * 1. Collecte de toutes les associations unidirectionnelles potentielles
	 * (attributs dont le type est une autre classe du projet)
	 * 2. Fusion des associations bidirectionnelles (A -> B et B -> A)
	 * 
	 * Gère les collections (List, Set, etc.) pour déterminer les multiplicités.
	 * 
	 * @param classes    Liste de toutes les classes analysées
	 * @param mapClasses Map (nom -> classe) pour la résolution rapide
	 * @return Liste des associations détectées
	 */
	public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes,
			HashMap<String, ClasseObjet> mapClasses) 
	{
		List<AssociationObjet> associationsFinales = new ArrayList<>();

		// ÉTAPE 1 : Collecte de toutes les associations unidirectionnelles potentielles
		List<AssociationObjet> associationsUnidirectionnelles = new ArrayList<>();
		HashSet<AssociationObjet> associationsUsedInFusion = new HashSet<>();

		for (ClasseObjet classeOrigine : classes)
		{
			if (classeOrigine.getattributs() == null)
				continue;

			for (AttributObjet attribut : classeOrigine.getattributs()) 
			{
				String typeAttribut = attribut.getType();
				String typeCible = typeAttribut;

				MultipliciteObjet multCible = new MultipliciteObjet(1, 1);
				MultipliciteObjet multOrigine = new MultipliciteObjet(1, 1);

				boolean estCollection = false;

				// Logique de détection Collection/Tableau
				if (typeAttribut.contains("<") && typeAttribut.contains(">")) 
				{
					int idx1 = typeAttribut.indexOf('<');
					int idx2 = typeAttribut.indexOf('>', idx1 + 1);

					if (idx1 != -1 && idx2 != -1 && idx2 > idx1)
					{
						typeCible = typeAttribut.substring(idx1 + 1, idx2).trim();
					}
					estCollection = true;
				} 
				else if (typeAttribut.endsWith("[]")) 
				{
					typeCible = typeAttribut.replace("[]", "").trim();
					estCollection = true;
				}

				if (estCollection)
					multCible = new MultipliciteObjet(0, MULT_INDEFINIE);

				if (mapClasses.containsKey(typeCible) && !typeCible.equals(classeOrigine.getNom())) 
				{
					ClasseObjet classeCible = mapClasses.get(typeCible);

					AssociationObjet associationPotentielle = new AssociationObjet(
							classeCible, classeOrigine, multCible, multOrigine, attribut.getNom(), true);
					associationsUnidirectionnelles.add(associationPotentielle);
				}
			}
		}

		// ÉTAPE 2 : Fusion des associations bidirectionnelles (A <-> B)
		HashSet<String> pairesDeClassesFusionnees = new HashSet<>();

		for (AssociationObjet assoOrigine : associationsUnidirectionnelles)
		{
			// Si cette association a déjà été marquée comme utilisée, on passe
			if (associationsUsedInFusion.contains(assoOrigine))
				continue;

			// Récupérer les informations de A -> B
			ClasseObjet classeA = assoOrigine.getClasseFille(); // A
			ClasseObjet classeB = assoOrigine.getClasseMere(); // B

			// Créer une clé pour la paire non ordonnée
			String nomA = classeA.getNom();
			String nomB = classeB.getNom();
			// Utiliser l'ordre alphabétique pour la clé de la paire
			String clePaire = (nomA.compareTo(nomB) < 0) ? nomA + ":" + nomB : nomB + ":" + nomA;

			// Si une fusion pour cette paire de classes a déjà été ajoutée, on passe (pour
			// la gestion des liens multiples comme centre/points)
			if (pairesDeClassesFusionnees.contains(clePaire)) 
			{
				continue;
			}

			// 2. Chercher B -> A dans le reste de la liste
			AssociationObjet assoInverse = null;
			for (AssociationObjet a : associationsUnidirectionnelles)
			{
				// Vérifier si c'est l'inverse (B -> A) ET si elle n'a pas déjà été utilisée
				if (!associationsUsedInFusion.contains(a) &&
						a.getClasseFille() == classeB && a.getClasseMere() == classeA) 
				{
					assoInverse = a;
					break;
				}
			}

			if (assoInverse != null)
			{
				// Marquer les deux associations comme utilisées
				associationsUsedInFusion.add(assoOrigine);
				associationsUsedInFusion.add(assoInverse);

				// Marquer la paire de classes comme fusionnée
				pairesDeClassesFusionnees.add(clePaire);

				MultipliciteObjet multA_role = assoInverse.getMultDest();
				MultipliciteObjet multB_role = assoOrigine.getMultDest();

				// Créer la nouvelle association bidirectionnelle
				AssociationObjet assoFinale = new AssociationObjet(
						classeB, // classeMere (Destination B)
						classeA, // classeFille (Origine A)
						multB_role, // multiDest (Pour B)
						multA_role, // multiOrig (Pour A)
						null, // Nom de l'attribut (omettable)
						false // NON unidirectionnelle -> BIDIRECTIONNELLE
				);
				associationsFinales.add(assoFinale);
			}
		}

		// ÉTAPE 3 : Ajout des associations unidirectionnelles restantes

		for (AssociationObjet asso : associationsUnidirectionnelles)
		{
			// Si l'association n'a pas été utilisée pour former un lien bidirectionnel, on
			// l'ajoute.
			if (!associationsUsedInFusion.contains(asso)) 
			{
				associationsFinales.add(asso);
			}
		}

		// Retourner la liste finale
		return associationsFinales;
	}

	/**
	 * Liste tous les fichiers Java (.java) d'un dossier.
	 * 
	 * @param cheminDossier Chemin absolu vers le dossier à parcourir
	 * @return Liste des fichiers Java trouvés
	 */
	public ArrayList<File> ClassesDuDossier(String cheminDossier)
	{
		File dossier = new File(cheminDossier);
		File[] tousLesFichiers = dossier.listFiles();
		ArrayList<File> fichiersJava = new ArrayList<>();

		if (tousLesFichiers != null)
		{
			for (File f : tousLesFichiers)
			{
				if (f.getName().endsWith(".java")) 
				{
					fichiersJava.add(f);
				}
			}
		}
		return fichiersJava;
	}

	// -------------------- Méthodes d'aide pour le parsing manuel
	// --------------------

	/**
	 * Sépare une chaîne en mots (tokens) en utilisant les espaces comme
	 * séparateurs.
	 * 
	 * @param s Chaîne à décomposer
	 * @return Liste des mots extraits
	 */
	private List<String> separerMots(String s)
	{
		List<String> tokens = new ArrayList<>();
		String token = "";
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) 
			{
				if (!token.isEmpty()) 
				{
					tokens.add(token);
					token = "";
				}
			} 
			else 
			{
				token += c;
			}
		}

		if (!token.isEmpty()) 
		{
			tokens.add(token);
		}

		return tokens;
	}

	/**
	 * Trouve l'index du premier espace dans une chaîne.
	 * 
	 * @param s Chaîne à analyser
	 * @return Index du premier espace, ou -1 si aucun espace trouvé
	 */
	private int indexEspace(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			if (Character.isWhitespace(s.charAt(i)))
				return i;
		}
			
		return -1;
	}

	/**
	 * Trouve l'index du dernier espace dans une chaîne.
	 * 
	 * @param s Chaîne à analyser
	 * @return Index du dernier espace, ou -1 si aucun espace trouvé
	 */
	private int dernierIndexEspace(String s)
	{
		for (int i = s.length() - 1; i >= 0; i--)
		{   
			 if (Character.isWhitespace(s.charAt(i)))
				return i;
		}
		   
		return -1;
	}

	/**
	 * Vérifie si un mot est un modificateur d'attribut.
	 * 
	 * @param s Mot à vérifier
	 * @return true si c'est un modificateur (public, private, static, final, etc.)
	 */
	private boolean aModifierMotCle(String s)
	{
		return s.equals("public") || s.equals("private") || s.equals("protected") ||
				s.equals("static") || s.equals("final") || s.equals("transient") || s.equals("volatile");
	}

	/**
	 * Vérifie si un mot est un modificateur de méthode.
	 * 
	 * @param s Mot à vérifier
	 * @return true si c'est un modificateur (public, static, abstract,
	 *         synchronized, etc.)
	 */
	private boolean aMethodeModif(String s)
	{
		return s.equals("public") || s.equals("private") || s.equals("protected") ||
				s.equals("static") || s.equals("final") || s.equals("abstract") ||
				s.equals("synchronized") || s.equals("default");
	}

	/**
	 * Vérifie si un mot est un modificateur de visibilité.
	 * 
	 * @param s Mot à vérifier
	 * @return true si c'est un modificateur de visibilité (public, private,
	 *         protected)
	 */
	private boolean aVisibilite(String s)
	{
		return s.equals("public") || s.equals("private") || s.equals("protected");
	}

	/**
	 * Lit le premier identifiant dans une chaîne.
	 * 
	 * S'arrête au premier séparateur rencontré (espace, '{', '<', ',', '(').
	 * 
	 * @param s Chaîne à analyser
	 * @return Premier identifiant extrait
	 */
	private String lireNom(String s)
	{
		s = s.trim();
		if (s.isEmpty())
			return "";

		String id = "";
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);

			// Si on rencontre un séparateur, on s'arrête
			if (Character.isWhitespace(c) || c == '{' || c == '<' || c == ',' || c == '(') 
			{
				break;
			}

			id += c; // concaténation volontaire
		}
		return id;
	}

	/**
	 * Découpe une chaîne par virgules en respectant les génériques.
	 * 
	 * Ignore les virgules à l'intérieur des chevrons (< >) pour gérer correctement
	 * les types génériques comme List<String>, Map<String, Integer>, etc.
	 * 
	 * @param s Chaîne à découper (par exemple liste de paramètres ou d'interfaces)
	 * @return Liste des fragments découpés
	 */
	private List<String> decoupage(String s)
	{
		List<String> parts = new ArrayList<>();
		String part = "";
		int depthAngle = 0;
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if (c == '<')
			{
				depthAngle++;
				part += c;
			} 
			else if (c == '>')
			{
				if (depthAngle > 0)
					depthAngle--;
				part += c;
			} 
			else if (c == ',' && depthAngle == 0)
			{
				parts.add(part);
				part = "";
			} 
			else 
			{
				part += c;
			}
		}

		if (!part.isEmpty())
			parts.add(part);
	
		return parts;
	}
}