package          PAQ_STOCKAGE as

  function identif_hangar(ligne hangar.lig%type, colonne hangar.col%type) return NUMBER ;

  procedure affecter_container(le_container container.id_container%type) ;

  PAS_HANGAR_DISPO Exception ;
  PRAGMA exception_init(PAS_HANGAR_DISPO,-20001);

  CONTAINER_INCONNU Exception ;
  PRAGMA exception_init(CONTAINER_INCONNU,-20002);

  CONTAINER_VIDE Exception ;
  PRAGMA exception_init(CONTAINER_VIDE,-20003);
  
end PAQ_STOCKAGE ;
