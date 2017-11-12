import java.util.*;
import java.sql.*;

/**
 *  Classe qui permet de simuler les caisses d'un supermarche -- TP JDBC
 *
 *@author     Anne-Cecile Caron
 */
public class Magasin {

  private final static Base b = new Base();
  private final static Random hasard = new Random();

  private List<Caisse> lesCaisses = new LinkedList<Caisse>(); // toutes les caisses

  // Les Statements
  private CallableStatement affect_cs = Magasin.b.creerAppel("{call PAQ_CAISSE.affectation_client(?)}");
  
  // on choisit de creer 3 CallableStatement pour 1 objet Magasin
  // on peut aussi preferer en definir 3 pour chaque caisse. Dans ce cas, on les definit dans la classe Caisse
  // et on les initialise directement dans le constructeur de Caisse avec la valeur de this.num
  private CallableStatement cs_ouvrir = Magasin.b.creerAppel("{call PAQ_CAISSE.ouvrir_caisse(?)}"),
      cs_passer = Magasin.b.creerAppel("{call PAQ_CAISSE.passer_client(?)}"),
      cs_fermer = Magasin.b.creerAppel("{call PAQ_CAISSE.fermer_caisse(?)}"),
      cs_nbclients = Magasin.b.creerAppel("{? = call PAQ_CAISSE.nb_clients(?)}");;

  private PreparedStatement ps_duree = Magasin.b.preparerRequete("select duree_prevue from client where idCaisse=? and rang=1"),
      ps_numClient = Magasin.b.preparerRequete("select nvl(max(numero),0) from client");
      
  private Statement s = Magasin.b.creerStatement();
  
  private int TAILLE_MAX ; // initialise dans le constructeur a partir du paquetage PAQ_CAISSE 
                           //(fonction stockee taille_maxi)


  /**
   * Un objet de la classe interne Caisse represente une caisse du supermarche
   *@author     Anne-Cecile Caron
   */
  private class Caisse {
    private int num; // le numero de la caisse
    private int duree = -1; // la duree restante de traitement du premier client
    // cette duree evolue constamment au court du temps.
    // quand elle vaut 0, c'est que le client de rang 1 vient de terminer, on peut passer au suivant
    // quand elle vaut -1; c'est qu'il n'y a pas de client en caisse
    
    private boolean ouverte ;

    /**
     *  Constructeur de Caisse
     *
     *@param  num  Le numero de la caisse
     *@param  ouv  true ssi la caisse est ouverte 
     */
    public Caisse(int num, boolean ouv) {
      this.num = num;
      this.ouverte = ouv ;
      try {
        if (ouv) {
          s.executeUpdate("insert into caisse(identifiant,ouverte) values(" + num + ",1)");
        } else {
          s.executeUpdate("insert into caisse(identifiant,ouverte) values(" + num + ",0)");
        }
      } catch (SQLException e) {
        System.out.println("constructeur de Caisse=>"+e.getMessage());
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
        cs_ouvrir.setInt(1, this.num);
        cs_ouvrir.execute();
        this.ouverte = true ;
        System.out.println("ouverture de la caisse "+this.num);
      } catch (SQLException e) {
        System.out.println("methode ouvrir de Caisse=>"+e.getMessage());
      }
    }
    
    /**
     *  fermeture de la caisse
     */
    public void fermer() {
      try {
        cs_fermer.setInt(1, this.num);
        cs_fermer.execute();
        this.ouverte = false ;
        System.out.println("fermeture de la caisse "+this.num);
      } catch (SQLException e) {
        if (e.getErrorCode() == -20105) {// PAS_DE_CAISSE_DISPO
          System.out.println("il n'y a pas de caisse disponible pour les clients");
        }
        System.out.println("mthode fermer de Caisse=>"+e.getMessage());
      }
    }


    /**
     *  le client de rang 1 vient de terminer, il quitte la caisse et le client suivant lui succede
     */
    public void passerClient() {
      try {
        // on fait passer le client de rang 1
        cs_passer.setInt(1, this.num);
        cs_passer.execute();
        // la duree de traitement du client courant 
        // devient le temps de traitement du nouveau client de rang 1
        // ou -1 si pas de client en caisse
        this.duree = temps_traitement();

        System.out.println("Un client quitte la caisse "+this.num);
      } catch (SQLException e) {
        if (e.getErrorCode() == -20107) {// PAS_DE_CLIENT 
          System.out.println("il n'y a pas de client en caisse "+this.num);
        }
        System.out.println("methode passerClient de Caisse=>"+e.getMessage());
      }
    }


    private int temps_traitement() {
      // on recherche le temps de traitement du client de rang 1
      // -1 si pas de client
      if (! this.ouverte) return -1 ;
      try {
        ps_duree.setInt(1, this.num);
        ResultSet rs = ps_duree.executeQuery();
        if (rs.next()) {
          // il y a un client en caisse
          int t = rs.getInt("duree_prevue");
          rs.close();
          return t;
        } else {
          // pas de client en caisse
          return -1;
        }
      } catch (SQLException e) {
        System.out.println("methode temps_traitement de Caisse=>"+e.getMessage());
        return -1;
      }
    }
    
