import java.util.*;
import java.sql.*;

/**
 *  Classe qui permet de simuler les caisses d'un supermarché -- TP JDBC
 *
 *@author     Anne-Cécile Caron
 */
public class Magasin {

  private final static Base b = new Base();
  private final static Random hasard = new Random();

  private int nbClients = 0;
  private List<Caisse> lesCaisses = new LinkedList<Caisse>(); // toutes les caisses

  // Les Statements

  // A COMPLETER par la declaration des statements utilisés par la suite
  // dans les classes Caisse et Magasin
  
  private int TAILLE_MAX ; // initialisé dans le constructeur à partir du paquetage PAQ_CAISSE (fonction stockee taille_maxi)


  /**
   * Un objet de la classe interne Caisse représente une caisse du supermarché
   *@author     Anne-Cecile Caron
   */
  private class Caisse {
    private int num; // le numéro de la caisse
    private int duree = -1; // la duree restante de traitement du premier client
    // cette duree évolue constamment au court du temps.
    // quand elle vaut 0, c'est que le client de rang 1 vient de terminer, on peut passer au suivant
    // quand elle vaut -1; c'est qu'il n'y a pas de client en caisse
    
    private boolean ouverte ;

    /**
     *  Constructeur de Caisse
     *
     *@param  num  Le numéro de la caisse
     *@param  ouv  true ssi la caisse est ouverte 
     */
    public Caisse(int num, boolean ouv) {
      this.num = num;
      this.ouverte = ouv ;
      try {
        // A COMPLETER : insertion d'une nouvelle caisse dans la base
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
    }

    /**
     *  test d'ouverture de la caisse
     *@return true ssi la caisse est ouverte
     */
    public boolean estOuverte(){
      return this.ouverte ;
    }

    /**
     *  ouverture de la caisse
     */
    public void ouvrir() {
      try {
        // A COMPLETER
        System.out.println("ouverture de la caisse "+this.num);
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
    }
    
    /**
     *  fermeture de la caisse
     */
    public void fermer() {
      try {
        // A COMPLETER
        System.out.println("fermeture de la caisse "+this.num);
      } catch (SQLException e) {
        // A COMPLETER = si exception PAS_DE_CAISSE_DISPO alors afficher un message adapté
        System.out.println(e.getMessage());
      }
    }


    /**
     *  le client de rang 1 vient de terminer, il quitte la caisse et le client suivant lui succède
     */
    public void passerClient() {
      try {
        // on fait passer le client de rang 1
        // A COMPLETER
        // la duree de traitement du client courant 
        // devient le temps de traitement du nouveau client de rang 1
        // ou -1 si pas de client en caisse
        this.duree = temps_traitement();
        // tel que le paquetage est écrit, quand un client passe en caisse, il reste dans la base 
        // on peut imaginer qu'un autre client avec le même identifiant entre alors dans le magasin
        System.out.println("un client quitte la caisse "+this.num + " et ... un  autre arrive dans le magasin");
        // vous pouvez modifier cela si vous voulez, en retirant de la base le client
      } catch (SQLException e) {
        // A COMPLETER = si exception PAS_DE_CLIENT alors afficher un message adapté
        System.out.println(e.getMessage());
      }
    }


    private int temps_traitement() {
      // on recherche le temps de traitement du client de rang 1
      // -1 si pas de client (ou si la caisse est fermée)
      // A COMPLETER
      } catch (SQLException e) {
        System.out.println(e.getMessage());
        return -1;
      }
    }
    
   /** 
    * Le nombre de clients à la caisse
    */
    public int nbClients(){
      if (! this.ouverte) return 0 ;
      try{
        // c'est surement plus efficace de gérer un compteur du nombre de clients en caisse
        // plutôt que de poser la requête suivante ...
        ResultSet rs = s.executeQuery("select count(*) from client where idCaisse="+this.num);
        rs.next() ;
        int n = rs.getInt(1);
        rs.close();
        return n;
      }catch(SQLException e){
        System.out.println(e.getMessage());
        return 0;
      }
    }

    /**
     *  un top de simulation
     * A chaque top, les clients ont un peu moins à attendre.
     */
    public void top() {
      if (this.duree > 0) {
        this.duree--;
      } else if (this.duree == 0) {
        this.passerClient();
      } else { // duree vaut -1, on regarde si un client est arrivé entre temps
        this.duree = temps_traitement();
      }
    }


    /**
     *  représentation de l'état de la caisse
     *
     *@return    une chaine qui contient les valeurs (numéro de caisse, durée restante de traitement du 1er client, nb de clients)
     */
    public String toString() {
      String s = "";
      return "(caisse " + this.num + ", duree " + this.duree + ", #clients " + this.nbClients()+ ")";
    }
  }// fin de la classe interne Caisse


  /**
   * Constructeur de Magasin
   *
   *@param  nbCaisses          nombre total de caisses du magasin
   *@param  nbCaissesOuvertes  nombre de caisses ouvertes
   */

