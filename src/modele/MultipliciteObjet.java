package modele;

public class MultipliciteObjet 
{
    //999999999 = * ("infini")
    private int debut;
    private int fin;

    public MultipliciteObjet(int debut, int fin) 
    {
        this.debut = debut;
        this.fin = fin;
    }

    public int getDebut() {    return debut ;   }
    public int getFin  () {    return fin   ;   }

    public void setDebut( int debut ) {    this.debut = debut ;   }
    public void setFin  ( int fin   ) {    this.fin   = fin   ;   }

    @Override
    public String toString()
    {
        if (this.debut == 999999999) return "*";
        if (this.fin   == 999999999) return this.debut + "..*";
        return this.debut + ".." + this.fin;
    }
}
