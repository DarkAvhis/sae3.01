package modele.entites;

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
    /*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
    private int debut;
    private int fin;

    /*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
    public MultipliciteObjet(int debut, int fin) 
    {
        this.debut = debut;
        this.fin   = fin;
    }

    /*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    public int getDebut() { return this.debut ; }
    public int getFin  () { return this.fin   ; }

    /*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
    public void setDebut(int debut) { this.debut = debut  ; }
    public void setFin  (int fin  ) { this.fin   = fin    ; }

    /*-------------------------------------- */
	/* toString                              */
	/*-------------------------------------- */

    /**
     * Retourne une représentation textuelle de la multiplicité.
     * 
     * Exemples :
     * 
     *     1..1
     *     0..*
     *     1..*
     * 
     * note : 999999999 --> représente l'étoile 
     *
     * @return Chaîne représentant la multiplicité
     */
    public String toString()
    {
        if (this.debut == 999999999) return "*";
        if (this.fin   == 999999999) return this.debut + "..*";
        
        return this.debut + ".." + this.fin;
    }
}