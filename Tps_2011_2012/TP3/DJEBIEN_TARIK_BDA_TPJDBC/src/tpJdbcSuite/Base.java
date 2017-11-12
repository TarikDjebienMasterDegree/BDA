package tpJdbcSuite;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *  Une base est reliée à une base de donnée relationnelle
 *
 *@author     Anne-Cecile Caron
 */
public class Base {


	public static final int forwardOnly = ResultSet.TYPE_FORWARD_ONLY;
	public static final int scrollInsensitive = ResultSet.TYPE_SCROLL_INSENSITIVE;
	public static final int scrollSensitive = ResultSet.TYPE_SCROLL_SENSITIVE;
	public static final int concurReadOnly = ResultSet.CONCUR_READ_ONLY;
	public static final int concurUpdatable = ResultSet.CONCUR_UPDATABLE;

	// connexion a la base
	private Connection connect;
	
	/**
	 * Accesseur pour l'objet connection de la BDD
	 * @return la connection de ma base
	 */
	public Connection getConnection(){
		return this.connect;
	}

	/**
	 *  Fenetre permettant de saisir une URL de connexion et un login/mot de passe
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
			javax.swing.JTextField url = new javax.swing.JTextField("jdbc:oracle:thin:@localhost:1521:XE", 40);
			javax.swing.JTextField login = new javax.swing.JTextField("COMPTE",30);
			javax.swing.JPasswordField pw = new javax.swing.JPasswordField("COMPTE",30);
			javax.swing.JButton ok = new javax.swing.JButton("OK");
			dialogue.getContentPane().setLayout(new java.awt.GridLayout(4, 2));
			dialogue.getContentPane().add(new javax.swing.JLabel("Url de connexion :"));
			dialogue.getContentPane().add(url);
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
					url.getText(),  
					login.getText(),
					new String(pw.getPassword())
					);
		}
	}

	/**
	 *  Constructeur qui ouvre une fenetre de connexion
	 */
	public Base() {
		// une fenetre s'ouvre pour demander le nom et mot de passe
		// puis appelle la methode ouvrirBase
		@SuppressWarnings("unused")
		FenetreConnexion fc = new FenetreConnexion();

	}


	/**
	 *  ouvre la base avec le login (url,nom,passwd)
	 *
	 *@param  url     url de connexion a la base
	 *@param  nom     nom d'utilisateur Oracle
	 *@param  passwd  mot de passe associé
	 */
	public Base(String url, String nom, String passwd) {
		this.ouvrirBase(url, nom, passwd);
	}


	/**
	 *  Ouverture de la connexion
	 *
	 *@param  url     url de connexion
	 *@param  nom     nom d'utilisateur Oracle
	 *@param  passwd  mot de passe associé
	 */
	public void ouvrirBase(String url, String nom, String passwd) {
		try{

			Class.forName("oracle.jdbc.driver.OracleDriver");
			System.out.println("DRIVER OK ! ");

			this.connect = DriverManager.getConnection(url, nom, passwd);

			System.out.println("Connection effective à "+url);
		}catch (java.sql.SQLException e){
			System.out.println("ORA-01017: invalid username/password; logon denied");
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Connexion refusée. Vérifiez que le nom de machine et le port sont corrects et que postmaster accepte les connexions TCP/IP.");
		}
		// On desactive la validation des transactions automatiques
		try {
			this.connect.setAutoCommit(false);
		} catch (SQLException e) {
			System.out.println("Le mode autocommit n'a pas ete desactive");
			e.printStackTrace();
		}
	}


	/**
	 *  fermeture de la connexion
	 */
	public void fermerBase() {
		try {
			this.connect.close();
		} catch (SQLException e) {
			System.out.println("La connexion ne s'est pas fermée correctement");
			e.printStackTrace();
		}
	}

	/**
	 *  création d'un Statement pour des ResultSet non modifiables et non
	 *  navigables
	 *
	 *@return    le Statement
	 */
	public Statement creerStatement() {
		try {
			Statement statement = this.connect.createStatement(Base.forwardOnly, Base.concurReadOnly);
			return statement;
		} catch (SQLException e) {
			System.out.println("echec de la creation du statement");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *  création d'un Statement pour des ResultSet modifiables et navigables
	 *
	 *@return    le Statement
	 */
	public Statement creerStatement(int type, int operation ) {
		try {
			Statement statement = this.connect.createStatement(type,operation);
			return statement;
		} catch (SQLException e) {
			System.out.println("echec de la creation du statement");
			e.printStackTrace();
			return null;
		}
	}


	/**
	 *  création d'un CallableStatement pour un appel de module stocké donné
	 *
	 *@param  s  L'appel de procédure ou fonction
	 *@return    le CallableStatement
	 */
	public CallableStatement creerAppel(String s) {
		try {
			CallableStatement callableStatement = this.connect.prepareCall(s);
			return callableStatement;
		} catch (SQLException e) {
			System.out.println("echec de la creation du callablestatement");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *  création d'un PreparedStatement 
	 *
	 *@param  s  La requete qu'il faut preparer
	 *@return    le PreparedStatement
	 */
	public PreparedStatement preparerRequete(String s) {
		try{
			PreparedStatement preparedStatement = this.connect.prepareStatement(s);
			return preparedStatement;
		}catch(SQLException e) {
			System.out.println("échec de la creation du PreparedStatement");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creer un ResultSet qui n'ont pas le comportement par default
	 * @param query
	 * @param type
	 * @param operation
	 * @return
	 */
	public ResultSet creerResultSet(String query,int type,int operation){
		try {
			return this.creerStatement(type, operation).executeQuery(query);
		} catch (SQLException e) {
			System.out.println("echec de la creation du resultSet personnalisee");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Valider la transaction courante sur la Base
	 */
	public void validerTransaction(){
		try {
			this.connect.commit();
			System.out.println("La transaction a ete valide");
		} catch (SQLException e) {
			System.out.println("La validation de la transaction a echoue");
			e.printStackTrace();
		}
	}

	/**
	 *  Annuler la transaction courante sur la Base
	 */
	public void annulerTransaction(){
		try {
			this.connect.rollback();
			System.out.println("La transaction a ete annulee");
		} catch (SQLException e) {
			System.out.println("L' annulation de la transaction a echoue");
			e.printStackTrace();
		}
	}
}