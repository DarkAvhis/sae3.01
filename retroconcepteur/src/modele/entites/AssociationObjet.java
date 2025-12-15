package src.modele.entites;


/**
 * Représente une association entre deux classes dans un diagramme UML.
 */
public class AssociationObjet extends LiaisonObjet 
{
    // Suppression des attributs masqués (classeDest, classeOrig)
    
	private boolean estUnidirectionnel;
    private MultipliciteObjet multiDest;
	private MultipliciteObjet multiOrig;

	/*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
    
    // Le constructeur reçoit classeDest et classeOrig (qui deviennent classeMere et classeFille dans LiaisonObjet)
    public AssociationObjet(ClasseObjet classeMere, ClasseObjet classeFille, MultipliciteObjet multiDest, MultipliciteObjet multiOrig, String nomAttribut, boolean unidirectionnel) 
    {
        // Appel au constructeur de LiaisonObjet
        super(nomAttribut, classeMere, classeFille);
        this.multiDest          = multiDest;
        this.multiOrig          = multiOrig;
        this.estUnidirectionnel = unidirectionnel;      
    }

    /*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    public MultipliciteObjet getMultOrig       () {  return this.multiOrig          ; }
    public MultipliciteObjet getMultDest       () {  return this.multiDest          ; }
    public boolean           getUnidirectionnel() {  return this.estUnidirectionnel ; } 
    
    /*-------------------------------------- */
	/* Les Modificateur                      */
	/*-------------------------------------- */
    public void setMultOrig       (MultipliciteObjet multOrig) {    this.multiOrig          = multOrig        ;   }
    public void setMultDest       (MultipliciteObjet multDest) {    this.multiDest          = multDest        ;   }
    public void setUnidirectionnel(boolean unidirectionnel   ) {    this.estUnidirectionnel = unidirectionnel ;   } 
    
    /*-------------------------------------- */
	/*               toString                */
	/*-------------------------------------- */
    public String toString() 
    {
        String sens    = (this.estUnidirectionnel) ? "unidirectionnelle" : "bidirectionnelle";
        
        String origine = (this.getClasseFille() != null) ? this.getClasseFille().getNom()  : "?"; 
        String dest    = (this.getClasseMere () != null) ? this.getClasseMere ().getNom()  : "?";
        
        String multO   = (this.multiOrig  != null) ? this.multiOrig.toString() : "?";
        String multD   = (this.multiDest  != null) ? this.multiDest.toString() : "?";

        return String.format("Association %d : %s de %s(%s) vers %s(%s)", 
                              this.getNum(), sens, origine, multD, dest, multO);
    } 
}