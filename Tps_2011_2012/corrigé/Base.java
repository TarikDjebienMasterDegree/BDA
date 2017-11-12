import java.sql.*;
import java.io.Console;

/**
 *  Une base est reliee a une base de donnee relationnelle
 *
 */
public class Base {

  // connexion a la base
  private Connection connect;

  /**
   * constructeur de Base. Ouvre une connexion a la base Oracle. Utilisation de la console pour lire le login et password
   */
  public Base() {
    // une fenetre s'ouvre pour demander le nom et mot de passe
    // puis appelle la methode ouvrirBase
    Console console = System.console();
    String name = console.readLine("[Identifiant BDD]: ");
    char[] pdt =console.readPassword("[Mot de passe]: ");
    String passdata = new String(pdt);
    this.ouvrirBase(name, passdata);
  }

  /**
   * constructeur de Base. Ouvre une connexion a la base Oracle avec le login et password passes en parametre
   * @param nom le nom de login
   * @param passwd le mot de passe
   */
  public Base(String nom, String passwd) {
    this.ouvrirBase(nom, passwd);
  }

  /**
   * Ouvre une connexion a la base Oracle avec le login et password passes en parametre
   * @param nom le nom de login
   * @param passwd le mot de passe
   */
  public void ouvrirBase(String nom, String passwd) {
    try {
      DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      connect = DriverManager.getConnection("jdbc:oracle:thin:@orval.fil.univ-lille1.fr:1521:filora10gr2", nom, passwd);
    }
    catch (SQLException e2) {
      System.err.println("erreur connexion ou sql" + e2.getErrorCode() + " " + e2.getMessage());
      System.exit(0);
    }
  }

  /**
   * ferme la connexion courante, ouverte par le constructeur
   */
  public void fermerBase() {
    try {
      connect.close();
      // la fermeture de la Connection entraine la fermeture
      // des Statement associes.
    }
    catch (SQLException e) {
      System.err.println("erreur fermeture base de donnee");
    }
  }

  /**
   * creation d'un objet Statement avec le comportement par defaut.
   * @return un Statement permettant de poser des requetes via la connexion courante
   */
  public Statement creerStatement() {
    Statement stmt = null;
    try {
      stmt = connect.createStatement();
    }
    catch (SQLException e) {
      System.err.println("erreur creation statement");
      e.printStackTrace();
    }
    return stmt;
  }

  /**
   * creation d'un CallableStatement
   * @param la chaine de caracteres qui represente l'appel de procedure/fonction stockee en JDBC
   * @return le CallableStatement correspondant au parametre s et a la connexion courante
   */
  public CallableStatement creerAppel(String s) {
    CallableStatement stmt = null;
    try {
      stmt = connect.prepareCall(s);
    }
    catch (SQLException e) {
      System.err.println("erreur creation CallableStatement");
      e.printStackTrace();
    }
    return stmt;
  }
  
  /**
   *  creation d'un PreparedStatement 
   *
   *@param  s  La requete qu'il faut preparer
   *@return    le PreparedStatement correspondant au parametre s et a la connexion courante
   */
  public PreparedStatement preparerRequete(String s) {
    PreparedStatement stmt = null;
    try {
      stmt = connect.prepareStatement(s);
    } catch (SQLException e) {
      System.err.println("erreur creation PreparedStatement " + e.getMessage());
      e.printStackTrace();
    }
    return stmt;
  }
  
  public static void main(String args[]){
      Base b = new Base() ;
      System.out.println("ok ouverture connexion");
      Statement st = b.creerStatement() ;
      try{
        ResultSet rs = st.executeQuery("select * from tab");
        while (rs.next()){
          System.out.println(rs.getString(1)+" "+rs.getString(2));
        }
        st.close();
      }catch(SQLException sqle){
        System.out.println(sqle.getErrorCode() + " " + sqle.getMessage());
      }
      b.fermerBase() ;
      System.out.println("ok fermeture connexion");
  }
}
