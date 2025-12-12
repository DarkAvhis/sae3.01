package src.modele;

/**
 * Représente une relation d'héritage entre deux classes d'un diagramme UML.
 * 
 * Dans une relation d'héritage :
 * - La classe fille (classeFille) est la classe enfant qui hérite
 * - La classe mère (classeMere) est la classe parent dont on hérite
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class HeritageObjet extends LiaisonObjet
{

	/**
	 * Constructeur d'une relation d'héritage.
	 * 
	 * @param classeDest Classe parent (superclasse)
	 * @param classeOrig Classe enfant (sous-classe)
	 */
	public HeritageObjet(ClasseObjet classeDest, ClasseObjet classeOrig) 
	{
		super(null, classeDest, classeOrig);
	}

	/**
	 * Retourne une représentation textuelle simple de l'héritage.
	 *
	 * @return une chaîne du type : "Fille hérite de Mere".
	 */
	public String toString() 
	{
		return String.format("%-10s", classeFille.getNom()) + " hérite de " + classeMere.getNom();
	}
}