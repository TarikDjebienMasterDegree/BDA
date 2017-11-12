import java.sql.*;

/**
 *  Une base est reli�e � une base de donn�e relationnelle
 *
 *@author     Anne-Cecile Caron
 */
public class Base {

  /**
   *  Fenetre permettant de saisir un login/mot de passe
   *
   *@author     Anne-Cecile Caron
   */
  private class FenetreConnexion {

    /**
     *  Constructor for the FenetreConnexion object
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
   *@param  passwd  mot de passe associ�
   */
  public Base(String nom, String passwd) {
    this.ouvrirBase(nom, passwd);
  }


  /**
   *  Ouverture de la connexion
   *
   *@param  nom     nom d'utilisateur Oracle
   *@param  passwd  mot de passe associ�
   */
  public void ouvrirBase(String nom, String passwd) {
    // A COMPLETER
  }


  /**
   *  fermeture de la connexion
   */
  public void fermerBase() {
    // A COMPLETER
  }

  /**
   *  cr�ation d'un Statement pour des ResultSet non modifiables et non
   *  navigables
   *
   *@return    le Statement
   */
  public Statement creerStatement() {
    // A COMPLETER
    return null;
  }


  /**
   *  cr�ation d'un CallableStatement pour un appel de module stock� donn�
   *
   *@param  s  L'appel de proc�dure ou fonction
   *@return    le CallableStatement
   */
  public CallableStatement creerAppel(String s) {
    // A COMPLETER
    return null ;
  }
  
  /**
   *  cr�ation d'un PreparedStatement 
   *
   *@param  s  La requete qu'il faut preparer
   *@return    le PreparedStatement
   */
  public PreparedStatement preparerRequete(String s) {
    // A COMPLETER
    return null;
  }
}


