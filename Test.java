import java.util.ArrayList;

public class Test 
{
    public static void main(String[] args) 
    {
        AttributObjet att1 = new AttributObjet( "y", "instance", "String", "private") ; 
        AttributObjet att2 = new AttributObjet( "x", "instance", "String", "public") ; 

        MethodeObjet met1 =  new MethodeObjet("getY", null, "String", "public") ; 
        MethodeObjet met2 =  new MethodeObjet("getX", null , "String", "public") ; 

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
