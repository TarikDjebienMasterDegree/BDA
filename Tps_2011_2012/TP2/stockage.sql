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

-- on cree les hangars
begin
  for i in 1..5 loop
    for j in 1..9 loop
      insert into hangar(lig,col,capacite) values(i,j,i*1000);
    end loop;
  end loop;
end ;

