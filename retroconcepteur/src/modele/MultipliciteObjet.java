package src.modele;

/**
 * Représente la multiplicité d'une relation UML.
 * 
 * La multiplicité indique le nombre d'instances d'une classe pouvant participer
 * à une relation avec une autre classe.
 * 
 * Exemples courants :
 * - 1..1 : exactement une instance (relation obligatoire unique)
 * - 0..1 : zéro ou une instance (relation optionnelle unique)
 * - 0..* : zéro ou plusieurs instances (collection optionnelle)
 * - 1..* : au moins une instance (collection obligatoire)
 * 
 * La valeur spéciale 999999999 représente "*" (infini).
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class MultipliciteObjet
{
	/** Début de la multiplicité (valeur minimale) */
	private int debut;

	/** Fin de la multiplicité (valeur maximale) */
	private int fin;

	// ----------------- Constructeur -----------------

	/**
	 * Constructeur d'une multiplicité.
	 *
	 * @param debut Valeur minimale (borne inférieure) de la multiplicité
	 * @param fin   Valeur maximale (borne supérieure) de la multiplicité (utiliser
	 *              999999999 pour "*")
	 */
	public MultipliciteObjet(int debut, int fin) 
	{
		this.debut = debut;
		this.fin = fin;
	}

	// ----------------- Getters -----------------

	/** @return Valeur minimale de la multiplicité */
	public int getDebut() 
	{
		return debut;
	}

	/** @return Valeur maximale de la multiplicité */
	public int getFin() 
	{
		return fin;
	}

	// ----------------- Setters -----------------

	/** @param debut Nouvelle valeur minimale de la multiplicité */
	public void setDebut(int debut) 
	{
		this.debut = debut;
	}

	/** @param fin Nouvelle valeur maximale de la multiplicité */
	public void setFin(int fin) 
	{
		this.fin = fin;
	}

	// ----------------- toString -----------------

	/**
	 * Retourne une représentation textuelle de la multiplicité.
	 * 
	 * Exemples :
	 * 
	 * 1..1
	 * 0..*
	 * 1..*
	 *
	 * @return Chaîne représentant la multiplicité
	 */
	@Override
	public String toString() 
	{
		if (this.debut == 999999999)
			return "*";
		if (this.fin == 999999999)
			return this.debut + "..*";

		return this.debut + ".." + this.fin;
	}
}
