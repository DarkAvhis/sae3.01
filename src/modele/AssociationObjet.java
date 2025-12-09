package modele;


/**
 * Représente une association entre deux classes dans un diagramme UML.
 * 
 * Une association peut être unidirectionnelle ou bidirectionnelle, 
 * et possède des multiplicité pour l'origine et la destination.
 */
public class AssociationObjet extends LiaisonObjet 
{
    /**
     * Indique si l'association est unidirectionnelle (true) ou bidirectionnelle (false).
     */
	private boolean estUnidirectionnel;

    /*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
	private boolean unidirectionnel;
	private MultipliciteObjet multOrig;
	private MultipliciteObjet multDest;

	/*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
    /**
     * Multiplicité de l'origine de l'association.
     * Multiplicité de la destination de l'association.
     */
    private MultipliciteObjet multiDest;
	private MultipliciteObjet multiOrig;

	/**
     * Constructeur de l'association.
     * 
     * @param classeDest Classe cible de l'association
     * @param classeOrig Classe origine de l'association
     * @param multiDest Multiplicité de la classe cible
     * @param multiOrig Multiplicité de la classe origine
     * @param nomAttribut Nom de l'attribut représentant l'association
     * @param unidirectionnel Vrai si l'association est unidirectionnelle
     */
    public AssociationObjet(ClasseObjet classeDest, ClasseObjet classeOrig, MultipliciteObjet multDest, MultipliciteObjet multOrig, String nomAttribut, boolean unidirectionnel) 
    {
        super(nomAttribut, classeDest, classeOrig);
        this.multiDest          = multDest;
        this.multiOrig          = multOrig;
        this.estUnidirectionnel = unidirectionnel;
    }

    /*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    public MultipliciteObjet getMultOrig       () {    return multOrig        ;   }
    public MultipliciteObjet getMultDest       () {    return multDest        ;   }
    public boolean           getUnidirectionnel() {    return unidirectionnel ;   }

    /*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
    public void setMultOrig       (MultipliciteObjet multOrig) {    this.multOrig        = multOrig        ;   }
    public void setMultDest       (MultipliciteObjet multDest) {    this.multDest        = multDest        ;   }
    public void setUnidirectionnel(boolean unidirectionnel   ) {    this.unidirectionnel = unidirectionnel ;   }

    /*-------------------------------------- */
	/* toString                              */
	/*-------------------------------------- */
   
    @Override
    public String toString() 
    {
        String sens    = (this.estUnidirectionnel) ? "unidirectionnelle"       : "bidirectionnelle";
        String origine = (this.classeOrig != null) ? this.classeOrig.getNom()  : "?";
        String dest    = (this.classeDest != null) ? this.classeDest.getNom()  : "?";
        String multO   = (this.multiOrig  != null) ? this.multiOrig.toString() : "?";
        String multD   = (this.multiDest  != null) ? this.multiDest.toString() : "?";

        return String.format("Association %d : %s de %s(%s) vers %s(%s)",
                             this.getNum(), sens, origine, multO, dest, multD);
    }
     
}
