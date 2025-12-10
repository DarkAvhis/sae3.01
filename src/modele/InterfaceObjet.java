package modele;


/**
 * Représente une relation d'implémentation d'une interface dans un diagramme UML.
 * Dans cette relation :
 * classeDest : la classe qui implémente l'interface (la classe concrète)</li>
 * classeOrig : l'interface ou classe abstraite implémentée</li>
 */
public class InterfaceObjet extends LiaisonObjet 
{
     /**
     * Crée une relation d'implémentation entre une classe et une interface.
     *
     * @param nomAttribut      Le nom de la relation (souvent vide pour l'implémentation)
     * @param classeDest       La classe concrète qui implémente l'interface
     * @param classeOrig       L'interface ou classe abstraite implémentée
     */
    public InterfaceObjet( String nomAttribut, ClasseObjet classeMere, ClasseObjet classeFille) 
    {
        super(nomAttribut, classeMere, classeFille);
    }

    /**
     * Retourne une représentation textuelle de la relation d'implémentation.
     *
     * @return une chaîne du type : "ClasseDest implémente ClasseOrig"
     */
    @Override
    public String toString() 
    {
        return classeMere.getNom() + "\timplémente " + classeFille.getNom();
    }
     
}
