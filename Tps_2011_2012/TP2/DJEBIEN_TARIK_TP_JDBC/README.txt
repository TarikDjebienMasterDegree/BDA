README :

// Auteur : Djebien Tarik
// Date : Octobre 2011
// UE : BDA
// Objet : TP2 gestion de stock PL/SQL et JDBC


Arborescence de l'archive :
     |
     |___paq_stockage_body.sql : Le script de création du paquetage PL/SQL (fait en binôme avec Rakotobe eric )
     |
     |___lib/ le JAR driver Oracle ojdbc14
     |
     |___stock.jar le JAR de l'application contenant les sources et la Javadoc, celui-ci doit être dans le même dossier que /lib lors de son execution avec "$java -jar stock.jar" pour être fonctionnel.
     |
     |___README.txt ce fichier courant

Remarque :
Lors du chargement de la JFrame, spécifiée l'URL de connexion et le user/password en fonction de l'intsance de BDD utilisée.
Les valeurs mises par default dans les JLabel correspondent à l'url de ma BDD Oracle 10g express edition en localhost (pas  filora10g).

Cordialement,

Djebien Tarik.
