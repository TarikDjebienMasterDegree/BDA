-- la réponse aux questions 1 et 2 était donnée dans quiz_obj.sql

-- réponses aux requêtes
-- les questionnaires et leurs titres
select id_quiz,titre
from quiz2 ;

-- idem avec le nombre de questions
select q.id_quiz,q.titre,(select count(*) from table(q.les_questions)) as nb_questions
from quiz2 q;

-- le resultat du quiz 10 si on a une majorité de carré
select q.les_resultats.carre
from quiz2 q
where q.id_quiz = 10;

-- les questions du quiz 10
select quest.ref_question.numero, quest.ref_question.enonce
from the(select q.les_questions from quiz2 q where q.id_quiz=10) quest;

-- ou bien
select quest.numero, quest.enonce
from question2 quest
where quest.le_quiz.id_quiz=10;

-- les réponses possibles pour tous les quiz
select q.id_quiz, quest.ref_question.numero, prop.ref_proposition.num_prop, prop.ref_proposition.texte_prop
from quiz2 q,
    table(q.les_questions) quest,
    table(quest.ref_question.les_propositions) prop
    
-- ou bien
select p.la_question.le_quiz.id_quiz, p.la_question.numero, p.num_prop, p.texte_prop
from proposition2 p

-- ou bien
select quest.le_quiz.id_quiz, quest.numero, prop.ref_proposition.num_prop, prop.ref_proposition.texte_prop
from question2 quest,
     table(quest.les_propositions) prop
     
-- les réponses possibles pour le quiz 10 question 1
select p.num_prop, p.texte_prop
from proposition2 p
where p.la_question.le_quiz.id_quiz=10 and p.la_question.numero=1;

-- ou bien
select prop.ref_proposition.num_prop, prop.ref_proposition.texte_prop
from the(select quest.ref_question.les_propositions 
         from the(select q.les_questions from quiz2 q where q.id_quiz=10) quest 
         where quest.ref_question.numero=1) prop
         
         
-- question 4
-- plans d'exécution pour connaître toutes les réponses de la personne d'identifiant 10, au questionnaire d'identifiant 10

-- en relationnel
select question.enonce, proposition.texte_prop
from reponse
join question on reponse.numero = question.numero and reponse.id_quiz = question.id_quiz
join proposition on proposition.num_prop = reponse.num_prop and proposition.numero = reponse.numero and proposition.id_quiz = reponse.id_quiz
where reponse.id_pers=10 and reponse.id_quiz=10

SELECT STATEMENT	ALL_ROWS	4	1	81			
  NESTED LOOPS		4	1	81			
    NESTED LOOPS		3	1	54			
      TABLE ACCESS(BY INDEX ROWID) CARON.REPONSE	ANALYZED	2	1	12			
        INDEX(RANGE SCAN) CARON.REPONSE_PKEY	ANALYZED	1	1				
      TABLE ACCESS(BY INDEX ROWID) CARON.QUESTION	ANALYZED	1	1	42			
        INDEX(UNIQUE SCAN) CARON.QUESTION_PKEY	ANALYZED	0	1				
    TABLE ACCESS(BY INDEX ROWID) CARON.PROPOSITION	ANALYZED	1	1	27			
      INDEX(UNIQUE SCAN) CARON.PROPOSITION_PKEY	ANALYZED	0	1				

-- en rel-objet
select 
tlr.ref_reponse.la_proposition.la_question.enonce,
tlr.ref_reponse.la_proposition.texte_prop
from the(select p.les_reponses from personne2 p where p.id_pers=10) tlr
where tlr.ref_reponse.la_proposition.la_question.le_quiz.id_quiz=10;

SELECT STATEMENT	ALL_ROWS	2	1	37			
  TABLE ACCESS(FULL) CARON.TAB_LES_REPONSES	ANALYZED	2	1	37			
    TABLE ACCESS(BY INDEX ROWID) CARON.PERSONNE2	ANALYZED	0	1	23			
      INDEX(UNIQUE SCAN) CARON.PERSONNE2_PKEY	ANALYZED	0	1		

-- question 5
create or replace package PAQ_QUIZ as
  DONNEE_INCONNUE Exception ;
  pragma exception_init(DONNEE_INCONNUE,-20100);
end PAQ_QUIZ ;

create or replace procedure repondre_question(la_personne personne2.id_pers%type,
                                 le_quiz quiz2.id_quiz%type, 
                                 le_numero question2.numero%type,
                                 la_reponse proposition2.num_prop%type) is
  la_ref_prop REF proposition_type;
  la_ref_pers REF personne_type ;
  la_ref_reponse REF reponse_type;
begin
  begin
    dbms_output.put_line('recherche de la proposition');
    select ref(p) into la_ref_prop
    from proposition2 p
    where p.num_prop = la_reponse
    and p.la_question.numero = le_numero
    and p.la_question.le_quiz.id_quiz = le_quiz ;
    
    dbms_output.put_line('recherche de la personne');
    select ref(pers) into la_ref_pers
    from personne2 pers
    where pers.id_pers = la_personne ;
  
  exception
    when no_data_found then raise paq_quiz.donnee_inconnue ;
  end ;
  
  dbms_output.put_line('recherche de la reponse');
  select ref(rep) into la_ref_reponse
  from reponse2 rep
  where rep.la_personne = la_ref_pers
    and rep.la_proposition.la_question.numero = le_numero
    and rep.la_proposition.la_question.le_quiz.id_quiz = le_quiz; -- si on ne connait pas deref
    
  dbms_output.put_line('la reponse existe');
  update reponse2 rep
  set la_proposition = la_ref_prop
  where rep.la_proposition.la_question = deref(la_ref_prop).la_question;
  -- pas besoin de modifier la table imbriquée les_reponses dans personne2
  
exception
  when no_data_found then 
    insert into reponse2 rep
    values (la_ref_pers, la_ref_prop) 
    returning ref(rep) into la_ref_reponse ; 
    
    insert into the(select pers.les_reponses from personne2 pers where pers.id_pers = la_personne)
    values (la_ref_reponse);
    
end ;
