package vue;

public class LiaisonVue 
{
    public enum TypeLiaison {
        ASSOCIATION_UNIDI, // Ligne pleine + flèche simple
        ASSOCIATION_BIDI,  // Ligne pleine simple
        HERITAGE,          // Ligne pleine + triangle vide
        IMPLEMENTATION     // Ligne pointillée + triangle vide
    }

    private String nomClasseOrig; // Pour trouver le BlocClasse de départ
    private String nomClasseDest; // Pour trouver le BlocClasse d'arrivée
    private TypeLiaison type;
    
    // NOUVEAUX CHAMPS pour les multiplicités
    private String multipliciteOrig; 
    private String multipliciteDest;

    public LiaisonVue(String nomClasseOrig, String nomClasseDest, TypeLiaison type, String multOrig, String multDest) 
    {
        this.nomClasseOrig = nomClasseOrig;
        this.nomClasseDest = nomClasseDest;
        this.type = type;
        this.multipliciteOrig = (multOrig != null) ? multOrig : "";
        this.multipliciteDest = (multDest != null) ? multDest : "";
    }

    // Getters
    public String getNomClasseOrig() { return nomClasseOrig; }
    public String getNomClasseDest() { return nomClasseDest; }
    public TypeLiaison getType()     { return type;          }
    
    // NOUVEAUX GETTERS
    public String getMultipliciteOrig() { return multipliciteOrig; }
    public String getMultipliciteDest() { return multipliciteDest; }
}