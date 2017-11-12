create table HANGAR (
  lig NUMBER(2),
  constraint interval_lig check (lig between 1 and 5),
  col NUMBER(2),
  constraint interval_col check (col between 1 and 9),
  constraint hangar_pkey primary key(lig,col),
  capacite NUMBER not null,
  volume_reel NUMBER DEFAULT 0 not null,
  constraint vol_inf_cap check (volume_reel <= capacite)
) ;

create table PRODUIT (
  id_produit NUMBER(3) constraint produit_pkey primary key,
  nom VARCHAR2(20) not null,
  categorie NUMBER(2) not null,
  constraint interval_categ check (categorie between 1 and 50)
) ;

create table CONTAINER (
  id_container NUMBER(4) constraint container_pkey primary key,
  volume NUMBER not null,
  id_produit NUMBER constraint container_produit_fkey references PRODUIT,
  lig NUMBER(2),
  col NUMBER(2),
  constraint container_hangar_fkey foreign key(lig,col) references HANGAR
);

-- on cree les hangars
begin
  for i in 1..5 loop
    for j in 1..9 loop
      insert into hangar(lig,col,capacite) values(i,j,i*1000);
    end loop;
  end loop;
end ;

-- on cree des produits
insert into produit values(1,'produit 1',2);
insert into produit values(2,'produit 2',2);
insert into produit values(3,'produit 3',2);
insert into produit values(4,'produit 4',2);
insert into produit values(5,'produit 5',10);
insert into produit values(6,'produit 6',10);
insert into produit values(7,'produit 7',23);
insert into produit values(8,'produit 8',23);
insert into produit values(9,'produit 9',23);

-- des containers, pas encore rangés dans les hangars
-- prod de catégorie 2
insert into container values (1,300,3,null,null);
insert into container values (2,200,1,null,null);
insert into container values (6,200,3,null,null);
-- prod de categorie 23
insert into container values (3,600,9,null,null);
insert into container values (8,800,8,null,null);
insert into container values (9,500,9,null,null);
-- prod de categorie 10
insert into container values (4,400,5,null,null);
insert into container values (5,300,5,null,null);
insert into container values (10,300,6,null,null);
-- container vide  
insert into container values (7,300,null,null,null); 

-- une vue
-- les hangars non vides avec leur catégorie de prod
-- (on peut aussi utiliser un distinct)
create or replace view hangar_avec_categ as
select lig,col,volume_reel, capacite, categorie
from hangar
natural join container
natural join produit
group by lig,col,volume_reel, capacite, categorie ;

-- autre vue,
-- les hangars vides
create or replace view hangar_vide as
select lig, col, capacite
from hangar where 0 = (select count(*) from container where 
hangar.lig = container.lig and hangar.col = container.col);

-- on peut utiliser plus simplement volume_reel !
create or replace view hangar_vide as
select lig, col, capacite
from hangar where volume_reel=0;

-- trigger
create or replace trigger calcul_vol_reel 
before insert or delete or update of lig,col,volume
on container
for each row
begin
  if (deleting or updating) then
    update hangar
    set volume_reel = volume_reel - :old.volume
    where hangar.lig = :old.lig and hangar.col = :old.col ;
  end if ;
  if (inserting or updating) then
    update hangar
    set volume_reel = volume_reel + :new.volume
    where hangar.lig = :new.lig and hangar.col = :new.col ;
  end if ;
end calcul_vol_reel ;

-- le paquetage
create or replace package PAQ_STOCKAGE as

  function identif_hangar(ligne hangar.lig%type, colonne hangar.col%type) return NUMBER ;
  
  procedure affecter_container(le_container container.id_container%type) ;
  
  PAS_HANGAR_DISPO Exception ;
  PRAGMA exception_init(PAS_HANGAR_DISPO,-20001);
  
  CONTAINER_INCONNU Exception ;
  PRAGMA exception_init(CONTAINER_INCONNU,-20002);
  
  CONTAINER_VIDE Exception ;
  PRAGMA exception_init(CONTAINER_VIDE,-20003);
