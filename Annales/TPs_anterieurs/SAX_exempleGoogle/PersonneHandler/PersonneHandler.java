import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*; 
import java.io.*; 
import java.util.*; 

public class PersonneHandler extends DefaultHandler{
   //résultats de notre parsing
   private List<Personne> annuaire;
   private Personne personne;
   //flags nous indiquant la position du parseur
   private boolean inAnnuaire, inPersonne, inNom, inPrenom, inAdresse;
   
   // simple constructeur
   public PersonneHandler(){
      super();	
      
   }
   //détection d'ouverture de balise
   public void startElement(String uri,
                         String localName,
                         String qName,
                         Attributes attributes)
                  throws SAXException{
      System.out.println(uri+" "+localName+" "+qName);
      if(qName.equals("annuaire")){
         annuaire = new LinkedList<Personne>();
         inAnnuaire = true;
      }else if(qName.equals("personne")){
         personne = new Personne();
         try{
         	int id = Integer.parseInt(attributes.getValue("id"));
            personne.setId(id);
         }catch(Exception e){
         	//erreur, le contenu de id n'est pas un entier
            throw new SAXException(e);
         }
         inPersonne = true;	
      }else if(qName.equals("nom")){
         inNom = true;	
      }else if(qName.equals("prenom")){
         inPrenom = true;	
      }else if(qName.equals("adresse")){
         inAdresse = true;	
      }else{
         //erreur, on peut lever une exception
         throw new SAXException("Balise "+qName+" inconnue.");	
      }
   }
   //détection fin de balise
   public void endElement(String uri,
                       String localName,
                       String qName)
                throws SAXException{
      if(qName.equals("annuaire")){
         inAnnuaire = false;
      }else if(qName.equals("personne")){
      	 annuaire.add(personne);
      	 personne = null;
         inPersonne = false;	
      }else if(qName.equals("nom")){
         inNom = false;	
      }else if(qName.equals("prenom")){
         inPrenom = false;	
      }else if(qName.equals("adresse")){
         inAdresse = false;	
      }else{
         //erreur, on peut lever une exception
         throw new SAXException("Balise "+qName+" inconnue.");	
      }          	
   }
   //détection de caractères
   public void characters(char[] ch,
                       int start,
                       int length)
                throws SAXException{
      String lecture = new String(ch,start,length);
      if(inNom){
         personne.setNom(lecture);	
      }else if(inPrenom){
         personne.setPrenom(lecture);	
      }else if(inAdresse){
         personne.setAdresse(lecture);	
      }          	
   }
   //début du parsing
   public void startDocument() throws SAXException {
   	  System.out.println("Début du parsing");
   }
   //fin du parsing
   public void endDocument() throws SAXException {
   	  System.out.println("Fin du parsing");
   	  System.out.println("Resultats du parsing");
   	  for(Personne p : annuaire){
   	     System.out.println(p);
   	  }
   }
   
   // test
   public static void main(String[] args){
      try{
         // création d'une fabrique de parseurs SAX
         SAXParserFactory fabrique = SAXParserFactory.newInstance();
			
         // création d'un parseur SAX
         SAXParser parseur = fabrique.newSAXParser();
			
         // lecture d'un fichier XML avec un DefaultHandler
         File fichier = new File("./ExempleSAX.xml");
         DefaultHandler gestionnaire = new PersonneHandler();
         parseur.parse(fichier, gestionnaire);
		
      }catch(ParserConfigurationException pce){
         System.out.println("Erreur de configuration du parseur");
         System.out.println("Lors de l'appel à SAXParser.newSAXParser()");
      }catch(SAXException se){
         System.out.println("Erreur de parsing");
         System.out.println("Lors de l'appel à parse()");
         se.printStackTrace();
      }catch(IOException ioe){
         System.out.println("Erreur d'entrée/sortie");
         System.out.println("Lors de l'appel à parse()");
      }
   }	
}