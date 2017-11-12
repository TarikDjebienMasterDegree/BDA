drop type enfant_type force ;
drop type activite_type force ;
drop type ens_activite_type force ;
drop type ens_enfant_type force ;
drop type ref_enfant_type force ;
drop type ref_activite_type force ;

-- création des types
create type activite_type ;
/
create type ref_activite_type as object(
  ref_activite REF activite_type 
);
/
create type ens_activite_type as TABLE OF ref_activite_type ;
/
create type enfant_type as object(
  id_enfant NUMBER,
  nom VARCHAR2(20),
  prenom VARCHAR2(30),
  les_activites ens_activite_type,
  MEMBER PROCEDURE afficher ( SELF IN OUT NOCOPY enfant_type )
);
/

create type body enfant_type as
-- cette méthode sera utilisée pour tester l'exercice 3 question 3.2
  MEMBER PROCEDURE afficher ( SELF IN OUT NOCOPY enfant_type ) is
  begin
    dbms_output.put_line(nom || ' '|| prenom);
  end ;
end ;
/

create type ref_enfant_type as object(
  ref_enfant REF enfant_type 
);
/
create type ens_enfant_type as TABLE OF ref_enfant_type ;
/
create type activite_type as object(
  id_activite NUMBER,
  libelle VARCHAR2(50),
  les_inscrits ens_enfant_type,
  MEMBER PROCEDURE afficher ( SELF IN OUT NOCOPY activite_type )

);
/

create type body activite_type as
  MEMBER PROCEDURE afficher ( SELF IN OUT NOCOPY activite_type ) is
  begin
    dbms_output.put_line(libelle);
  end ;
end ;
/

-- création des tables
create table ACTIVITE of activite_type(
constraint activite_pkey primary key(id_activite),
constraint defaut_les_inscrits les_inscrits default ens_enfant_type()
)nested table les_inscrits store as tab_les_inscrits;

create table ENFANT of enfant_type(
constraint enfant_pkey primary key(id_enfant),
constraint defaut_les_activites les_activites default ens_activite_type()
)nested table les_activites store as tab_les_activites;

-- 2 enfants, 3 activités
insert into enfant(id_enfant,nom,prenom) values (1,'ROLLET','Léa');
insert into enfant(id_enfant,nom,prenom) values (2,'CARON','Théo');

insert into activite(id_activite,libelle) values (10,'peinture');
insert into activite(id_activite,libelle) values (20,'football');
insert into activite(id_activite,libelle) values (30,'danse');

-- la procédure de la question 2.2
create or replace procedure inscrire(enf enfant.id_enfant%type, act activite.id_activite%type) as
begin  
  -- on ajoute une ligne dans la table les_activites de cet enfant
  insert into the(select e.les_activites from enfant e where e.id_enfant = enf)
  select ref(a) from activite a where a.id_activite = act;
  
  -- on ajoute une ligne dans la table les_inscrits pour cette activité
  insert into the(select a.les_inscrits from activite a where a.id_activite = act)
  select ref(e) from enfant e where e.id_enfant = enf ;
end ;

-- on inscrit Léa Rollet aux activités 'peinture' et 'dance'
execute inscrire(1,30);
execute inscrire(1,10);
-- on inscrit Théo Caron à l'activité 'football'
execute inscrire(2,20);

-- un 3e enfant sans activité
insert into enfant(id_enfant,nom,prenom) values (3,'DUPONT','Arthur');

-- Q2.1 : requetes 

-- Les activités de Léa Rollet
select a.ref_activite.id_activite, a.ref_activite.libelle
from the(select e.les_activites from enfant e where e.nom = 'ROLLET' and e.prenom='Léa') a

-- ou bien
select a.ref_activite.libelle
from enfant e,
        table(e.les_activites) a
where e.nom = 'ROLLET' and e.prenom = 'Léa'

-- les activités avec leur nombre d'inscrits
select a.id_activite, a.libelle, (select count(*) from table(a.les_inscrits))
from activite a
--> le plan d'execution indique un cout 3 pour  cette requete

-- ou bien (pas tres objet)
select a.libelle,count(i.ref_enfant.id_enfant)
from activite a,
table (a.les_inscrits) i
group by a.libelle
--> le plan d'execution indique un cout 7 pour cette requete

-- remarque : on ne pas pas utiliser le count sans group by
select a.libelle,count(i.ref_enfant)
from activite a,
table (a.les_inscrits) i
--> ERREUR à la ligne 1 :
--  ORA-00937: la fonction de groupe ne porte pas sur un groupe simple 

-- les enfants qui ne sont inscrits à aucune activité
select e.nom, e.prenom
from enfant e
where (select count(*) from table(e.les_activites)) = 0 ;

-- cette requête ne fonctionne pas
-- car l'enfant qui n'a pas d'activite n'est pas
-- pris en compte (comme pour une jointure)
select e.nom, e.prenom, count(a.ref_activite)
from enfant e,
table (e.les_activites) a
group by e.nom, e.prenom
having count(a.ref_activite)=0