end PAQ_STOCKAGE ;


create or replace package body PAQ_STOCKAGE as
  function identif_hangar(ligne hangar.lig%type, colonne hangar.col%type) return NUMBER is
  begin
    return 10*ligne + colonne ;
  end ;
  
  procedure affecter_container(le_container container.id_container%type) is
    cat Produit.categorie%type ;
    vol container.volume%type ;
    prod container.id_produit%type;
    ligne hangar.lig%type ;
    colonne hangar.col%type ;
    nb NUMBER ;
  begin
    -- on recherche la catégorie du produit stocké dans le container
    -- ainsi que le volume du container
    begin
      select categorie,volume,container.id_produit into cat,vol,prod
      from container
      left join produit on container.id_produit = produit.id_produit
      where id_container = le_container ;
    exception 
      when NO_DATA_FOUND then 
        raise CONTAINER_INCONNU ;
    end ;
    -- on vérifie que le container est plein
    if (prod is null) then raise CONTAINER_VIDE ; end if ;
    
    dbms_output.put_line('categorie : '||cat||' volume : '||vol);
    -- on met à null lig et col pour ce container
    update container
    set lig=null, col=null
    where id_container=le_container;
    -- on recherche un hangar dispo pour cette categorie
    -- est-ce qu'on peut compléter un hangar
    select min(identif_hangar(lig,col)) into nb
    from hangar_avec_categ
    where capacite-volume_reel > vol
    and categorie = cat ;
    -- nb peut être null
    if (nb is null) then
      -- est-ce qu'il y a encore un hangar vide
      select min(identif_hangar(lig,col)) into nb
      from hangar_vide ;
      dbms_output.put_line('hangar : '||nb||' vide ');
      if nb is null then raise PAS_HANGAR_DISPO ; end if ;
    else
      dbms_output.put_line('hangar : '||nb||' a completer ');
    end if ;
    colonne := nb mod 10 ;
    ligne := (nb-colonne)/10 ;
    dbms_output.put_line('ligne : '||ligne||' colonne : '||colonne);
    -- on fait l'affectation
    update container
    set lig = ligne, col = colonne
    where id_container = le_container ;
  end ;
  
end PAQ_STOCKAGE ;

execute paq_stockage.affecter_container(50); -- container inconnu
ERREUR à la ligne 1 :
ORA-20002:
ORA-06512: à "CARON.PAQ_STOCKAGE", ligne 24
ORA-01403: aucune donnée trouvée
ORA-06512: à ligne 1 

execute paq_stockage.affecter_container(7); -- container vide
ERREUR à la ligne 1 :
ORA-20003:
ORA-06512: à "CARON.PAQ_STOCKAGE", ligne 27
ORA-06512: à ligne 1 

execute paq_stockage.affecter_container(1);
categorie : 2 volume : 300
hangar : 11 vide
ligne : 1 colonne : 1
Procédure PL/SQL terminée avec succès.

execute paq_stockage.affecter_container(2);
categorie : 2 volume : 200
hangar : 11 a completer
ligne : 1 colonne : 1
Procédure PL/SQL terminée avec succès.

execute paq_stockage.affecter_container(3);
categorie : 23 volume : 600
hangar : 12 vide
ligne : 1 colonne : 2
Procédure PL/SQL terminée avec succès.

execute paq_stockage.affecter_container(8);
categorie : 23 volume : 800
hangar : 13 a completer
ligne : 1 colonne : 3
Procédure PL/SQL terminée avec succès.

-- on supprime tous les hangars vides
delete from hangar where (lig=1 and col>4) or (lig > 1)

execute paq_stockage.affecter_container(4); -- plus de place
categorie : 10 volume : 400
hangar : vide

BEGIN paq_stockage.affecter_container(4); END;

*

ERREUR à la ligne 1 :
ORA-20001:
ORA-06512: à "CARON.PAQ_STOCKAGE", ligne 46
ORA-06512: à ligne 1 

