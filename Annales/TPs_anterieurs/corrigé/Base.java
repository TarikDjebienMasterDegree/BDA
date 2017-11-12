import java.sql.*;

/**
 *  Une base est reliée à une base de donnée relationnelle
 * Version simplifiee de la classe Base, avec auto-commit et
 * pas de possibilite de faire de commit explicitement
 * Les ResultSet sont non mofiables et non navigables
 *
 */
public class Base {

  /**
   *  Fenetre permettant de saisir un login/mot de passe
   *
   */
  private class FenetreConnexion {

    /**
     *  Constructeur de FenetreConnexion 
     */
    FenetreConnexion() {
      final javax.swing.JDialog dialogue =
          new javax.swing.JDialog((java.awt.Frame) null, "Connexion Oracle", true);
      javax.swing.JTextField login = new javax.swing.JTextField(30);
      javax.swing.JPasswordField pw = new javax.swing.JPasswordField(30);
      javax.swing.JButton ok = new javax.swing.JButton("OK");
      dialogue.getContentPane().setLayout(new java.awt.GridLayout(3, 2));
      dialogue.getContentPane().add(new javax.swing.JLabel("Nom :"));
      dialogue.getContentPane().add(login);
      dialogue.getContentPane().add(new javax.swing.JLabel("Mot de Passe :"));
      dialogue.getContentPane().add(pw);
      dialogue.getContentPane().add(ok);
      ok.addActionListener(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            dialogue.dispose();
          }
        });
      dialogue.pack();
      dialogue.setVisible(true);
      ouvrirBase(
          login.getText(),
          new String(pw.getPassword())
          );
    }
  }

  // connexion a la base
  private Connection connect;


  /**
   *  Constructeur qui ouvre une fenetre de connexion
   */
  public Base() {
    // une fenetre s'ouvre pour demander le nom et mot de passe
    // puis appelle la methode ouvrirBase
    FenetreConnexion fc = new FenetreConnexion();
  }


  /**
   *  ouvre la base avec le login (nom,passwd)
   *
   *@param  nom     nom d'utilisateur Oracle
   *@param  passwd  mot de passe associé
   */
  public Base(String nom, String passwd) {
    this.ouvrirBase(nom, passwd);
  }


  /**
   *  Ouverture de la connexion
   *
   *@param  nom     nom d'utilisateur Oracle
   *@param  passwd  mot de passe associé
   */
  public void ouvrirBase(String nom, String passwd) {
    try {
      DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      connect = DriverManager.getConnection("jdbc:oracle:thin:@orval.fil.univ-lille1.fr:1521:filora10gr2", nom, passwd);
    } catch (SQLException e) {
      System.err.println("erreur connexion ou sql " + e.getMessage());
      System.exit(0);
    }
  }


  /**
   *  fermeture de la connexion
   */
  public void fermerBase() {
    try {
      connect.close();
      // la fermeture de la Connection entraine la fermeture
      // des Statement associes.
    } catch (SQLException e) {
      System.err.println("erreur fermeture base de donnee");
    }
  }

  /**
   *  création d'un Statement pour des ResultSet non modifiables et non
   *  navigables
   *
   *@return    le Statement
   */
  public Statement creerStatement() {
    Statement stmt = null;
    try {
      stmt = connect.createStatement();
    } catch (SQLException e) {
      System.err.println("erreur creation statement " + e.getMessage());
      e.printStackTrace();
    }
    return stmt;
  }


  /**
   *  création d'un CallableStatement pour un appel de module stocké donné
   *
   *@param  s  L'appel de procédure ou fonction
   *@return    le CallableStatement
   */
  public CallableStatement creerAppel(String s) {
    CallableStatement stmt = null;
    try {
      stmt = connect.prepareCall(s);
    } catch (SQLException e) {
      System.err.println("erreur creation CallableStatement " + e.getMessage());
      e.printStackTrace();
    }
    return stmt;
  }
  
  /**
   *  création d'un PreparedStatement 
   *
   *@param  s  La requete qu'il faut preparer
   *@return    le PreparedStatement
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

}


