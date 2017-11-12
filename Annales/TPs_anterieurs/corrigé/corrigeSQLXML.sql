---------------------------------------------------------
-- Génération de XML à partir de tables relationnelles --
---------------------------------------------------------

-- on veut les noms des pays
select xmlelement("pays", nom) from pays

<pays>france</pays>
<pays>cameroun</pays>
<pays>maroc</pays> 
...

-- ou bien
select xmlelement(name "pays", nom) from pays 

-- ou bien (bof)
select xmlforest(pays.nom as pays) from pays


-- on veut chaque pays avec 3 sous-elements ref_pays, nom et nb_habitants

select xmlelement("pays",
xmlforest(p.ref_pays, p.nom, p.nb_habitants)
)
 from pays p
 
<pays><REF_PAYS>1</REF_PAYS><NOM>france</NOM><NB_HABITANTS>61000000</NB_HABITANTS></pays>
<pays><REF_PAYS>2</REF_PAYS><NOM>cameroun</NOM><NB_HABITANTS>15700000</NB_HABITANTS></pays>
<pays><REF_PAYS>3</REF_PAYS><NOM>maroc</NOM><NB_HABITANTS>29600000</NB_HABITANTS></pays> 
...

-- ou bien
select xmlelement("pays",
xmlelement(name "ref_pays",p.ref_pays), 
xmlelement(name "nom",p.nom), 
xmlelement(name "nb_habitants",p.nb_habitants)
)
 from pays p
 
<pays><ref_pays>1</ref_pays><nom>france</nom><nb_habitants>61000000</nb_habitants></pays>
<pays><ref_pays>2</ref_pays><nom>cameroun</nom><nb_habitants>15700000</nb_habitants></pays>
<pays><ref_pays>3</ref_pays><nom>maroc</nom><nb_habitants>29600000</nb_habitants></pays> 
...

-- idem en mettant ref_pays en attribut
select xmlelement("pays",
xmlattributes(p.ref_pays), 
xmlelement(name "nom",p.nom), 
xmlelement(name "nb_habitants",p.nb_habitants)
)
 from pays p
 
<pays REF_PAYS="1"><nom>france</nom><nb_habitants>61000000</nb_habitants></pays>
<pays REF_PAYS="2"><nom>cameroun</nom><nb_habitants>15700000</nb_habitants></pays>
<pays REF_PAYS="3"><nom>maroc</nom><nb_habitants>29600000</nb_habitants></pays> 
...

-- ou bien en renommant l'attribut
select xmlelement("pays",
xmlattributes(p.ref_pays as id), 
xmlelement(name "nom",p.nom), 
xmlelement(name "nb_habitants",p.nb_habitants)
)
 from pays p
 
-- si on veut 1 seul document avec tout
select xmlelement("les_pays",
xmlagg(
xmlelement("pays",
xmlattributes(p.ref_pays as id), 
xmlelement(name "nom",p.nom), 
xmlelement(name "nb_habitants",p.nb_habitants)
)
)
)
 from pays p
 
  <les_pays>
   <pays ID="1"><nom>france</nom><nb_habitants>61000000</nb_habitants></pays>
   <pays ID="2"><nom>cameroun</nom><nb_habitants>15700000</nb_habitan ts></pays>
   <pays ID="3"><nom>maroc</nom><nb_habitants>29600000</nb_habitants>
   ...
 </les_pays> 

-- ou
select xmlelement("les_pays",
xmlagg( 
xmlelement("pays",
xmlattributes(p.ref_pays as id),
xmlforest(p.nom, p.nb_habitants)
)))
 from pays p


 
-- jointure entre pays et ville
select * from pays 
join ville on pays.ref_pays=ville.ref_pays

select xmlelement(name "pays",
  xmlattributes(p.ref_pays as id, p.nom),
  xmlagg(xmlelement(name "ville",v.nom)))
from pays p
join ville v on p.ref_pays=v.ref_pays
group by p.ref_pays, p.nom

<pays ID="1" NOM="france"><ville>lille</ville><ville>paris</ville><ville>lyon</ville><ville>bordeaux</ville><ville>marseille</ville></pays>
<pays ID="2" NOM="cameroun"><ville>douala</ville><ville>garoua</ville><ville>yaoundé</ville><ville>bafoussam</ville></pays>
<pays ID="3" NOM="maroc"><ville>casablanca</ville><ville>rabat</ville><ville>fès</ville><ville>marrakech</ville></pays> 
...

select xmlelement("pays",
  xmlattributes(ref_pays as id, p.nom),
(select xmlagg(xmlelement(name "ville", v.nom))
         from ville v
         where p.ref_pays = v.ref_pays))
from pays p

-- on met tout sous un même élément
select xmlelement("les_pays",
  xmlagg(
    xmlelement("pays",
    xmlattributes(ref_pays as id),
    xmlelement(name "nom",p.nom),
    xmlelement(name "villes",
                 (select xmlagg(xmlelement("ville",
                         xmlforest(v.nom, v.nb_habitants)) order by v.nom)
                         from ville v
                  where p.ref_pays = v.ref_pays))
                  )
        )
        )
from pays p


-------------------------
-- Manipulation de XML --
-------------------------

create table TXML of XMLType ;

insert into TXML
select xmlelement(name "pays",
  xmlattributes(p.ref_pays as id),
  xmlelement(name "nom",p.nom),
  xmlagg(xmlelement(name "ville",v.nom)))
from pays p
join ville v on p.ref_pays=v.ref_pays
group by p.ref_pays, p.nom

select extract(t.object_value,'/pays/ville') from TXML t ;
-- équivalent à
select extract(value(t),'/pays/ville') from TXML t ;

<ville>lille</ville><ville>paris</ville><ville>lyon</ville><ville>bordeaux</ville><ville>marseille</ville>
<ville>douala</ville><ville>garoua</ville><ville>yaoundé</ville><ville>bafoussam</ville>
<ville>casablanca</ville><ville>rabat</ville><ville>fès</ville><ville>marrakech</ville> 
...
13 ligne(s) sélectionnée(s).

select value(v)
from TXML t,
     table(xmlsequence(extract(value(t),'pays/ville'))) v
     
VALUE(V)
<ville>lille</ville>
<ville>paris</ville>
<ville>lyon</ville>
<ville>bordeaux</ville>
...
60 lignes
