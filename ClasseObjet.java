
import java.util.ArrayList;

public class ClasseObjet {

    private String nom;
    private ArrayList<AttributObjet> attributs;
    private ArrayList<MethodeObjet> methodes;

    public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom) {
        this.attributs = attributs;
        this.methodes = methodes;
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ArrayList<AttributObjet> getattributs() {
        return attributs;
    }

    public void setattributs(ArrayList<AttributObjet> attributs) {
        this.attributs = attributs;
    }

    public ArrayList<MethodeObjet> getmethodes() {
        return methodes;
    }

    public void setmethodes(ArrayList<MethodeObjet> methodes) {
        this.methodes = methodes;
    }
    
    
}
