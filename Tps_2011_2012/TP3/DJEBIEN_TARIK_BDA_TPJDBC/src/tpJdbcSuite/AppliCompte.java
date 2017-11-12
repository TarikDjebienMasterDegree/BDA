package tpJdbcSuite;

import java.io.IOException;

public class AppliCompte {

	/**
	 * Compte bancaire du client
	 */
	private Compte c1;

	/**
	 * Compte bancaire destination
	 */
	private Compte c2;

	/**
	 * Somme du virement a effectuer
	 */
	private double montantVirement=0.0;

	/**
	 * Objet pour la lecture sur l'entree standard 
	 */
	private static Lecture lecture = new Lecture();

	/**
	 * La BDD relationnelle stockant nos compte bancaire 
	 */
	private Base maBase;

	/**
	 * Accesseur de la base
	 * @return la BDD relationnelle
	 */
	public Base getMaBase() {
		return maBase;
	}

	/**
	 * Constructeur par default
	 */
	public AppliCompte(){
		this.maBase=new Base();
		new Meta(this.maBase);
		Meta.generalites();
		Meta.listeTables();
		Meta.afficheResultatRequete("SELECT * FROM COMPTE");
	}

	/**
	 * Lance le processus d'initialisation des comptes bancaires
	 */
	public void initCompte(){
		String id_compte1 = null;
		String id_compte2 = null;

		// On recupere l'identifiant du compte courant
		System.out.println("Entrer l'identifiant de votre compte svp");
		System.out.print("id_compte : ");
		try {
			id_compte1 = AppliCompte.lecture.nextLine();
		} catch (IOException e) {
			System.out.println("Erreur dans la lecture de votre identifiant de compte bancaire");
			e.printStackTrace();
		}
		if(id_compte1!=null){
			this.c1=new Compte(id_compte1,this.getMaBase());
			System.out.println("L'identifiant de votre compte : "+id_compte1);
		}

		// On recupere l'identifiant du compte cible
		System.out.println("Entrer l'identifiant du compte vers lequel effectuer le virement svp");
		System.out.print("id_compte : ");
		try {
			id_compte2 = AppliCompte.lecture.nextLine();
		} catch (IOException e) {
			System.out.println("Erreur dans la lecture de l'identifiant du compte bancaire cible");
			e.printStackTrace();
		}
		if(id_compte2!=null){
			this.c2=new Compte(id_compte2, this.getMaBase());
			System.out.println("L'identifiant du compte pour le virement : "+id_compte2);
		}
		if(this.c1!=null && this.c2!=null){
			System.out.println("Operation d'initialisation des comptes bancaires terminee avec succes");
		}else{
			System.out.println("Operation d'initialisation des comptes bancaires echouee");
		}
	}

	/**
	 * Lance le processus de demande d'un virement dans l'application
	 */
	public void demandeVirement(){
		System.out.println("Quel est le montant de votre virement ? ");
		System.out.print("Montant : ");
		try {
			// Le montant du virement
			this.montantVirement = AppliCompte.lecture.nextDouble();
			// Condition de validation
			boolean leMontantEstPositif = this.montantVirement > 0;
			boolean lesComptesSontDifferents = !this.c1.getIdCompte().equals(this.c2.getIdCompte());
			boolean transactionTerminee = false;
			// Securite
			if(leMontantEstPositif && lesComptesSontDifferents){
				// Sauvegarde des montants précédents :
				double compteClientSoldeAvantDebit=this.c1.getSolde();
				System.out.println("Compte client avant debit : "+compteClientSoldeAvantDebit);
				double compteCibleSoldeAvantVersement=this.c2.getSolde();
				System.out.println("Compte cible avant versement : "+compteCibleSoldeAvantVersement);

				// On debite du compte du client le montant du virement
				this.c1.setSolde(this.c1.getSolde()-this.montantVirement);
				// On ajoute le montant du virement dans le compte destination
				this.c2.setSolde(this.c2.getSolde()+this.montantVirement);


				// montants courants :
				double compteClientSoldeApresDebit=this.c1.getSolde();
				double compteCibleSoldeApresVersement=this.c2.getSolde();
				System.out.println("Compte client apres debit : "+compteClientSoldeApresDebit);
				System.out.println("Compte cible apres debit : "+compteCibleSoldeApresVersement);

				// Condition de validation
				transactionTerminee = ((compteClientSoldeAvantDebit-this.montantVirement)==compteClientSoldeApresDebit)
						&& ((compteCibleSoldeAvantVersement+this.montantVirement)==compteCibleSoldeApresVersement);
			}
			// Valider la transaction si l'argent est bien retirer du compte et versée dans le second
			if(transactionTerminee){
				this.maBase.validerTransaction();
			}else{
				if(!leMontantEstPositif) 
					System.out.println("Entrer une somme positif!!!");
				if(!lesComptesSontDifferents) 
					System.out.println("Le compte cible doit etre different du courant!!!");
				if(!transactionTerminee) 
					System.out.println("ERROR : La somme n'a pas été correctement debiter puis verser.");
				this.maBase.annulerTransaction();
			}		
		} catch (NumberFormatException e) {
			System.out.println("Entrer une somme reelle svp ! exemple : 2.2 , 5.8, 14.58 etc ...");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Erreur durant la lecture de la somme pour le virement");
			e.printStackTrace();
		}
	}

	/**
	 * Programme principal de l'application de gestion des comptes
	 * @param args
	 */
	public static void main(String[] args) {
		AppliCompte monAppli = new AppliCompte();
		boolean enCours=true;
		String choix= "n";
		do{
			System.out.println("*************************************************************");
			System.out.println("***  Bienvenue dans l'application de gestion des comptes  ***");
			System.out.println("*************************************************************");

			// On initialise les deux comptes
			monAppli.initCompte();
			// Demande de virement
			monAppli.demandeVirement();
			// On demande à l'utilisateur s'il souhaite reiterer la transaction
			System.out.println("Souhaitez-vous refaire une nouvelle transaction");
			System.out.println("Oui : y");
			System.out.println("Non : n");
			try {
				choix = lecture.nextLine();
			} catch (IOException e) {
				System.out.println("erreur de lecture sur le clavier");
				e.printStackTrace();
			}
			enCours=(choix.toLowerCase().equals("y"))?true:false;
		}while(enCours);

		System.out.println("*************************************************************");
		System.out.println("****  Fermeture de l'application de gestion des comptes  ****");
		System.out.println("*************************************************************");

		//On libere les ressources memoires
		monAppli.getMaBase().fermerBase();
	}

}
