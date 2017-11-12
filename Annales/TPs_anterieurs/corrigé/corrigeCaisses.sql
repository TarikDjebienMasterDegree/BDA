create table caisse (
  identifiant varchar2(2) constraint caisse_pkey primary key,
  ouverte number(1),
  constraint ouverte_bool check (ouverte between 0 and 1)
) ;

create table client (
  numero number(3) constraint client_pkey primary key,
  duree_prevue number(3),
  idCaisse varchar2(3) constraint affect_caisse_fkey references caisse,
  rang number(2)
) ;

alter table caisse add(
  nbClients number default 0 not null
  );

create or replace view caisses_ouvertes as
select identifiant from caisse
where ouverte = 1 ;
                    
create or replace view durees_attentes as
select identifiant, nvl(sum(duree_prevue),0) duree
from caisses_ouvertes
left join client on idCaisse = identifiant 
group by identifiant ;

create or replace view meilleures_caisses as
select identifiant
from durees_attentes where duree = (select min(duree) from durees_attentes);

create or replace package PAQ_CAISSE as
  procedure affectation_client(leNumClient CLIENT.numero%type) ;
  
  procedure passer_client(la_caisse caisse.identifiant%type) ;
  
  procedure ouvrir_caisse(la_caisse caisse.identifiant%type) ;
  
  procedure fermer_caisse(la_caisse caisse.identifiant%type) ;
  
  -- fonction constante car 
  -- les constantes ne sont pas accessibles en JDBC
  function taille_max return NUMBER ;
  
  function nb_clients(la_caisse caisse.identifiant%type) return NUMBER ;
  
  PAS_DE_CAISSES_DISPO Exception ;
  PRAGMA exception_init(PAS_DE_CAISSES_DISPO,-20105);
  
  CLIENT_DEJA_EN_CAISSE Exception ;
  PRAGMA exception_init(CLIENT_DEJA_EN_CAISSE,-20106);
  
  PAS_DE_CLIENT Exception ;
  PRAGMA exception_init(PAS_DE_CLIENT,-20107);
  
  CAISSE_INCONNUE Exception ;
  PRAGMA exception_init(PAS_DE_CLIENT,-20108);
  
end ;

create or replace trigger calcul_nb_clients
after insert or delete or update of idCaisse 
on Client
for each row
begin
  if deleting or updating then
    if (:old.idCaisse is not null) then
      update Caisse
      set nbClients = nbClients-1
      where identifiant = :old.idCaisse ;
    end if ;
  end if ;
  if inserting or updating then
    if (:new.idCaisse is not null) then
      update Caisse
      set nbClients = nbClients+1
      where identifiant = :new.idCaisse ;
    end if ;
  end if ;
end ;

create or replace package body PAQ_CAISSE as

  procedure affectation_client(leNumClient CLIENT.numero%type) is
    meilleure_caisse CAISSE.identifiant%type ;
    nbClients number ;
    affect_cour CAISSE.identifiant%type ;
  begin
    -- on recherche une caisse ouverte la meilleure possible
    select max(identifiant) into meilleure_caisse
    from meilleures_caisses ;
    if meilleure_caisse is null then
      raise PAS_DE_CAISSES_DISPO ;
    end if ;
    
    -- est-ce que le client est deja en caisse
    select idCaisse into affect_cour
    from client
    where numero = leNumClient ;
    if affect_cour is not null then
      raise CLIENT_DEJA_EN_CAISSE ;
    end if ;
    
    -- on calcule le rang
    -- on recherche d'abord le nb de clients
    nbClients := nb_clients(meilleure_caisse);
    
    update client set idCaisse=meilleure_caisse,
                      rang=nbClients+1
    where numero = leNumClient;
  end ;
  
  procedure passer_client(la_caisse caisse.identifiant%type) is
  begin
    delete from client 
    where rang = 1 and idCaisse = la_caisse ;
    if SQL%rowcount = 0 then --pas de client a cette caisse
      raise PAS_DE_CLIENT ;
    end if ;
    update client set rang = rang-1 
    where idCaisse = la_caisse ;
  end ;
  
  procedure ouvrir_caisse(la_caisse caisse.identifiant%type) is
  begin
    update caisse set ouverte=1
    where identifiant = la_caisse ;
    if SQL%rowcount = 0 then
      raise CAISSE_INCONNUE ;
    end if;
  end ;
  
  procedure fermer_caisse(la_caisse caisse.identifiant%type) is
    cursor la_file_attente is
    select * from client where idCaisse = la_caisse ;
  begin
    update caisse set ouverte=0 where identifiant = la_caisse;
    for un_client in la_file_attente loop
      dbms_output.put_line('client numero '||un_client.numero);
      update client set idCaisse=null, rang=null
      where numero = un_client.numero ;
      dbms_output.put_line('affectation de '||un_client.numero);
      affectation_client(un_client.numero) ;
    end loop ;
  end ;
  
  function taille_max return NUMBER is
  begin
    return 3 ;
  end ;
  
  function nb_clients(la_caisse caisse.identifiant%type) return NUMBER is
    nb NUMBER ;
  begin
    select nbClients into nb
    from Caisse
    where identifiant = la_caisse ;
    return nb ;
  exception
    when NO_DATA_FOUND then raise CAISSE_INCONNUE ;
  end ;
end ;




