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
    
    // Vous pouvez ajouter d'autres informations ici si besoin (multiplicités, nom d'attribut)

    public LiaisonVue(String nomClasseOrig, String nomClasseDest, TypeLiaison type) 
    {
        this.nomClasseOrig = nomClasseOrig;
        this.nomClasseDest = nomClasseDest;
        this.type = type;
    }

    // Getters
    public String getNomClasseOrig() { return nomClasseOrig; }
    public String getNomClasseDest() { return nomClasseDest; }
    public TypeLiaison getType()     { return type;          }
} 