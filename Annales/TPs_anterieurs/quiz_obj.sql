-- en relationnel-objet
create type quiz_type ;
/
create type reponse_type;
/
create type ref_reponse_type as object(
  ref_reponse REF reponse_type
);
/
create type ens_reponses_type as table of ref_reponse_type ;
/
create type proposition_type ;
/
create type ref_proposition_type as object(
  ref_proposition REF proposition_type 
);
/
create type ens_propositions_type as table of ref_proposition_type;
/
create type question_type as object(
  le_quiz REF quiz_type,
  numero NUMBER(2),
  enonce VARCHAR2(100),
  les_propositions ens_propositions_type
);
/
create type ref_question_type as object(
  ref_question REF question_type 
);
/
create or replace type ens_questions_type as table of ref_question_type;
/
create type proposition_type as object(
  la_question REF question_type,
  num_prop NUMBER(1),
  texte_prop VARCHAR2(100),
  symbole_prop VARCHAR2(8)
  -- on ne gere pas de collection avec toutes les reponses contenant cette proposition
);
/
create type symboles_type as object(
  carre VARCHAR2(500),
  triangle VARCHAR2(500),
  rond VARCHAR2(500),
  etoile VARCHAR2(500)
);
/
create or replace type quiz_type as object(
  id_quiz NUMBER(3),
  titre VARCHAR2(50),
  date_parution DATE,
  les_resultats symboles_type,
  les_questions ens_questions_type
);
/
create type personne_type as object(
  id_pers NUMBER(4),
  nom VARCHAR2(30),
  prenom VARCHAR2(30),
  email VARCHAR2(50),
  les_reponses ens_reponses_type
);
/
create type reponse_type as object(
  la_personne REF personne_type,
  la_proposition REF proposition_type
);
/

-- on crée les tables :
create table Quiz2 of quiz_type(
  constraint quiz2_pkey primary key(id_quiz),
  constraint titre_not_null titre not null,
  constraint defaut_les_question les_questions default ens_questions_type()
)nested table les_questions store as tab_les_questions ;


create table question2 of question_type(
  -- on ne peut pas definir de cle primaire
  constraint defaut_les_propositions les_propositions default ens_propositions_type(),
  constraint question2_quiz2_fkey foreign key(le_quiz) references quiz2
)nested table les_propositions store as tab_les_propositions ;

create table proposition2 of proposition_type(
  constraint proposition2_question2_fkey foreign key(la_question) references question2
);

alter table proposition2 add constraint prop2_enum check (symbole_prop in ('carre', 'rond', 'triangle', 'etoile'));

create table personne2 of personne_type(
  constraint personne2_pkey primary key(id_pers),
  constraint prenom_nom_not_null check (nom is not null and prenom is not null),
  constraint defaut_les_reponses les_reponses default ens_reponses_type()
)nested table les_reponses store as tab_les_reponses ;

create table reponse2 of reponse_type(
  constraint reponse2_personne2_fkey foreign key(la_personne) references personne2,
  constraint reponse2_proposition2_fkey foreign key(la_proposition) references proposition2
);

-- on ajoute des données
insert into quiz2 values(10,'quel genre de casse-pieds êtes vous ?',sysdate,
symboles_type('Une tendance à l''autoritarisme','Une tendance à la victimisation','Une tendance à la prétention','Une tendance à la désinvolture'),
ens_questions_type()
);

insert into question2 values(
(select ref(q) from quiz2 q where id_quiz=10),
1,
'J''ai tendance à me ronger les ongles',
ens_propositions_type()
);

insert into question2 values(
(select ref(q) from quiz2 q where id_quiz=10),
7,
'Les critiques m''affectent particulièrement',
ens_propositions_type()
);

insert into the(select q.les_questions from quiz2 q where q.id_quiz=10)
select ref(ques) from question2 ques where ques.le_quiz.id_quiz=10;

insert into proposition2 values(
(select ref(q) from question2 q where q.le_quiz.id_quiz=10 and q.numero=1),
1,'vrai','carre');

insert into proposition2 values(
(select ref(q) from question2 q where q.le_quiz.id_quiz=10 and q.numero=1),
2,'faux','rond');

insert into the(select q.les_propositions from question2 q where q.le_quiz.id_quiz=10 and q.numero=1)
select ref(prop) from proposition2 prop where prop.la_question.le_quiz.id_quiz = 10 and prop.la_question.numero = 1;


insert into proposition2 values(
(select ref(q) from question2 q where q.le_quiz.id_quiz=10 and q.numero=7),
1,'vrai','triangle');

insert into proposition2 values(
(select ref(q) from question2 q where q.le_quiz.id_quiz=10 and q.numero=7),
2,'faux','etoile');

insert into the(select q.les_propositions from question2 q where q.le_quiz.id_quiz=10 and q.numero=7)
select ref(prop) from proposition2 prop where prop.la_question.le_quiz.id_quiz = 10 and prop.la_question.numero = 7;

insert into personne2(id_pers,nom,prenom,email)
values (10,'caron','anne-cécile','caron@hotmail.com');

insert into personne2(id_pers,nom,prenom,email)
values (20,'lebbe','jean-marie','theboss@laposte.net');

