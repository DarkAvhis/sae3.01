package modele;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class AnalyseurUML
{

	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage: java AnalyseurUML <chemin_du_fichier_java>");
			return;
		}

		AnalyseurUML analyseur = new AnalyseurUML();
		String chemin = args[0];
		ClasseObjet classeResultat = analyseur.analyserFichierUnique(chemin);

		
		if (classeResultat != null)
		{
			System.out.println(classeResultat.toString()); 
		}
	}

	public ClasseObjet analyserFichierUnique(String chemin)
	{
		File f = new File(chemin);
		String nomClasse = f.getName().replace(".java", "");

		ArrayList<AttributObjet> attributs = new ArrayList<>();
		ArrayList<MethodeObjet> methodes = new ArrayList<>();

		try (Scanner sc = new Scanner(f))
		{
			while (sc.hasNextLine())
			{
				String ligne = sc.nextLine().trim();

				if (ligne.startsWith("private") && ligne.endsWith(";"))
				{
					String[] parts = ligne.split("\\s+");
					if (parts.length >= 3)
					{
						String type = parts[1];
						String nom = parts[2].replace(";", "");
						attributs.add(new AttributObjet(nom, "instance", type, "private"));
					}
				}

				else if ( (ligne.startsWith("public") || ligne.startsWith("private") || ligne.startsWith("protected"))
						&& ligne.contains("(") && !ligne.contains("class "))
				{
					String avantParenthese = ligne.substring(0, ligne.indexOf("(")).trim();
					String[] parts = avantParenthese.split("\\s+");

					if (parts.length >= 2)
					{
						String visibilite = parts[0];
						String nomMethode = parts[parts.length - 1];
						String typeRetour = parts[parts.length - 2];

						// --- EXTRACTION DES PARAMETRES ---
						String contenuParam = ligne.substring(
								ligne.indexOf("(") + 1,
								ligne.lastIndexOf(")")
						).trim();

						HashMap<String, String> params = new HashMap<>();

						if (!contenuParam.isEmpty())
						{
							// Plusieurs paramètres : séparés par virgules
							String[] listeParams = contenuParam.split(",");

							for (String p : listeParams)
							{
								p = p.trim(); 
								// Exemple : "HashMap<String, String> parametre"

								int spaceIndex = p.lastIndexOf(" ");
								if (spaceIndex > 0)
								{
									String type = p.substring(0, spaceIndex).trim();
									String name = p.substring(spaceIndex + 1).trim();
									params.put(name, type);
								}
							}
						}

						// --- AJOUT : HashMap n’est jamais nulle maintenant ---
						methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite));
					}
				}

			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Fichier non trouvé.");
			return null;
		}

		return new ClasseObjet(attributs, methodes, nomClasse);
	}    

	public List<File> ClassesDuDossier(String cheminDossier)
	{
		File dossier = new File(cheminDossier);
		File[] tousLesFichiers = dossier.listFiles();
		List<File> fichiersJava = new ArrayList<>();
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
}