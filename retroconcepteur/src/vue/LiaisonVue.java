package vue;

/**
 * Représentation graphique d'une liaison entre deux classes.
 * * Encapsule les informations nécessaires pour dessiner les flèches et traits
 * représentant les relations (héritage, implémentation, association) entre
 * classes.
 * * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT,
 * Ariunbayar
 * BUYANBADRAKH, Yassine EL MAADI
 * 
 * @date 12 décembre 2025
 */
public class LiaisonVue {
    /**
     * Énumération des types de liaisons possibles.
     */
    public enum TypeLiaison {
        /** Association unidirectionnelle : ligne pleine avec flèche simple */
        ASSOCIATION_UNIDI,
        /** Association bidirectionnelle : ligne pleine simple */
        ASSOCIATION_BIDI,
        /** Héritage : ligne pleine avec triangle vide */
        HERITAGE,
        /** Implémentation d'interface : ligne pointillée avec triangle vide */
        IMPLEMENTATION,

        NESTED
    }

    private String nomClasseOrig; // Pour trouver le BlocClasse de départ
    private String nomClasseDest; // Pour trouver le BlocClasse d'arrivée
    private TypeLiaison type;

    // NOUVEAUX CHAMPS pour les multiplicités
    private String multipliciteOrig;
    private String multipliciteDest;

    // Rôles (libellés) sur chaque extrémité pour les associations
    private String roleOrig;
    private String roleDest;

    // Offsets pour l'affichage des rôles (suivent les déplacements des blocs)
    private int roleOrigOffsetAlong = 20;
    private int roleOrigOffsetPerp = 15;
    private int roleDestOffsetAlong = 20;
    private int roleDestOffsetPerp = 15;

    // Propriétés prédéfinies et contrainte éventuelle
    private String proprietes = ""; // ex: "{frozen, addOnly}"
    private String contrainte = ""; // texte libre (étape 7)

    /**
     * Constructeur d'une liaison vue.
     * * @param nomClasseOrig Nom de la classe origine
     * 
     * @param nomClasseDest Nom de la classe destination
     * @param type          Type de liaison
     * @param multOrig      Multiplicité côté origine (peut être null)
     * @param multDest      Multiplicité côté destination (peut être null)
     */
    public LiaisonVue(String nomClasseOrig, String nomClasseDest, TypeLiaison type, String multOrig, String multDest) {
        this.nomClasseOrig = nomClasseOrig;
        this.nomClasseDest = nomClasseDest;
        this.type = type;
        this.multipliciteOrig = (multOrig != null) ? multOrig : "";
        this.multipliciteDest = (multDest != null) ? multDest : "";
        this.roleOrig = "";
        this.roleDest = "";
    }

    /**
     * Constructeur enrichi pour les associations avec rôles.
     */
    public LiaisonVue(String nomClasseOrig, String nomClasseDest, TypeLiaison type, String multOrig, String multDest,
            String roleOrig, String roleDest,
            Integer roleOrigOffsetAlong, Integer roleOrigOffsetPerp,
            Integer roleDestOffsetAlong, Integer roleDestOffsetPerp,
            String proprietes, String contrainte) {
        this(nomClasseOrig, nomClasseDest, type, multOrig, multDest);
        this.roleOrig = (roleOrig != null) ? roleOrig : "";
        this.roleDest = (roleDest != null) ? roleDest : "";
        if (roleOrigOffsetAlong != null)
            this.roleOrigOffsetAlong = roleOrigOffsetAlong;
        if (roleOrigOffsetPerp != null)
            this.roleOrigOffsetPerp = roleOrigOffsetPerp;
        if (roleDestOffsetAlong != null)
            this.roleDestOffsetAlong = roleDestOffsetAlong;
        if (roleDestOffsetPerp != null)
            this.roleDestOffsetPerp = roleDestOffsetPerp;
        this.proprietes = (proprietes != null) ? proprietes : "";
        this.contrainte = (contrainte != null) ? contrainte : "";
    }

    // Getters
    public String getNomClasseOrig() {
        return nomClasseOrig;
    }

    public String getNomClasseDest() {
        return nomClasseDest;
    }

    public TypeLiaison getType() {
        return type;
    }

    // NOUVEAUX GETTERS
    public String getMultipliciteOrig() {
        return multipliciteOrig;
    }

    public String getMultipliciteDest() {
        return multipliciteDest;
    }

    // SETTERS pour multiplicités (édition)
    public void setMultipliciteOrig(String mult) {
        this.multipliciteOrig = (mult != null) ? mult : "";
    }

    public void setMultipliciteDest(String mult) {
        this.multipliciteDest = (mult != null) ? mult : "";
    }

    // GETTERS pour les rôles et offsets
    public String getRoleOrig() {
        return roleOrig;
    }

    public String getRoleDest() {
        return roleDest;
    }

    public int getRoleOrigOffsetAlong() {
        return roleOrigOffsetAlong;
    }

    public int getRoleOrigOffsetPerp() {
        return roleOrigOffsetPerp;
    }

    public int getRoleDestOffsetAlong() {
        return roleDestOffsetAlong;
    }

    public int getRoleDestOffsetPerp() {
        return roleDestOffsetPerp;
    }

    public String getProprietes() {
        return proprietes;
    }

    public String getContrainte() {
        return contrainte;
    }

    // SETTERS pour mettre à jour dynamiquement (édition future)
    public void setRoleOrig(String role) {
        this.roleOrig = (role != null) ? role : "";
    }

    public void setRoleDest(String role) {
        this.roleDest = (role != null) ? role : "";
    }

    public void setRoleOrigOffsetAlong(int offset) {
        this.roleOrigOffsetAlong = offset;
    }

    public void setRoleOrigOffsetPerp(int offset) {
        this.roleOrigOffsetPerp = offset;
    }

    public void setRoleDestOffsetAlong(int offset) {
        this.roleDestOffsetAlong = offset;
    }

    public void setRoleDestOffsetPerp(int offset) {
        this.roleDestOffsetPerp = offset;
    }

    public void setProprietes(String proprietes) {
        this.proprietes = (proprietes != null) ? proprietes : "";
    }

    public void setContrainte(String contrainte) {
        this.contrainte = (contrainte != null) ? contrainte : "";
    }
}