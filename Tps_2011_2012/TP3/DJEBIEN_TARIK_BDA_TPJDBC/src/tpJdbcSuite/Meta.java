package tpJdbcSuite;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * La classe stockant les metadonnees de notre BDD
 * @author tarik
 *
 */
public class Meta {
	
	/**
	 * La BDD relationnelle stockant nos compte bancaire 
	 */
	private static Base maBase;
	
	/**
	 * Les metadonnees de notre base
	 */
	private static DatabaseMetaData databaseMetadata;

	/**
	 * Constructeur d'initialisation de les Metadonnée de notre Base
	 * @param base la BDD relationnelle
	 */
	public Meta(Base base){
		Meta.maBase=base;
		try {
			Meta.databaseMetadata=Meta.maBase.getConnection().getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Accesseur de nos metadonnees
	 * @return
	 */
	public static DatabaseMetaData getDatabaseMetaData(){
		return Meta.databaseMetadata;
	}
	
	/**
	 * Methode qui affiche les informations de la BDD
	 */
	public static void generalites(){
		try {
			//Le nom et la version du produit SGBD interroge
			System.out.println("Nom : "+Meta.getDatabaseMetaData().getDatabaseProductName());
			System.out.println("Version : "+Meta.getDatabaseMetaData().getDatabaseProductVersion());
            //Le niveau d'isolement des transactions
			System.out.println("Niveau d'isolement des transactions : "+Meta.getDatabaseMetaData().getDefaultTransactionIsolation());
			//Le nom de l'utilisateur connecte
			System.out.println("Nom utilisateur : "+Meta.getDatabaseMetaData().getUserName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Methode qui affiche la liste des tables, vues, synonymes
	 */
	public static void listeTables(){
		//affiche la liste des tables, vues, synonymes
		System.out.println("Liste des tables : ");
		try {
			String[] types = new String[]{"TABLE","VIEW","SYNONYM"};
			ResultSet rs = Meta.getDatabaseMetaData().getTables(null,Meta.getDatabaseMetaData().getUserName(), "%", types);
			while(rs.next()){
				System.out.println("Type : "+rs.getString("TABLE_TYPE"));
				System.out.println("Schema : "+rs.getString("TABLE_SCHEM"));
				System.out.println("Name : "+rs.getString("TABLE_NAME"));
				System.out.println("Desc : ");
				Meta.desc(rs.getString("TABLE_NAME"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Methode qui affiche la liste des procedures
	 */
	public static void listeProcedures(){
		System.out.println("Liste des procedures : ");
		try {
			ResultSet rs = Meta.getDatabaseMetaData().getProcedures(null, Meta.getDatabaseMetaData().getUserName(), "%");
			while(rs.next()){
				System.out.println("Name : "+rs.getString("PROCEDURE_NAME"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	 /**
	   *  Affiche le schéma d'une relation, façon "desc" de SQLPlus
	   * le second paramètre n'est pas indispensable si vous avez une variable d'instance de type Connection
	   *
	   *@param  nomTable          le nom de la table, vue, synonyme
	   *@param  connect          la connection à la base
	   */
	  public static void desc(String nomTable) {
	    

	    try {
	      DatabaseMetaData dmd = Meta.getDatabaseMetaData();

	      ResultSet lesColonnes = dmd.getColumns(null, dmd.getUserName(), nomTable, "%");
	      java.util.Set<String> laPK = new java.util.HashSet<String>() ;
	    
	      // on remplit l'ensemble laPK avec les colonnes constituant la clef primaire de la tables
	      ResultSet lesClefs = dmd.getPrimaryKeys(null, dmd.getUserName(), nomTable);
	      if (lesClefs == null) {
	        System.out.println("getPrimaryKeys ne donne rien");
	      } else {
	        while (lesClefs.next()) {
	          laPK.add(lesClefs.getString("COLUMN_NAME"));
	        }
	      }
	    
	      // on affiche les colonnes
	      while (lesColonnes.next()) {
	        String s = "NULL" ;
	        if ((lesColonnes.getString("is_nullable")).equals("NO")) s = "NOT NULL" ;
	        if (laPK.contains(lesColonnes.getString("column_name"))) s = s + " PK";
	        System.out.println(lesColonnes.getString("column_name") + " " + lesColonnes.getString("type_name")+ " " + lesColonnes.getInt("column_size")
	             + " " + s);
	        // si on veut le type SQL de java.sql, pas le type Oracle, utiliser lesColonnes.getInt("data_type")
	      }

	      // contraintes de clefs étrangères
	      lesClefs = dmd.getImportedKeys(null, dmd.getUserName(), nomTable);
	      if (lesClefs == null) {
	        System.out.println("getImportedKeys ne donne rien");
	      } else {
	        while (lesClefs.next()) {
	          System.out.println(lesClefs.getString("FK_NAME")+" : "+lesClefs.getString("FKCOLUMN_NAME") + "->" + lesClefs.getString("PKTABLE_NAME")+"."+lesClefs.getString("PKCOLUMN_NAME"));
	        }
	      }
	      
	    }catch(SQLException e){
	      System.err.println("erreur SQL " + e.getErrorCode() + " " + e.getMessage());
	      e.printStackTrace();
	    }
	  }
	  
	  /**
	   * Affiche le resultat d'une requete SQL en ligne de commande
	   */
	  public static void afficheResultatRequete(String query){
		  Statement statement = Meta.maBase.creerStatement();
		  try {
			ResultSet result = statement.executeQuery(query);
            ResultSetMetaData resultMeta = result.getMetaData();
			
			System.out.println("\n**********************************");
			// On affiche le nom des colonnes
			// Attention : contrairement aux indices de tableaux, les indices de colonnes SQL
			// commencent à 1!
			for(int i=1; i<= resultMeta.getColumnCount();i++){
				System.out.print("\t"+resultMeta.getColumnName(i).toUpperCase()+"\t *");
			}
			
			System.out.println("\n**********************************");
			// Tant que l'objet ResultSet retourne des lignes de resultats.
			// La methode next() permet de positionner l'objet sur la ligne de resultats.
			// Au premier tour de la boucle, cette methode place l'objet sur la premiere ligne.
			// Si on a pas positionne l'objet ResultSet et qu'on tente une lecture, une exception est levee.
			while(result.next()){
				for(int i=1;i<=resultMeta.getColumnCount();i++){
					System.out.print("\t"+result.getObject(i).toString()+"\t |");
				}
				System.out.println("\n----------------------------------");
			}
			
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	  }

}
