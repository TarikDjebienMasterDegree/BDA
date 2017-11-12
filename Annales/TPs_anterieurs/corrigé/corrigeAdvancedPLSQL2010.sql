-- Exercice 1 --> SQL dynamique

--------
--Q1.1--
--------
create or replace procedure MENAGE1(nom_table VARCHAR2) AS
BEGIN
  dbms_output.put_line('attention suppression de la table '||upper(nom_table));
  EXECUTE IMMEDIATE 'drop table '||upper(nom_table)|| ' cascade constraints' ;
END;

--------
--Q1.2--
--------
create or replace procedure MENAGE2(nom_obj VARCHAR2, type_obj VARCHAR2) AS
  cpt NUMBER ;
  stmt VARCHAR2(100) ;
  fin VARCHAR2(30);
  cursor c is
    select *
    from USER_OBJECTS
    where OBJECT_NAME like upper(nom_obj)
    and OBJECT_TYPE = upper(type_obj) ;
BEGIN
  select count(*) into cpt
  from USER_OBJECTS
  where OBJECT_NAME like upper(nom_obj)
  and OBJECT_TYPE = upper(type_obj) ;
  dbms_output.put_line('il y a '||cpt||' objet(s) concerné(s)');
  if upper(type_obj) = 'TABLE' then
    fin := ' cascade constraints' ;
  elsif upper(type_obj) = 'TYPE' then -- pas demandé mais pratique pour relationnel-objet
    fin := ' force';
  else
    fin := '' ;
  end if ;
  for obj in c loop
    stmt := 'drop '||obj.OBJECT_TYPE||' '||obj.OBJECT_NAME || fin ;
    dbms_output.put_line(obj.OBJECT_NAME||' supprimé') ;
    execute immediate stmt ;  
  end loop ;
END;
---------------------------------------------------------------------------
-- Exercice 2 --> curseurs "for update"

-- La question 2.1  = création des tables
--------
--Q2.2--
--------
update equipe 
set total = (select j1.points+j2.points+j3.points
             from Points_j1 j1, points_j2 j2, points_j3 j3 
             where j1.id_equ = equipe.id and j2.id_equ = equipe.id and j3.id_equ = equipe.id);
--> on parcourt la table Points_j1 autant de fois qu'il y a de ligne dans Equipe
-- idem pour points_j2 et points_j1
-- ici il y a 20 équipes donc 20 parcours de chaque table POINT_Ji
-- plan d'execution :
UPDATE STATEMENT	ALL_ROWS	                3	20	520			
  UPDATE CARON.EQUIPE							
    TABLE ACCESS(FULL) CARON.EQUIPE		        3	20	520			
    MERGE JOIN(CARTESIAN)		                9	1	78			
      MERGE JOIN(CARTESIAN)	                       	6	1	52			
        TABLE ACCESS(FULL) CARON.POINTS_J1		3	1	26			
        BUFFER(SORT)		                        3	1	26			
           TABLE ACCESS(FULL) CARON.POINTS_J2		3	1	26			
      BUFFER(SORT)		                        6	1	26			
        TABLE ACCESS(FULL) CARON.POINTS_J3		3	1	26	

--------
--Q2.3--
--------
create or replace procedure ajout_points is
  cursor les_points is
  select points_j1.id_equ, points_j1.points p1, points_j2.points p2, points_j3.points p3
  from points_j1
  join points_j2 on points_j1.id_equ = points_j2.id_equ
  join points_j3 on points_j2.id_equ = points_j3.id_equ ;
begin
  for une_ligne in les_points loop
    update equipe
    set total = une_ligne.p1 + une_ligne.p2 + une_ligne.p3
    where une_ligne.id_equ = equipe.id ;
  end loop ;
end ;
--> on parcourt 1 seule fois chaque relation POINTS_Ji, 
-- mais on fait 1 update par équipe donc 20 requêtes update
-- plan d'execution du select dans le curseur :
SELECT STATEMENT	ALL_ROWS	        10	20	1560			
  HASH JOIN	                            	10	20	1560			
    HASH JOIN		                        7	20	1040			
      TABLE ACCESS(FULL) CARON.POINTS_J1	3	20	520			
      TABLE ACCESS(FULL) CARON.POINTS_J2	3	20	520			
    TABLE ACCESS(FULL) CARON.POINTS_J3		3	20	520	


