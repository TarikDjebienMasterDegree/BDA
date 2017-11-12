public class Personne{
   private int id;
   private String nom, prenom, adresse;
   
   public Personne(){}
   
   public int getId(){return id;}
   public String getNom(){return nom;}
   public String getPrenom(){return prenom;}
   public String getAdresse(){return adresse;}
   
   public void setId(int id){this.id = id;}
   public void setNom(String nom){this.nom = nom;}
   public void setPrenom(String prenom){this.prenom = prenom;}
   public void setAdresse(String adresse){this.adresse = adresse;}

   public String toString(){
      return new StringBuffer("Nom : ").append(nom).append(", ")
      			.append("Prenom : ").append(prenom).append(", ")
      			.append("Adresse : ").append(adresse)
      			.toString();	
   }
}