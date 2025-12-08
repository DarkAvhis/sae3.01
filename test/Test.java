package test;
import java.util.ArrayList;

import modele.AttributObjet;
import modele.ClasseObjet;
import modele.MethodeObjet;
import modele.Visibilite;

public class Test 
{
    public static void main(String[] args) 
    {
        AttributObjet att1 = new AttributObjet( "y", "instance", "String", Visibilite.PRIVATE) ; 
        AttributObjet att2 = new AttributObjet( "x", "instance", "String", Visibilite.PUBLIC) ; 

        MethodeObjet met1 =  new MethodeObjet("getY", null , "String", Visibilite.PUBLIC) ; 
        MethodeObjet met2 =  new MethodeObjet("getX", null , "String", Visibilite.PUBLIC) ; 

        ArrayList<AttributObjet> attribts = new ArrayList<>() ; 
        ArrayList<MethodeObjet> methodes = new ArrayList<>() ; 

        attribts.add(att1); 
        attribts.add(att2); 

        methodes.add(met1) ; 
        methodes.add(met2) ; 

        ClasseObjet class1 = new ClasseObjet(attribts, methodes, "Point");

        System.out.println( class1.toString());

    }    
}