-- author : Rakotobe eric and Djebien tarik
PACKAGE BODY PAQ_STOCKAGE AS 

  -- donne une clé de hashage a un hangar
  function identif_hangar(ligne hangar.lig%type, colonne hangar.col%type) return NUMBER is
  begin
    return (10 * ligne + colonne ) ; 
  end ; 
  
  -- affecte un hangar a un container 
  procedure affecter_container(le_container container.id_container%type) is 
    id_container container.id_container%type ;
    volume_du_container container.volume%type ;
    lig_container container.volume%type ;
    col_container container.volume%type ;
    produit_du_container container.id_produit%type  ;
    categorie_produit_contenaire produit.CATEGORIE%type ; 
    un_hangar_adapte_lig hangar.lig%type ; 
    un_hangar_adapte_col hangar.col%type ; 
    hash_cle NUMBER ; 
    new_col_by_hash hangar.col%type ; 
    new_lig_by_hash hangar.lig%type ; 
  begin

    --selection des informations concernant le conteneur 
    begin 
        select id_container, volume , lig , col , id_produit 
        into id_container, volume_du_container, lig_container, col_container , produit_du_container 
        from container 
        where id_container = le_container ; 
    --si pas de conteneur retourne par notre select
    exception when No_data_found then 
        raise CONTAINER_INCONNU;
    end ; 
  
    --si le container est vide, il n'a pas de produit, donc pas de reference vers un produit
    if (produit_du_container is null) then
        raise CONTAINER_VIDE;
    end if ; 
    
    --si le container est deja dans un hangar, on met lig et col a null
    if ( (lig_container is not null) and (col_container is not null) ) then
        update container 
        set container.COL = null, container.LIG = null 
        where id_container = le_container ; 
    end if ; 


    
    -- On recherche maintenant un hangar qui contient deja un produit de cette categorie 
    -- et qui a assez de place pour accueillir notre conteneur
    
    --selection de la categorie du produit du conteneur 
    select categorie
    into categorie_produit_contenaire
    from produit p , container c 
    where p.id_produit = c.id_produit
    and c.id_container = le_container ;
    
    select max(identif_hangar(lig, col)) 
    into hash_cle
    from hangar_avec_categorie
    where categorie_produit_contenaire = categorie
    and capacite <= volume_reel + volume_du_container ; --capacite du hangar < volume du hangar + volume du conteneur
    
    --si on a trouve un hangar adapte ie lig et col ne sont pas nuls, on met a jour la table conteneur en faisant
    --reference au hangar, le select aurait pu renvoyer plusieurs lignes
    -- il faudra donc en choisir un seul 
    if ( hash_cle is not null ) then
        new_col_by_hash := (hash_cle mod 10) ; 
        new_lig_by_hash := ( ( hash_cle - (hash_cle mod 10) ) / 10 ) ;
        update container 
        set container.COL = new_col_by_hash , container.LIG = new_lig_by_hash
        where id_container = le_container ; 
    else 
        --on recherche un hangar vide en utilisant la vue 
        select max(identif_hangar(lig, col))  
        into hash_cle
        from hangar_vide ; 
        new_col_by_hash := (hash_cle mod 10) ; 
        new_lig_by_hash := ( ( hash_cle - (hash_cle mod 10) ) / 10 ) ;
        -- puis on insere le conteneur dans notre hangar
        update container 
        set container.COL = new_col_by_hash, container.LIG = new_lig_by_hash 
        where id_container = le_container ; 
    end if ;

  end; 
