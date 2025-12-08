package modele;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.lang.String;
import java.lang.System;

public class LectureFichier 
{
    private String nomDeLaClasse = ""; 

    private List<AttributObjet> listeAttributs;
    private List<MethodeObjet>  listeMethodeObjets;

    /**
     * Nettoie un mot des symboles de ponctuation non alphanumériques.
     */
    private String nettoyerMot(String mot) 
    {
        return mot.replaceAll("[^a-zA-Z0-9_<>.]", ""); 
    }
    
    /**
     * Analyse et convertit la chaîne de paramètres bruts en une liste d'objets AttributObjet.
     */
    private List<AttributObjet> analyserParametres(String chaineParametresBruts)
    {
        List<AttributObjet> listeParams = new ArrayList<>();
        if (chaineParametresBruts.isEmpty()) 
        {
            return listeParams;
        }

        // Utilisation du split, méthode simple mais efficace ici
        String[] defsParams = chaineParametresBruts.trim().split(",");

        for (String defParam : defsParams) 
        {
            if (defParam.trim().isEmpty()) continue;

            String[] mots = defParam.trim().split("\\s+");
            
            if (mots.length >= 2) 
            {
                String pType = mots[mots.length - 2];
                String pNom = mots[mots.length - 1];
                listeParams.add(new AttributObjet(pNom, pType, "", "")); 
            }
        }
        return listeParams;
    }


    // --- Constructeur Principal (Logique d'analyse) ---

    public LectureFichier(String nomFichier) 
    {
        this.listeAttributs  = new ArrayList<AttributObjet>();
        this.listeMethodeObjets   = new ArrayList<MethodeObjet>();

        File fichier = new File(nomFichier);
        if (!fichier.exists()) 
        {
            System.err.println("Erreur : Fichier non trouvé : " + fichier.getAbsolutePath());
            return;
        }

        // 1. Recherche du nom de la classe
        try (Scanner scannerFichier = new Scanner(fichier))
        {
            while (scannerFichier.hasNext())
            {
                String mot = scannerFichier.next();
                if (mot.equals("class") && scannerFichier.hasNext())
                {
                    nomDeLaClasse = nettoyerMot(scannerFichier.next());
                    break; 
                }
            }
        }
        catch (FileNotFoundException e) 
        {  
            e.printStackTrace();
        }

        // 2. Analyse Principale des Membres
        try (Scanner scannerFichier = new Scanner(fichier))
        {
            String visibilite = "";
            String portee = "instance";
            String typeDeRetour = "";
            boolean estDeclaration = false;
            
            while (scannerFichier.hasNext()) 
            {
                String mot = scannerFichier.next();
                
                // 1. Détection des modificateurs
                if (mot.equals("public") || mot.equals("private") || mot.equals("protected"))
                {
                    visibilite = mot;
                    portee = "instance"; 
                    typeDeRetour = ""; 
                    estDeclaration = true;
                    continue;
                }
                
                if (mot.equals("static") && estDeclaration)
                {
                    portee = "classe (static)";
                    continue;
                }
                
                // 2. Détection du type
                if (estDeclaration && typeDeRetour.isEmpty() && !mot.contains("class") && !mot.contains("interface"))
                {
                    typeDeRetour = nettoyerMot(mot);
                    continue;
                }
                
                // 3. Traitement de la déclaration
                if (estDeclaration && !typeDeRetour.isEmpty())
                {
                    String nomEtSymbole = mot;
                    
                    // --- Cas A : Attribut ---
                    if (nomEtSymbole.contains(";") && !nomEtSymbole.contains("("))
                    {
                        String nomAttribut = nettoyerMot(nomEtSymbole.replace(";", ""));
                        if (!nomAttribut.isEmpty())
                        {
                            AttributObjet nouvelAttribut = new AttributObjet(nomAttribut, typeDeRetour, visibilite, portee);
                            this.listeAttributs.add(nouvelAttribut);
                        }
                    }
                    
                    // --- Cas B : Méthode ou Constructeur ---
                    else if (nomEtSymbole.contains("("))
                    {
                        String nomMethodeObjet = nettoyerMot(nomEtSymbole.substring(0, nomEtSymbole.indexOf("(")));
                        
                        // Si le nom de la méthode est vide, le nom est le type de retour capturé précédemment (cas rare)
                        if (nomMethodeObjet.isEmpty() && !typeDeRetour.isEmpty()) {
                            nomMethodeObjet = typeDeRetour;
                            // Pour les setters (void), le vrai type est le mot capturé avant, mais on garde "void" pour l'affichage.
                        }
                        
                        // Extraction des paramètres : Lecture continue jusqu'à ')'
                        String paramsBruts = nomEtSymbole.substring(nomEtSymbole.indexOf("(") + 1);
                        
                        while (scannerFichier.hasNext())
                        {
                            String jetonActuel = scannerFichier.next();
                            if (jetonActuel.contains(")")) 
                            {
                                paramsBruts += jetonActuel.substring(0, jetonActuel.indexOf(")")).trim();
                                break;
                            }
                            paramsBruts += jetonActuel + " ";
                        }
                        
                        // Analyse des paramètres par la méthode privée
                        List<AttributObjet> listeParams = analyserParametres(paramsBruts);

                        // Création de l'objet MethodeObjet
                        String typeRetourMethodeObjet = nomMethodeObjet.equals(nomDeLaClasse) ? "Constructeur" : typeDeRetour;
                        MethodeObjet nouvelleMethodeObjet = new MethodeObjet(nomMethodeObjet, typeRetourMethodeObjet, visibilite, listeParams);
                        this.listeMethodeObjets.add(nouvelleMethodeObjet);
                    }
                    
                    // Réinitialisation des variables
                    visibilite = "";
                    portee = "instance";
                    typeDeRetour = "";
                    estDeclaration = false;
                }
            }
        } 
        catch (Exception e) 
        {
            System.err.println("Erreur critique inattendue lors de l'analyse du fichier (vérifiez le format):");
            e.printStackTrace();
        }        
    }
    
