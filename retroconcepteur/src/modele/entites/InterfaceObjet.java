package src.modele.entites;


import java.util.ArrayList;
import java.util.List;

/**
 * Représente une relation d'implémentation d'une interface dans un diagramme UML.
 * MODIFIÉ : classeFille est la classe concrète unique. lstInterfaces est la liste des interfaces implémentées.
 */
public class InterfaceObjet extends LiaisonObjet 
{

    /*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
    private List<ClasseObjet> lstInterfaces; 

    /*-------------------------------------- */
	/* Constructeurs                         */
	/*-------------------------------------- */
    public InterfaceObjet(ClasseObjet classe) 
    {
        super(classe); 
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

    public List<ClasseObjet> getLstInterfaces()
    {
        return this.lstInterfaces;
    }
    
    /*-------------------------------------- */
	/* toString                              */
	/*-------------------------------------- */
    /**
     * Retourne une représentation textuelle de toutes les relations d'implémentation.
     * Format attendu: "Classe Concrète implémente Interface1, Interface2, ..."
     */
    public String toString() 
    {
        String nomFille = (this.classeFille != null) ? this.classeFille.getNom() : "[Classe Concrète manquante]";
        
        if (this.lstInterfaces.isEmpty())
        {
            return nomFille + " n'implémente aucune interface enregistrée.";
        }

        String sRet = "";
        
        for (int i = 0; i < this.lstInterfaces.size(); i++)
        {
            if (this.lstInterfaces.get(i) != null)
            {
                sRet += this.lstInterfaces.get(i).getNom();
                if (i < this.lstInterfaces.size() - 1)
                {
                    sRet+= " , " ; 
                }
            }
        }

        return String.format("%-10s", nomFille) + " implémente " + sRet;
    }
}