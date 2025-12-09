package modele;


/**
 * Représente la multiplicité d'une relation UML.
 * 
 * La multiplicité indique combien d'instances d'une classe sont liées à une autre classe.
 * Par exemple :
 * 
 * 1..1 : exactement une instance</li>
 * 0..* : zéro ou plusieurs instances</li>
 * 1..* : au moins une instance</li>
 * La valeur spéciale {@code 999999999} est utilisée pour représenter "*" (infini).
 */
public class MultipliciteObjet 
{
    /** Début de la multiplicité (valeur minimale) */
    private int debut;

    /** Fin de la multiplicité (valeur maximale) */
    private int fin;

    // ----------------- Constructeur -----------------

    /**
     * Crée un objet multiplicité.
     *
     * @param debut Valeur minimale de la multiplicité
     * @param fin Valeur maximale de la multiplicité (999999999 = infini "*")
     */
    public MultipliciteObjet(int debut, int fin) 
    {
        this.debut = debut;
        this.fin   = fin;
    }

    // ----------------- Getters -----------------

    /** @return Valeur minimale de la multiplicité */
    public int getDebut() { return debut; }

    /** @return Valeur maximale de la multiplicité */
    public int getFin() { return fin; }

    // ----------------- Setters -----------------

    /** @param debut Nouvelle valeur minimale de la multiplicité */
    public void setDebut(int debut) { this.debut = debut; }

    /** @param fin Nouvelle valeur maximale de la multiplicité */
    public void setFin(int fin) { this.fin = fin; }


    // ----------------- toString -----------------

    /**
     * Retourne une représentation textuelle de la multiplicité.
     * 
     * Exemples :
     * 
     *     1..1
     *     0..*
     *     1..*
     *
     * @return Chaîne représentant la multiplicité
     */
    @Override
    public String toString()
    {
        if (this.debut == 999999999) return "*";
        if (this.fin   == 999999999) return this.debut + "..*";
        
        return this.debut + ".." + this.fin;
    }
}