    // --- Affichage (toString) ---

    public String toString()
    {
        String resultat = ""; 
        
        // --- 1. AFFICHAGE DES ATTRIBUTS ---
        int compteurAttr = 1;
        
        for (AttributObjet attr : this.listeAttributs)
        {
            String porteeAffichee = attr.getPortee().equals("classe (static)") ? "statique" : "instance";
            String visibiliteAffichee = attr.getVisibilite().equals("private") ? "privée" : attr.getVisibilite();

            // CONCATÉNATION DE L'ATTRIBUT
            resultat += "attribut : " + compteurAttr++ + " nom : " + attr.getNom() + 
                        " type : " + attr.getType() + 
                        " visibilité : " + visibiliteAffichee + 
                        " portée : " + porteeAffichee + "\n";
        }

        // --- 2. AFFICHAGE DES MÉTHODES ET CONSTRUCTEURS ---
        
        for (MethodeObjet methode : this.listeMethodeObjets)
        {
            String nomAffiche = methode.getTypeRetour().equals("Constructeur") ? "Constructeur" : methode.getNom();
            String typeRetourAffichee;
            
            if (methode.getTypeRetour().equals("Constructeur"))
            {
                typeRetourAffichee = "";
            }
            else
            {
                String typeRetourValue = methode.getTypeRetour();
                
                // Uniformiser "int" -> "entier" et "void" -> "aucun"
                if (typeRetourValue.equals("int")) {
                    typeRetourValue = "entier";
                }
                
                typeRetourAffichee = typeRetourValue.equals("void") ? "type de retour : aucun" : 
                                     "type de retour : " + typeRetourValue;
            }
            
            // Ligne de la méthode/constructeur
            resultat += "méthode : " + nomAffiche + 
                        " visibilité : " + methode.getVisibilite() + 
                        (typeRetourAffichee.isEmpty() ? "" : " ") + 
                        typeRetourAffichee + "\n";
            
            // Lignes des paramètres
            List<AttributObjet> params = methode.getListeAttributObjets();
            if (params.isEmpty())
            {
                resultat += "paramètres : aucun\n";
            }
            else
            {
                for (int i = 0; i < params.size(); i++)
                {
                    AttributObjet param = params.get(i);
                    // Ligne de paramètre
                    resultat += "paramètres : p" + (i + 1) + 
                                " : " + param.getNom() + 
                                " type : " + param.getType() + "\n";
                }
            }
        }
        
        return resultat;
    }

    public static void main(String[] args) 
    {
        if (args.length == 0) 
        {
            System.out.println("Usage: java LectureFichier <nomFichierJava>");
            return;
        }

        System.out.println("--- ANALYSE DU FICHIER SOURCE (Étape 1) ---");
        LectureFichier analyseur = new LectureFichier(args[0]);
        System.out.print(analyseur.toString());
    }
}