--------
--Q2.4--
--------
create or replace procedure ajout_points_bis is
  cursor les_points is
  select equipe.total, points_j1.id_equ, points_j1.points p1, points_j2.points p2, points_j3.points p3
  from equipe
  join points_j1 on equipe.id = points_j1.id_equ
  join points_j2 on points_j1.id_equ = points_j2.id_equ
  join points_j3 on points_j2.id_equ = points_j3.id_equ
  for update of total;
  -- attention, un for update tout seul ne marche pas (le update ne modifie rien)
  -- il faut préciser la colonne à modifier.
begin
  for une_ligne in les_points loop
    update equipe
    set total = une_ligne.p1 + une_ligne.p2 + une_ligne.p3
    where current of les_points ;
  end loop ;
end ;
--> on profite du curseur pour faire l'update
-- on a autant d'instructions update que précédemment mais comme elles se
-- font en utilisant le curseur, on économise le temps de la recherche de la ligne à modifier

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
-- calcul du classement des équipes

alter table equipe add (
classement number(2) default 0 not null
);
--------
--Q2.5--
--------
update equipe e1
set classement = (select count(*)+1 from equipe e2 where e1.total < e2.total);

UPDATE STATEMENT	ALL_ROWS	83	20	520			
  UPDATE CARON.EQUIPE							
    TABLE ACCESS(FULL) CARON.EQUIPE		3	20	520			
    SORT(AGGREGATE)			1	13			
      TABLE ACCESS(FULL) CARON.EQUIPE		3	1	13			
--------
--Q2.6--
--------
create or replace procedure calculer_classements is
  cursor les_equipes is
  select * from equipe
  order by total desc
  for update ;
  compteur1 Number := 1 ;
  total_prec equipe.total%type:=0;
begin
  for une_equipe in les_equipes loop
    if une_equipe.total <> total_prec then
      compteur1 := les_equipes%rowcount ;
    end if ;
    update equipe set classement = compteur1 where current of les_equipes ;
    total_prec := une_equipe.total ;
  end loop ;
end ;

-----------------------------------------------------------------------------
-- Exercice 3 --> Bulk SQL

-- on utilise les tables Pays et Ville du TP Tuning

--------
--Q3.1--
--------
create or replace procedure afficher_villes(masque_ville VARCHAR2) is
  -- on a besoin d'une collection pour stocker le résultat du "bulk collect"
  Type table_villes is table of ville%rowtype ;
  les_villes table_villes ;
begin
  select * bulk collect into les_villes
  from ville 
  where nom like masque_ville ;
  
  for i in 1 .. les_villes.count loop
  -- si on écrit les_villes.first .. les_villes.last il faut vérifier 
  -- que les_villes.count <> 0
    dbms_output.put_line(les_villes(i).nom);
  end loop ;
end ;

--------
--Q3.2--
--------
create or replace procedure afficher_villes_bis(masque_ville VARCHAR2) is
  -- ici on va récupérer les lignes par paquets de 25
  -- on a toujours besoin d'une collection (pour récupérer 25 lignes)
  Type table_villes is table of ville%rowtype ;
  les_villes table_villes ;
  -- on a aussi besoin d'un curseur pour l'itération
  cursor villes_cur is
  select * from ville 
  where nom like masque_ville ;
begin
  open villes_cur ;
  loop
    fetch villes_cur bulk collect into les_villes limit 25 ;
    exit when les_villes.count = 0 ; -- ne pas utiliser villes_cur%notfound
                                     -- sinon on rate le dernier paquet
    for i in 1 .. les_villes.count loop
      dbms_output.put_line(les_villes(i).nom);
    end loop ;
  end loop ;
end ;

--------
--Q3.3--
--------
create or replace procedure supprimer_villes(masque_ville VARCHAR2) is
  -- une collection pour récupérer les noms des villes supprimées
  Type table_noms_villes is table of ville.nom%type ;
  les_noms_villes table_noms_villes ;
begin
  -- on supprime, en récupérant les noms des villes supprimées
  delete from ville where nom like masque_ville
  returning nom bulk collect into les_noms_villes;

  -- on affiche le contenu de la collection les_noms_villes
  for i in 1.. les_noms_villes.count loop
    dbms_output.put_line(les_noms_villes(i));
  end loop ;
end ;


