package modele.entites;

/**
 * Représente une association entre deux classes dans un diagramme UML.
 */
public class AssociationObjet extends LiaisonObjet 
{
    // Suppression des attributs masqués (classeDest, classeOrig)

    private boolean           estUnidirectionnel;
    private MultipliciteObjet multiDest         ;
    private MultipliciteObjet multiOrig         ;

    // Propriétés prédéfinies
    private boolean frozen ;
    private boolean addOnly;
    private boolean requete;

    // Rôles (libellés) sur chaque extrémité de l'association
    private String roleOrig; // côté classeFille (origine)
    private String roleDest; // côté classeMere (destination)

    // Offsets d'affichage du rôle par rapport au lien (suivent les déplacements)
    private int roleOrigOffsetAlong = 20; // décalage le long de la ligne
    private int roleOrigOffsetPerp  = 15; // décalage perpendiculaire
    private int roleDestOffsetAlong = 20;
    private int roleDestOffsetPerp  = 15;

    /*-------------------------------------- */
    /* Constructeur                          */
    /*-------------------------------------- */

    // Le constructeur reçoit classeDest et classeOrig (qui deviennent classeMere et
    // classeFille dans LiaisonObjet)
    public AssociationObjet(ClasseObjet classeMere, ClasseObjet classeFille, MultipliciteObjet multiDest,
            MultipliciteObjet multiOrig, String nomAttribut, boolean unidirectionnel) 
    {
        // Appel au constructeur de LiaisonObjet
        super(nomAttribut, classeMere, classeFille);
        this.estUnidirectionnel = unidirectionnel;

        this.multiDest = multiDest;
        this.multiOrig = multiOrig;
        
        this.roleOrig = null ;
        this.roleDest = null ;
        this.frozen   = false;
        this.addOnly  = false;
        this.requete  = false;
    }

    /*-------------------------------------- */
    /* Les Accesseurs                        */
    /*-------------------------------------- */
    public MultipliciteObjet getMultOrig           () { return this.multiOrig          ; }
    public MultipliciteObjet getMultDest           () { return this.multiDest          ; }
    public boolean           getUnidirectionnel    () { return this.estUnidirectionnel ; }
    public String            getRoleOrig           () { return this.roleOrig           ; }
    public String            getRoleDest           () { return this.roleDest           ; }
    public int               getRoleOrigOffsetAlong() { return this.roleOrigOffsetAlong; }
    public int               getRoleOrigOffsetPerp () { return this.roleOrigOffsetPerp ; }
    public int               getRoleDestOffsetAlong() { return this.roleDestOffsetAlong; }
    public int               getRoleDestOffsetPerp () { return this.roleDestOffsetPerp ; }

    public boolean isFrozen () { return this.frozen ; }
    public boolean isAddOnly() { return this.addOnly; }
    public boolean isRequete() { return this.requete; }

    /*-------------------------------------- */
    /* Les Modificateurs                     */
    /*-------------------------------------- */
    public void setMultOrig           (MultipliciteObjet multOrig       ) { this.multiOrig           = multOrig       ; }
    public void setMultDest           (MultipliciteObjet multDest       ) { this.multiDest           = multDest       ; }
    public void setUnidirectionnel    (boolean           unidirectionnel) { this.estUnidirectionnel  = unidirectionnel; }
    public void setRoleOrig           (String            role           ) { this.roleOrig            = role           ; }
    public void setRoleDest           (String            role           ) { this.roleDest            = role           ; }
    public void setRoleOrigOffsetAlong(int               offset         ) { this.roleOrigOffsetAlong = offset         ; }
    public void setRoleOrigOffsetPerp (int               offset         ) { this.roleOrigOffsetPerp  = offset         ; }
    public void setRoleDestOffsetAlong(int               offset         ) { this.roleDestOffsetAlong = offset         ; }
    public void setRoleDestOffsetPerp (int               offset         ) { this.roleDestOffsetPerp  = offset         ; }
    public void setFrozen             (boolean           frozen         ) { this.frozen              = frozen         ; }
    public void setAddOnly            (boolean           addOnly        ) { this.addOnly             = addOnly        ; }
    public void setRequete            (boolean           requete        ) { this.requete             = requete        ; }

    /*-------------------------------------- */
    /* toString                              */
    /*-------------------------------------- */
    public String toString() 
    {
        String sens = (this.estUnidirectionnel) ? "unidirectionnelle" : "bidirectionnelle";

        String origine = (this.getClasseFille() != null) ? this.getClasseFille().getNom() : "?";
        String dest    = (this.getClasseMere () != null) ? this.getClasseMere ().getNom() : "?";

        String multO = (this.multiOrig != null) ? this.multiOrig.toString() : ""; 
        String multD = (this.multiDest != null) ? this.multiDest.toString() : "";

        return String.format("Association %d : %s de %s(%s) vers %s(%s)",
                this.getNum(), sens, origine, multD, dest, multO);
    }
}