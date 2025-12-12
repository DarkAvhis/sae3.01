package src.vue;

/**
 * Représentation graphique d'une liaison entre deux classes.
 * 
 * Encapsule les informations nécessaires pour dessiner les flèches et traits
 * représentant les relations (héritage, implémentation, association) entre
 * classes.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class LiaisonVue
{
    /**
     * Énumération des types de liaisons possibles.
     */
    public enum TypeLiaison
{
        /** Association unidirectionnelle : ligne pleine avec flèche simple */
        ASSOCIATION_UNIDI,
        /** Association bidirectionnelle : ligne pleine simple */
        ASSOCIATION_BIDI,
        /** Héritage : ligne pleine avec triangle vide */
        HERITAGE,
        /** Implémentation d'interface : ligne pointillée avec triangle vide */
        IMPLEMENTATION
    }

    private String nomClasseOrig; // Pour trouver le BlocClasse de départ
    private String nomClasseDest; // Pour trouver le BlocClasse d'arrivée
    private TypeLiaison type;

    // NOUVEAUX CHAMPS pour les multiplicités
    private String multipliciteOrig;
    private String multipliciteDest;

    /**
     * Constructeur d'une liaison vue.
     * 
     * @param nomClasseOrig Nom de la classe origine
     * @param nomClasseDest Nom de la classe destination
     * @param type          Type de liaison
     * @param multOrig      Multiplicité côté origine (peut être null)
     * @param multDest      Multiplicité côté destination (peut être null)
     */
    public LiaisonVue(String nomClasseOrig, String nomClasseDest, TypeLiaison type, String multOrig, String multDest) 
{
        this.nomClasseOrig = nomClasseOrig;
        this.nomClasseDest = nomClasseDest;
        this.type = type;
        this.multipliciteOrig = (multOrig != null) ? multOrig : "";
        this.multipliciteDest = (multDest != null) ? multDest : "";
    }

    // Getters
    public String getNomClasseOrig() 
{
        return nomClasseOrig;
    }

    public String getNomClasseDest() 
{
        return nomClasseDest;
    }

    public TypeLiaison getType() 
{
        return type;
    }

    // NOUVEAUX GETTERS
    public String getMultipliciteOrig() 
{
        return multipliciteOrig;
    }

    public String getMultipliciteDest() 
{
        return multipliciteDest;
    }
}