   /** 
    * Le nombre de clients a la caisse
    */
    public int nbClients(){
      try{
        cs_nbclients.setInt(2,this.num);
        cs_nbclients.registerOutParameter(1,java.sql.Types.INTEGER);
        cs_nbclients.execute();
        return cs_nbclients.getInt(1);
      } catch (SQLException e) {
        System.out.println("methode nbClients de Caisse=>"+e.getMessage());
        return 0;
      }
    }

    /**
     *  un top de simulation
     * A chaque top, les clients ont un peu moins a attendre.
     */
    public void top() {
      if (this.duree > 0) {
        this.duree--;
      } else if (this.duree == 0) {
        this.passerClient();
      } else { // duree vaut -1, on regarde si un client est arrive entre temps
        this.duree = temps_traitement();
      }
    }


    /**
     *  representation de l'etat de la caisse
     *
     *@return    une chaine qui contient les valeurs (numero de caisse, duree restante de traitement du 1er client, nb de clients)
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
      CallableStatement cs_taille_max = Magasin.b.creerAppel("{? = call PAQ_CAISSE.taille_max}");
      cs_taille_max.registerOutParameter(1,java.sql.Types.INTEGER);
      cs_taille_max.execute();
      TAILLE_MAX = cs_taille_max.getInt(1);
      cs_taille_max.close() ;
    }catch(SQLException e){
      System.out.println("pb pour la lecture de TAILLE_MAX");
      TAILLE_MAX = 3; // on triche un peu ...
    }
    // on cree les caisses
    System.out.println("creation des caisses");
    for (int i = 1; i <= nbCaissesOuvertes; i++) {
      this.lesCaisses.add(new Caisse(i, true));
    }
    for (int i = nbCaissesOuvertes + 1; i <= nbCaisses; i++) {
      this.lesCaisses.add(new Caisse(i, false));
    }
  }


  /**
   *  arrivee d'un client dans le magasin
   *  Cette methode ajoute un client dans la base de donnees
   */
  private void arriveeClient() {
    try {
      // on ajoute un client dans la base
      ResultSet rs = ps_numClient.executeQuery();
      rs.next();
      int num = rs.getInt(1) + 1; // un nouveau numero de client
      // pour des raisons d'efficacite, il peut etre judicieux 
      // d'utiliser un compteur comme variable d'instance du magasin, 
      // plutot que de faire une requete a chaque fois
      int dureePrevue = hasard.nextInt(10) + 1;// entier entre 1 et 10
      rs.close();
      s.executeUpdate("insert into Client(numero,duree_prevue) values (" + num + "," + dureePrevue + ")");
      System.out.println("arrivee du client " + num + " dans le magasin");
    } catch (SQLException e) {
      System.out.println("methode arriveeClient de Magasin=>"+e.getMessage());
    }
  }


  /**
   *  Un client part du magasin sans passer aux caisses
   */
  private void departClient() {
    try {
      // on enleve un client de la base, c'est un client qui n'est pas en caisse
      ResultSet rs = s.executeQuery("select nvl(max(numero),0) from client where idCaisse is null");
      rs.next();
      int num = rs.getInt(1);
      // num vaut maintenant le plus grand numero de clients qui n'est pas en caisse (ou 0)
      rs.close();
      if (num > 0) {
        // on peut enlever un client
        s.executeUpdate("delete from client where numero = " + num);
        System.out.println("depart sans achat du client " + num);
      }
    } catch (SQLException e) {
      System.out.println("methode departClient de Magasin=>"+e.getMessage());
    }
  }


  /**
   *  affectation de quelques clients aux caisses, clients choisis aleatoirement
   */
  private void affectations() {
    try {
      ResultSet rs = s.executeQuery("select * from client where idCaisse is null");
      while (rs.next()) {
        int i = hasard.nextInt(2); // i vaut 0 ou 1
        if (i == 1) {
          // le client va aux caisses
          affect_cs.setInt(1, rs.getInt("numero"));
          affect_cs.execute();
          System.out.println("le client "+rs.getInt("numero")+" va en caisse");
        }// sinon, le client reste dans le magasin
      }
      rs.close();
    } catch (SQLException e) {
      // ajouter le traitement de l'exception PAS_DE_CAISSE_DISPO
      System.out.println("methode affectations de Magasin=>"+e.getMessage());
    }
  }


  /**
   *  Representation de l'etat du magasin
   *
   *@return    Une chaine de caracteres qui contient toutes les representations des caisses
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
    // on recherche le plus petit nombre de clients a une caisse ouverte
    String req = "select min(count(numero)) from caisses_ouvertes left join client on idCaisse = identifiant group by identifiant" ;
    try{
      ResultSet rs = s.executeQuery(req);
      rs.next();
      int nb_min_clients = rs.getInt(1) ;
      if (nb_min_clients == this.TAILLE_MAX) { this.ouvrirCaisse(); }
    }catch(SQLException e){
      System.out.println("methode testerSaturation de Magasin=>"+e.getMessage());
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
    // on affecte certains clients presents dans le magasin a une caisse
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
    // on peut imaginer de fermer aleatoirement une caisse de temps en temps
  }
  
  /**
   *  supprime les donnees de la base (les clients et les caisses)
   */
  public void menage() {
    try{
      // on supprime les donnees de la base
      s.executeUpdate("delete from client");
      s.executeUpdate("delete from caisse");
    }catch(SQLException e){
      System.out.println("pb a la suppression des donnees");
    }
  }


  /**
   *  programme de simulation
   *
   *@param  sur la ligne de commande : le nombre de pas de simulation (20 par defaut)
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

