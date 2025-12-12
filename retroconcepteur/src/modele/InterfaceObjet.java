package src.modele;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une relation d'implémentation d'interfaces dans un diagramme UML.
 * 
 * Une classe concrète (classeFille) peut implémenter plusieurs interfaces.
 * Cette classe stocke la liste de toutes les interfaces implémentées.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class InterfaceObjet extends LiaisonObjet
{
    // Rétablissement de la liste pour stocker plusieurs interfaces
    private List<ClasseObjet> lstInterfaces;

    /**
     * Constructeur d'une relation d'implémentation d'interfaces.
     * 
     * Initialise la relation pour une classe concrète.
     * Les interfaces seront ajoutées ensuite via ajouterInterface().
     *
     * @param classeFille La classe concrète qui implémente des interfaces
     */
    public InterfaceObjet(ClasseObjet classeFille) 
{
        // On passe null pour classeMere car il y en aura potentiellement plusieurs.
        super(null, null, classeFille);
        this.lstInterfaces = new ArrayList<>();
    }

    /**
     * Ajoute une interface à la liste des implémentations.
     * 
     * @param interfaceObjet L'interface (représentée par un ClasseObjet) à ajouter
     */
    public void ajouterInterface(ClasseObjet interfaceObjet) 
{
        if (interfaceObjet != null) 
{
            this.lstInterfaces.add(interfaceObjet);
        }
    }

    /**
     * Retourne une représentation textuelle de toutes les relations
     * d'implémentation.
     * Format attendu: "Classe Concrète implémente Interface1, Interface2, ..."
     */
    @Override
    public String toString() 
{
        // 1. Récupérer le nom de la classe concrète
        String nomFille = (this.classeFille != null) ? this.classeFille.getNom() : "[Classe Concrète manquante]";

        if (this.lstInterfaces.isEmpty()) 
{
            return nomFille + " n'implémente aucune interface enregistrée.";
        }

        // 2. Assembler les noms des interfaces séparés par des virgules
        String sRet = "";

        for (int i = 0; i < this.lstInterfaces.size(); i++) 
{
            if (this.lstInterfaces.get(i) != null) 
{
                sRet += this.lstInterfaces.get(i).getNom();
                if (i < this.lstInterfaces.size() - 1) 
{
                    sRet += " , "; // Utilisation de ' , ' pour plus de clarté
                }
            }
        }

        return String.format("%-10s", nomFille) + " implémente " + sRet;
    }

    /**
     * Retourne la liste des interfaces implémentées par la classe concrète.
     * 
     * @return la liste des interfaces
     */
    public List<ClasseObjet> getLstInterfaces() 
{
        return this.lstInterfaces;
    }

}
