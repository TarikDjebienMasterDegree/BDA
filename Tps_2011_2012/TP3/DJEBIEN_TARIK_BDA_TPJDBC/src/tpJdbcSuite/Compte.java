package tpJdbcSuite;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Compte {

	private ResultSet ID_compte_and_Solde;

	public Compte(String id_compte, Base base){
		//Requete la table compte pour recuperer la ligne correspondant au compte d'ID_compte
		String query = "SELECT C.id_compte, C.solde " +
				"FROM compte C " +
				"WHERE C.id_compte = "+id_compte;
		//Recupere le ResultSet d'une seule ligne contenant les informations du compte courant
		this.ID_compte_and_Solde=
				base.creerResultSet(
						query,
						Base.scrollSensitive,
						Base.concurUpdatable
						);
		try {
			this.ID_compte_and_Solde.next();
		} catch (SQLException e) {
			System.out.println("erreur positionnement du curseur sur le resultSet");
			e.printStackTrace();
		}
	}

	public double getSolde(){
		try {
			return this.ID_compte_and_Solde.getDouble("solde");
		} catch (SQLException e) {
			System.out.println("echec de la recuperation du solde : valeur 0.0 renvoyee");
			e.printStackTrace();
			return 0.0;
		}
	}

	public void setSolde(double s){
		boolean compteMisAjour=false;
		try {
			this.ID_compte_and_Solde.updateDouble("solde", s);
			compteMisAjour=true;
		} catch (SQLException e) {
			System.out.println("Le solde n'a pas ete modifie");
			e.printStackTrace();
		}
		if(compteMisAjour){
			try {
				this.ID_compte_and_Solde.updateRow();
			} catch (SQLException e) {
				System.out.println("La mise a jour du solde a echoue");
				e.printStackTrace();
			}
		}
	}

	public String getIdCompte(){
		try {
			return this.ID_compte_and_Solde.getString("ID_COMPTE");
		} catch (SQLException e) {
			System.out.println("echec de la recuperation de l'ID du compte : valeur echecRecupID renvoyee");
			e.printStackTrace();
			return "echecRecupID";
		}
	}


}
