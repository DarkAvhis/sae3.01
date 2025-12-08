package modele;
public  enum Visibilite 
{
    PRIVATE   ( "-"  ),
    PUBLIC    ( "+"  ),
    //PACKAGE   ( '~'  ),
    PROTECTED ( "#"  );

    private String libelle ; 
    Visibilite( String libelle ) { this.libelle = libelle ;  }
    public String getLibelle() { return this.libelle ;  }
}
