package modele;


/**
 * Représente une relation d'héritage entre deux classes d'un diagramme UML.
 * Dans une relation d'héritage :
 * - La classe d'origine (classeOrig) est la classe enfant (subclass).
 * - La classe de destination (classeDest) est la classe parent (superclass).
 */
public class HeritageObjet extends LiaisonObjet 
{
    /**
     * Constructeur d'une relation d'héritage.
     *
     * @param classeMere   La classe mere  (super-classe).
     * @param classeFille  La classe fille (sous-classe).
     * @param nomRelation  Le nom de la relation (souvent vide pour un héritage).
     */
    public HeritageObjet(String nomAttribut, ClasseObjet classeMere, ClasseObjet classeFille) 
    {
        super(nomAttribut, classeMere, classeFille);
    }


    /**
     * Retourne une représentation textuelle simple de l'héritage.
     *
     * @return une chaîne du type : "Fille hérite de Mere".
     */
    public String toString() 
    {
        return classeFille.getNom() + " hérite de " + classeMere.getNom();
    }
     
}
