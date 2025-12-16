package vue;

import java.util.List;

import modele.entites.AssociationObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;

/**
 * Vue console minimale : centralise l'affichage .
 */
public class ConsoleVue
{
    public ConsoleVue() {}

    public void afficherUsage()
    {
        System.out.println("Usage: java AnalyseIHMControleur <chemin_du_repertoire>");
    }

    public void afficherMessage(String msg)
    {
        if (msg != null) System.out.println(msg);
    }

    public void afficherClasses(List<ClasseObjet> classes)
    {
        System.out.println("\n=== DIAGRAMMES DE CLASSES (ETAPE 2 & 3) ===");

        if (classes == null || classes.isEmpty()) 
        {
            System.out.println("(Aucune classe trouvée)");
            return;
        }

        for (ClasseObjet c : classes) 
        {
            System.out.println(c);
        }
    }

    public void afficherAssociations(List<AssociationObjet> associations)
    {
        System.out.println("\n=== LIAISONS D'ASSOCIATION (ETAPE 3) ===");

        if (associations == null || associations.isEmpty()) 
        {
            System.out.println("(Aucune association)");
            return;
        }

        for (AssociationObjet a : associations) 
        {
            System.out.println(a);
        }
    }

    public void afficherHeritages(List<HeritageObjet> heritages)
    {
        System.out.println("\n=== HERITAGE (ETAPE 4) ===");

        if (heritages == null || heritages.isEmpty()) 
        {
            System.out.println("(Aucun héritage)");
            return;
        }

        for (HeritageObjet h : heritages) 
        {
            System.out.println(h);
        }
    }

    public void afficherImplementations(List<InterfaceObjet> implementations)
    {
        System.out.println("\n=== IMPLEMENTATION (ETAPE 4) ===");

        if (implementations == null || implementations.isEmpty()) 
        {
            System.out.println("(Aucune implémentation)");
            return;
        }

        for (InterfaceObjet i : implementations) 
        {
            System.out.println(i);
        }
    }
}