create type tab_rowid_type as table of VARCHAR2(18);

-- question 3.1
create or replace function requete_generique(la_table USER_TAB_COLS.TABLE_NAME%type, condition VARCHAR2) 
return tab_rowid_type is
  stmt VARCHAR2(100) ;
  tab_rowid tab_rowid_type ;
begin
  -- on fabrique la requête
  if condition is null then
    stmt := 'select rowid from '||la_table ;
  else
    stmt := 'select rowid from '||la_table||' where '||condition ;
  end if ;
  -- on sélectionne les ROWID
  execute immediate stmt bulk collect into tab_rowid ;
  
  return tab_rowid ;
  
end ;

-- on teste
ACTIVITE :

ROWID               LIBELLE
AABD3CAAEAAADO9AAD 	peinture
AABD3CAAEAAADO9AAE 	football
AABD3CAAEAAADO9AAF 	danse

set serveroutput on

declare
  res tab_rowid_type ;
begin
  res := requete_generique('ACTIVITE','libelle like ''%a%''');
  for i in 1 .. res.count loop
    dbms_output.put_line(res(i));
  end loop;
end ;
-- affichage :
AABD3CAAEAAADO9AAE
AABD3CAAEAAADO9AAF
Procédure PL/SQL terminée avec succès.

-- question 3.2

create or replace procedure requete_generique_objet(la_table USER_TAB_COLS.TABLE_NAME%type, condition VARCHAR2) is
  stmt VARCHAR2(500) ;
  --tab_rowid tab_rowid_type ;
begin
  stmt := 'declare t tab_'||la_table||'_type ; begin ' ;
  if condition is null then
    stmt := stmt||'select value(l) bulk collect into t from '||la_table ||' l ;';
  else
    stmt := stmt||'select value(l) bulk collect into t from '||la_table||' l where '||condition ||' ;';
  end if ;
  stmt := stmt||' for i in t.first .. t.last loop t(i).afficher ; end loop;' ;
  stmt := stmt||' end;' ;
  dbms_output.put_line(stmt);

  execute immediate stmt ;
  
end ;

-- on teste
create type tab_enfant_type as table of enfant_type;
create type tab_activite_type as table of activite_type;

execute requete_generique_objet('activite','id_activite>10');
-- affichage :
declare t tab_activite_type ; begin select value(l) bulk collect into t from activite l where id_activite>10 ; for i in t.first .. t.last loop
t(i).afficher ; end loop; end;
football
danse
Procédure PL/SQL terminée avec succès.
