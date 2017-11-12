import java.sql.*;

/**
 *  cette classe permet d'appeler les procedures et fonctions du paquetage 
 *  stocke paq_stockage
 *  (paquetage PL/SQL).
 */
public class Stock {

  private Base laBase;
  private CallableStatement CsAffecterContainer ;
  private PreparedStatement reqProd ;
  private Statement stmt ;


  /**
   * Constructeur de Stock
   * @param b l'objet Base avec la connexion ouverte
   */
  public Stock(Base b) {
    this.laBase = b;
    this.CsAffecterContainer
       = laBase.creerAppel("{call paq_stockage.affecter_container(?)}");
    this.reqProd = laBase.preparerRequete("select id_container, col, lig from container where id_produit = ?");
    this.stmt = laBase.creerStatement();
  }
  
  /**
   * permet de remplir un container avec un produit
   * Le container n'est plus range dans un hangar apres appel a cette methode
   * @param idContainer identifiant du container
   * @param idProduit identifiant du produit
   */
  public void remplirContainer(int idContainer, int idProduit) throws SQLException{
    int i = stmt.executeUpdate("update container set id_produit = "+idProduit+ ", lig=null, col=null where id_container="+idContainer);
    if (i == 0) throw new SQLException("ce container n'existe pas");
    // si le container existe mais pas le produit --> pb de clef étrangère donc SQLException
  }
  
  /**
   * permet de ranger un container dans un hangar
   * Le hangar sera trouve en fonction du type de produit et des containers deja ranges dans les hangars
   * @param idContainer identifiant du container a ranger
   */ 
  public void affecterContainer(int idContainer) throws SQLException{
    try{
      CsAffecterContainer.setInt(1,idContainer);
      CsAffecterContainer.execute();
    }catch(SQLException e){
      if (e.getErrorCode() == -20001) throw new SQLException("pas de hangar disponible");
      if (e.getErrorCode() == -20002) throw new SQLException("container inconnu");
    }
  }
  
  /**
   * affiche la liste des containers et le hangar ou ils sont stockes, pour un produit donne
   * affiche (0,0) comme (lig,col) de hangar signifie que le container n'est pas encore range dans un hangar.
   * @param idProduit identifiant du produit
   */
  public void voirStockProduit(int idProduit) throws SQLException{
    reqProd.setInt(1,idProduit);
    ResultSet res = reqProd.executeQuery() ;
    System.out.println("CONTAINER, LIGNE, COLONNE :");
    while (res.next()){
      System.out.println(res.getInt("id_container")+" "+res.getInt("lig")+" "+res.getInt("col"));
      // affiche 0 à la place de null
    }
  }
  
  public static void main(String args[]){
    try{
      Base b = new Base() ;
      Stock sto = new Stock(b) ;
      sto.remplirContainer(8,6) ;
      sto.affecterContainer(8);
      sto.voirStockProduit(6);
    }catch(SQLException sqle){
      System.out.println("erreur dans Stock "+sqle.getErrorCode()+" "+sqle.getMessage());
    }
  }
}
