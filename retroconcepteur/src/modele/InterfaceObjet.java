package src.modele;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une relation d'implémentation d'une interface dans un diagramme UML.
 * MODIFIÉ : classeFille est la classe concrète unique. lstInterfaces est la liste des interfaces implémentées.
 */
public class InterfaceObjet extends LiaisonObjet 
{
    // Rétablissement de la liste pour stocker plusieurs interfaces
    private List<ClasseObjet> lstInterfaces; 

     /**
     * Constructeur d'initialisation pour la classe concrète.
     * La classe Mere (Parent) est null car plusieurs interfaces seront ajoutées.
     *
     * @param classeFille      La classe concrète (classe fille)
     */
    public InterfaceObjet(ClasseObjet classeFille) 
    {
        // On passe null pour classeMere car il y en aura potentiellement plusieurs.
        super(null, null, classeFille); 
        this.lstInterfaces = new ArrayList<>();
    }
    
    /**
     * Ajoute une interface à la liste des implémentations.
     * @param interfaceObjet L'interface (qui est un ClasseObjet) à ajouter.
     */
    public void ajouterInterface(ClasseObjet interfaceObjet)
    {
        if (interfaceObjet != null)
        {
            this.lstInterfaces.add(interfaceObjet);
        }
    }
    
    /**
     * Retourne une représentation textuelle de toutes les relations d'implémentation.
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
                    sRet+= " , " ; // Utilisation de ' , ' pour plus de clarté
                }
            }
        }

        return String.format("%-10s", nomFille) + " implémente " + sRet;
    }
     
}