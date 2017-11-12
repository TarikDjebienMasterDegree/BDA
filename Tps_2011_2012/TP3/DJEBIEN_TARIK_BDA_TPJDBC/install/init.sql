CREATE TABLE compte (
id_compte VARCHAR(20) constraint compte_pkey primary key,
solde NUMBER default 0.0 not null,
constraint solde_positif check (solde >= 0)
);
