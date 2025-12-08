package modele;
public  enum Visibilite 
{
    PRIVATE   ( '-'  ),
    PUBLIC    ( '+'  ),
    //PACKAGE   ( '~'  ),
    PROTECTED ( '#'  );

    private char libelle ; 
    Visibilite( char libelle ) { this.libelle = libelle ;  }
    public char getLibelle() { return this.libelle ;  }
}