  public Magasin(int nbCaisses, int nbCaissesOuvertes) {
    try{
      // on recherche la valeur de la taille maxi d'une file d'attente
      // C'est possible si on a dans le paquetage PAQ_CAISSE une fonction taille_maxi qui donne ce nombre
      // Ce n'est pas possible si l'on ne dispose que d'une constante dans le paquetage PAQ_CAISSE
      // donc, A COMPLETER par l'initialisation de TAILLE_MAX
    }catch(SQLException e){
      System.out.println("pb pour la lecture de TAILLE_MAX");
      TAILLE_MAX = 3; // on triche un peu ;-)
    }
    // on créé les caisses
    System.out.println("creation des caisses");
    for (int i = 1; i <= nbCaissesOuvertes; i++) {
      this.lesCaisses.add(new Caisse(i, true));
    }
    for (int i = nbCaissesOuvertes + 1; i <= nbCaisses; i++) {
      this.lesCaisses.add(new Caisse(i, false));
    }
  }


  /**
   *  arrivée d'un client dans le magasin
   *  Cette méthode ajoute un client dans la base de données
   */
  private void arriveeClient() {
    try {
      // on ajoute un client dans la base
      ResultSet rs = ps_numClient.executeQuery();
      rs.next();
      int num = rs.getInt(1) + 1; // un nouveau numéro de client
      // pour des raisons d'efficacité, il peut être judicieux 
      // d'utiliser un compteur comme variable d'instance du magasin, 
      // plutôt que de faire une requête à chaque fois
      int dureePrevue = hasard.nextInt(10) + 1;// entier entre 1 et 10
      rs.close();
      s.executeUpdate("insert into Client(numero,duree_prevue) values (" + num + "," + dureePrevue + ")");
      this.nbClients++; // c'est le nombre de clients, pas forcément le plus grand numéro de client
      System.out.println("arrivee du client " + num + " dans le magasin");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }


  /**
   *  Un client part du magasin sans passer aux caisses
   */
  private void departClient() {
    try {
      // on enlève un client de la base, c'est un client qui n'est pas en caisse
      // A COMPLETER
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }


  /**
   *  affectation de quelques clients aux caisses, clients choisis aléatoirement
   */
  private void affectations() {
    try {
      // on choisit aléatoirement des clients
      // qui ne sont pas en caisse
      // et on les affecte à une caisse
      // A COMPLETER
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }


  /**
   *  Représentation de l'état du magasin
   *
   *@return    Une chaine de caractères qui contient toutes les représentations des caisses
   */
  public String toString() {
    Iterator it = lesCaisses.iterator();
    String s = "[ ";
    while (it.hasNext()) {
      Caisse c = (Caisse) it.next();
      if (c.estOuverte()) {
        s += c.toString() + " ";
      }
    }
    return s + "]";
  }
  
  private void testerSaturation(){
    // si toutes les caisses ont au moins TAILLE_MAX clients, ouvrir une nouvelle caisse
    // pour le savoir, on recherche le plus petit nombre de clients à une caisse ouverte
    try{
      // A COMPLETER
    }catch(SQLException e){
      System.out.println(e.getMessage());
    }
  }
  
  private void ouvrirCaisse(){
    Iterator it = lesCaisses.iterator();
    while (it.hasNext()){
      Caisse c = (Caisse) it.next();
      if (!c.estOuverte()) {
        c.ouvrir() ;
        return ;
      }
    }
  }


  /**
   *  un top de simulation
   */
  public void top() {
    // un top de simulation
    // est-ce qu'il faut ajouter/retirer un client?
    int i = hasard.nextInt(5);
    if (i == 0) { // une chance sur 5
      this.departClient();
    } else { // 4 chances sur 5
      this.arriveeClient();
    }
    // on affecte certains clients présents dans le magasin à une caisse
    this.affectations();
    // les clients progressent dans les files d'attente des caisses
    Iterator it = lesCaisses.iterator();
    while (it.hasNext()) {
      Caisse c = (Caisse) it.next();
      c.top();
    }
    // si toutes les caisses ont trop de clients, on ouvre une nouvelle caisse
    this.testerSaturation() ;
    // Remarque : dans cette simulation, je ne ferme jamais de caisse
    // on peut imaginer de fermer aléatoirement une caisse de temps en temps
  }
  
  /**
   *  supprime les données de la base (les clients et les caisses)
   */
  public void menage() {
    try{
      // on supprime les données de la base
      s.executeUpdate("delete from client");
      s.executeUpdate("delete from caisse");
    }catch(SQLException e){
      System.out.println("pb à la suppression des données");
    }
  }


  /**
   *  programme de simulation
   *
   *@param  sur la ligne de commande : le nombre de pas de simulation (20 par défaut)
   */
  public static void main(String args[]) {
    int nb_tops = 20 ;
    try {
      nb_tops = Integer.parseInt(args[0]);
      System.out.println(nb_tops + " pas de simulation");
    }catch(Exception e){
      System.out.println("20 pas de simulation (defaut)");
    }
    Magasin mag = new Magasin(10, 2);
    // magasin avec 10 caisses dont 2 ouvertes.
    for (int chrono = 1; chrono <= nb_tops; chrono++) {
      System.out.println("etape" + chrono + ":");
      mag.top();
      System.out.println(mag);
    }
    mag.menage() ;
    Magasin.b.fermerBase(); // on ferme aussi les Statements
    System.exit(1);
  }
}

