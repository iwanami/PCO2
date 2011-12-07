--=================================================================================
-- PCO2 - Laboratoire 1 - Modelisation de moniteurs en java
-- 
-- Nom           : labo2.adb
-- But           : Modelise une ville disposant de sites mettant proposant aux
--						 habitants de prendre des velos a des bornes et se deplacer de 
--						 site a site. Afin d'equilibrer les quantites de velos 
--						 disponibles a chaque site, une equipe en camionnette fait la 
--						 tournee des sites (ainsi que du depot, indice par nb_sites).
-- Fonctionnement: Cette classe dispose des elements suivants:
--                 - saisie: procedure permettant de saisir les informations
--							necessaires au bon fonctionnement de la ville. saisie permet 
--						   ainsi de choisir le nombre de sites, le nombre de bornes a
--							chaque site, le nombre de velos mis a disposition, et le 
--							nombre d'habitants
--                 - start: procedure creant et demarrant les taches sites, 
--						   habitants et camionnette
--						 - start contient:
--							 -nom: controle
--      					  but: Tache de controle permettant de terminer les autres 
--							  taches et d'ajouter ou supprimer des velos
--					        les velos ne peuvent etre ajoutes qu'au depot
--							 -nom: Habitant
--							  but: tache modelisant le comportement d'un habitant se
--							  deplacant de site en site
--							 -nom: Site
--							  but: prototype d'une tache Site. Il s'agit des taches 
--							  acceptant les rendez-vous des habitants, de la camionnette
--      					 -nom: Camionnette
--							  but: tache modelisant le comportement d'une Camionnette se 
--							  deplacant de site en site
--							 le detail du fonctionnement de ces taches est donne a la
--							 declaration du corps, dans start
-- Auteur: Numa Trezzini
--=================================================================================
with Ada.Text_IO; use Ada.Text_IO;
with Ada.Integer_Text_IO; use Ada.Integer_Text_IO;
with Ada.Numerics.Float_Random; use Ada.Numerics.Float_Random;
procedure Labo2 is

   --bornes du programme
   --temps de trajet entre sites (secondes)
   temps_trajet_min : constant Natural := 2;
   temps_trajet_max : constant Natural := 5;
   
   --temps d'activites des habitants
   temps_activite_min  : constant Natural := 2;
   temps_activite_max  : constant Natural := 5;
   
   --temps de pause de la camionnette
   temps_pause      : constant Natural := 5;
   
   --nombre minimum de sites
   nb_sites_min     : constant Natural := 2;
   
   --nombre minimum d'habitants
   nb_habitants_min : constant Natural := 5;
   
   --nombre minimum de bornes
   nb_bornes_min    : constant Natural := 4;

   --seed du generateur random
   gen : Generator;
   
   --fonction random renvoyant un entier aleatoire entre min et max
   function getRandom(min: Integer; max: Integer) return Integer is
   begin --getRandom
      return Integer(Float(max-min)*Random(gen)) + min;
   end getRandom;

   

   --==============================================================================
   --nom: saisie
   --but: permet de donner les limites des infrastructures mises a disposition par
	--		 la ville. L'utilisateur doit ainsi choisir le nombre de sites, le nombre
	--		 de bornes disponibles a chaque site, le nombre de velos et le nombre d'
   --     habitants
   --==============================================================================
   procedure saisie(nb_sites: out Natural; nb_bornes: out Natural; 
						  nb_velos: out Natural; nb_habitant: out Natural) is
      nb_velos_min: Natural;
   begin --init
      --saisie du nombre de sites mis a disposition par la ville
      loop
         Put("entrez le nombre de sites (min ");
         Put(nb_sites_min, 2);
         Put("): ");
         Get(nb_sites);
         New_Line;
         exit when nb_sites >= nb_sites_min;
      end loop;
      
      --saisie du nombre de bornes disponibles a chaque site
      loop
         Put("entrez le nombre de bornes (min ");
         Put(nb_bornes_min, 2);
         Put("): ");
         Get(nb_bornes);
         New_Line;
         exit when nb_bornes >= nb_bornes_min;
      end loop;
      
      --saisie du nombre de velos disponibles dans la ville
      nb_velos_min := nb_sites*(nb_bornes-2)+3;
      loop
         Put("entrez le nombre de velos (min ");
         Put(nb_velos_min, 2);
         Put("): ");
         Get(nb_velos);
         New_Line;
         exit when nb_velos >= nb_velos_min;
      end loop;
      
      --saisie du nombre d'habitants se deplacant dans la ville
      loop
         Put("entrez le nombre d'habitants (min ");
         Put(nb_habitants_min, 2);
         Put("): ");
         Get(nb_habitant);
         New_Line;
         exit when nb_habitant >= nb_habitants_min;
      end loop;
      
   end saisie;

   --==============================================================================
   --nom: start
   --but: une fois les limites de taille saisies grace a la procedure saisie, nous
   --     pouvons creer les tableaux de taches. Les taches seront ainsi
   --     automatiquement lancees. De plus, comme la procedure ne peut pas se
	--		 terminer avant l'arret des taches liees, elle contiendra la tache de
	--		 controle, qui est en charge de terminer toutes les autres (habitants, 
	--		 sites et camionnette)
   --==============================================================================
   procedure start(nb_sites: Natural; nb_bornes: Natural; nb_velos: Natural;
						 nb_habitants: Natural) is
      site_depot : constant Natural := nb_sites+1;
      
      --===========================================================================
      --nom: Habitant
      --but: tache modelisant le comportement d'un habitant se deplacant de site 
		--		 en site
      --===========================================================================
      task type Habitant(id: Natural) is
         entry termine;
      end Habitant;
   
      --===========================================================================
      --nom: Ptr_Habitant
      --but: Pointeur sur une tache de type habitant. permet l'allocation dynamique
		--		 d'habitants
      --===========================================================================
      type Ptr_Habitant is access Habitant;
      
      --===========================================================================
      --nom: Tab_Habitant
      --but: Tableau d'habitants, permet d'avoir une liste d'habitants
      --===========================================================================
      type Tab_Habitant is array (Natural range <>) of Ptr_Habitant;
   

      --===========================================================================
      --nom: Site
      --but: prototype d'une tache Site. Il s'agit des taches acceptant les
		--		 rendez-vous des habitants, de la camionnette
      --===========================================================================
      task type Site(id: Natural) is
         --rendez-vous d'attente d'un velo au cas ou il n'y en a pas de disponible 
			--sur le site
         entry AttenteVelo;
         --idem que AttenteVelo, mais en cas de manque de borne
         entry AttenteBorne;
         --rendez-vous permettant le depot d'un velo sur le site
         entry deposerVelo;
         --rendez-vous permettant le retrait d'un velo sur le site
         entry retirerVelo;
         --rendez-vous permettant le passage de la camionnette sur le site
         entry passageCamionnette(nb_velos_camionnette : in out Natural);
         --rendez-vous permettant la suppression arbitraire d'un velo sur le site
         entry volerVelo;
         --rendez-vous permettant l'ajout arbitraire d'un velo sur le site
         entry ajouterVelo;
         --rendez-vous permettant a la camionnette de deposer tous ses velos 
			--au depot
         entry deposerVeloDepot(nb_velos_camionnette : in out Natural);
         --rendez-vous permettant a la camionnette de prendre plusieurs velos 
			--au depot
         entry retirerVeloDepot(nb_velos_camionnette : in out Natural);
         --rendez-vous permettant l'arret des taches
         entry termine;
      end Site;
		     
      --===========================================================================
      --nom: Ptr_Site
      --but: type acces pointant sur un Site. Permet une meilleure manipulation 
		--dynamique de sites
      --===========================================================================
      type Ptr_Site is access Site;
      
      --===========================================================================
      --nom: Tab_Sites
      --but: Tableau de sites, liste tous les sites mis a disposition par la ville
      --===========================================================================
      type Tab_Sites is array (Natural range <>) of Ptr_Site;
      
      --===========================================================================
      --nom: Camionnette
      --but: tache modelisant le comportement d'une Camionnette se deplacant de
		--     site en site
      --===========================================================================
      task type Camionnette is
         entry termine;
      end Camionnette;
      
      --===========================================================================
      --nom: Ptr_Camionnette
      --but: type acces pointant sur une camionnette. Permet une meilleure
		-- manipulation dynamique de la camionnette
      --===========================================================================
      type Ptr_Camionnette is access Camionnette;
      
      --creation des tableaux necessaires au bon fonctionnement du programme
      habitants  : Tab_Habitant(1..nb_habitants);
      sites      : Tab_Sites(1..site_depot);
      camion     : Ptr_Camionnette;
      
      
      
      --creation du thread de controle
      --===========================================================================
      --nom: controle
      --but: Tache de controle permettant de terminer les autres taches et 
		--		 d'ajouter ou supprimer des velos
      --     les velos ne peuvent etre ajoutes qu'au depot
      --fonctionnement: on commence par choisir si l'on souhaite arreter le
		--						programme ou rajouter/supprimer des velos.
      --                dans le premier cas, les taches sont terminees et le  
      --                programme s'arrete.
      --                dans le deuxieme cas, l'utilisateur peut choisir entre 
      --                ajouter ou supprimer un velo. Ajouter se fera au depot, et
      --                le site de la suppression doit etre saisi.
      --===========================================================================
      task controle;
      task body controle is
            saisie : Integer;
      begin --controle
         loop
            loop
               Put("vous pouvez arreter le programme (1) ou ");
					Put("modifier le nombre de velos (2): ");
               Get(saisie);
               put("saisie: ");
               put(saisie);
               exit when (saisie = 1 or else saisie = 2);
            end loop;
            if saisie = 1 then
               --terminer le programme
               --on commence par arreter les habitants
               for h in habitants'range loop
                  habitants(h).termine;
               end loop;
               --une fois qu'ils sont termines, on arrete la camionnette
               camion.termine;
               --on termine les sites, egalement
               for s in sites'range loop
                  sites(s).termine;
               end loop;
               --et on sort de la boucle de controle, mettant ainsi fin
					--au programme
               exit;
            elsif saisie = 2 then
               saisie := 0;
               --on choisit d'ajouter/supprimer un velo
               while saisie /= 1 or else saisie /= 2 loop
                  Put("vous pouvez ajouter un velo (1) ou en supprimer un (2): ");
                  Get(saisie);
               end loop;
               if saisie = 1 then
                  --ajout d'un velo au depot
                  sites(site_depot).ajouterVelo;
               elsif saisie = 2 then
                  --on supprime un velo
                  saisie := 0;
                  --on choisit le site sur lequel enlever le velo
                  while saisie < 1 or else saisie > nb_sites loop
                     Put("entrez un nombre pour choisir un site (1, ");
                     Put(nb_sites, 2);
                     Put("): ");
                     Get(saisie);
                  end loop;
                  sites(saisie).volerVelo;
               end if;
            end if;
            saisie := 0;
         end loop;
      end controle;
    
      --===========================================================================
      --nom: Habitant
      --but: implemente le comportement d'un habitant se délacant de site en site
      --     grace aux velos mis a disposition par la ville
      --fonctionnement: L'habitant se comporte comme suit jusqu'a interruption 
		--						explicite:
      --                - prendre un velo au site courant
      --                - se deplacer jusqu'a un autre site, pris au hasard. 
		--						  le temps de deplacement est aleatoire
      --                - deposer le velo au nouveau site
      --                - faire une activite (dormir...)
      --                - le nouveau site devient le site courant
      --===========================================================================
      task body Habitant is
         site_courant: Natural := getRandom(1, nb_sites);
         site_suivant: Natural;
      begin --Habitant
         loop
            --l'habitant prend un velo au site sur lequel il est
            Put(id, 3);
            Put("arrive au site");
            Put(site_courant, 3);
            new_line;
            sites(site_courant).retirerVelo;
            --il se rend au site suivant
            site_suivant := (site_courant+getRandom(1, nb_sites)) mod nb_sites +1;
            Put(id, 3);
            Put("va au site");
            Put(site_suivant, 3);
            new_line;
            --duree de trajet
            delay Duration(getRandom(temps_trajet_min, temps_trajet_max));
            --une fois arrive, il depose son velo
            sites(site_suivant).deposerVelo;
            --il vaque a ses occupations
            Put(id, 3);
            Put("vaque");
            Put(site_courant, 3);
            new_line;
            --activite
            delay Duration(getRandom(temps_activite_min, temps_activite_max));
            --le site d'arrivee devient le site de depart
            site_courant := site_suivant;
            --on verifie si l'habitant doit s'arreter
            select
               accept termine;
               exit;
            else
               null;
            end select;
         end loop;
      end Habitant;
		      
      --===========================================================================
      --nom: Site
      --but: cree une tache Site afin de gerer les depots et retrait de velos par 
      --     des habitants.
      --fonctionnement: on choisit parmi les options suivantes:
      --                - un habitant veut prendre un velo, s'il y en a, il en 
      --                  prend un, sinon, il se met en attente grace a un requeue
      --                  sur AttenteVelo. s'il a pris un velo, il verifie s'il y 
      --                  a de l'attente sur une borne et reveille une tache si 
		--						  besoin est
      --                - un habitant veut deposer un velo, s'il y a des bornes, 
      --                  il pose son velo, sinon, il se met en attente grace a 
      --                  un requeue sur AttenteBorne. s'il a pu poser son velo,
      --                   il reveille les taches en attente d'un velo.
      --                - la tache de controle ajoute un velo au site
      --                - la tache de controle supprime un velo au site seulement 
      --                  s'il y en a a disposition
      --                - la camionnette passe. Dans ce cas, l'algorithme bien 
      --                  connu est applique.
      --                - le compte de velos est modifie normalement si la 
      --                  camionnette passe au depot
      --                - la boucle est quittee lors de la demande de terminaison
      --===========================================================================
      task body Site is
         nb_velos: Natural := nb_bornes-2; --nombre de velos disponibles
         velos_deposes : Natural; --nombre de velos deposes par la camionnette
      begin -- Site
         loop
            select
               --on retire un velo
               accept retirerVelo do
                  Put("va retirer velo");
                  Put(id, 3);
                  new_line;
                  --il y en a, on en prend donc un
                  if nb_velos > 0 then
                     nb_velos := nb_velos-1;
                     Put("pris");
                     Put(id, 3);
                     new_line;
                  else
                     --il n'y en a pas, on attend
                     Put("y a pas");
                     Put(id, 3);
                     new_line;
                     requeue attenteVelo;
                  end if;
               end retirerVelo;
               --s'il y a des habitants en attente d'une borne, on en reveille un
               if attenteBorne'count > 0 then
                  accept attenteBorne do
                     nb_velos := nb_velos+1;
                  end attenteBorne;
               end if;
            or
               --on depose un velo
               accept deposerVelo do
                  Put("va deposer velo");
                  Put(id, 3);
                  new_line;
                  --s'il y a des bornes, on pose le velo
                  if nb_velos < nb_bornes then
                     nb_velos := nb_velos+1;
                     Put("pose");
                     Put(id, 3);
                     new_line;
                  else
                     --sinon, on attend
                     Put("pas de borne");
                     Put(id, 3);
                     new_line;
                     requeue attenteBorne;
                  end if;
               end deposerVelo;
               --si des habitants attendent un velo, on les reveille
               if attenteVelo'count > 0 then
                  accept attenteVelo do
                     nb_velos := nb_velos-1;
                  end attenteVelo;
               end if; 
            or
               --on ajoute un velo au site
               accept ajouterVelo do
                  Put("velo ajoute");
                  Put(id, 3);
                  new_line;
                  nb_velos := nb_velos+1;
               end ajouterVelo;
               --si des habitants attendent un velo, on les reveille
               if attenteVelo'count > 0 then
                  accept attenteVelo do
                     nb_velos := nb_velos-1;
                  end attenteVelo;
               end if; 
            or
               --on supprime un velo du site, sinon, on ne fait rien
               accept volerVelo do
                  if nb_velos > 0 then
                     Put("velo vole");
                     Put(id, 3);
                     new_line;
                     nb_velos := nb_velos-1;
                     --s'il y a des habitants en attente d'une borne, 
							--on en reveille un
                     if attenteBorne'count > 0 then
                        accept attenteBorne do
                        nb_velos := nb_velos+1;
                        end attenteBorne;
                     end if;
                  else
                     Put("pas de velo a voler");
                     New_Line;
                  end if;
               end volerVelo;      
            or 
               accept passageCamionnette(nb_velos_camionnette: in out Natural) do
                  --s'il y a plus de deux bornes libres, on ajoute des velos
                  if nb_velos < nb_bornes-2 then
                     velos_deposes := Integer'min((nb_bornes-2)-nb_velos, 
												  nb_velos_camionnette);
                     nb_velos := nb_velos + velos_deposes;
                     nb_velos_camionnette := nb_velos_camionnette - velos_deposes;
                  --s'il y moins de deux bornes libres, on enleve des velos
                  elsif nb_velos > nb_bornes-2 then
                     velos_deposes := Integer'min(nb_velos-(nb_bornes-2), 
													4-nb_velos_camionnette);
                     nb_velos := nb_velos - velos_deposes;
                     nb_velos_camionnette := nb_velos_camionnette + velos_deposes;
                  end if;
                  Put("camion!");
                  Put(id, 3);
                  new_line;
                  nb_velos := nb_velos-1;
               end passageCamionnette;
            or
               --tous les velos sont deposes au depot
               accept deposerVeloDepot(nb_velos_camionnette: in out Natural) do
                  nb_velos := nb_velos + nb_velos_camionnette;
                  nb_velos_camionnette := 0;
               end deposerVeloDepot;
            or
               --on prend au maximum 4 velos du depot
               accept retirerVeloDepot(nb_velos_camionnette: in out Natural) do
                  nb_velos_camionnette := Natural'Min(nb_velos, 4);
                  nb_velos := nb_velos-nb_velos_camionnette;
               end retirerVeloDepot;
            or
               --on termine la boucle, terminant ainsi la tache
               accept termine;
               exit;
            end select;
               
         end loop;
      end Site;
   
      --===========================================================================
      --nom: Camionnette
      --but: implemente le comportement d'une camionnette se deplacant de site en
      --     site afin de gerer les quantites de velos mis a disposition
      --fonctionnement: La camionnette se comporte comme suit jusqu'a interruption
		--						explicite:
      --                - prendre des velos au depot
      --                - faire la tournee des sites afin d'equilibrer les  
		--						  quantites de velos
      --                - deposer le velo au depot
      --                - faire une pause (dormir...)
      --===========================================================================
      task body Camionnette is
         nb_velos : Natural := 0;
      begin --Camionnette
         loop
            --depart du depot
            sites(site_depot).retirerVeloDepot(nb_velos);
            --parcours des sites
            for i in 1..site_depot-1 loop
               sites(i).passageCamionnette(nb_velos);
               --trajet jusqu'au prochain site
               delay Duration(getRandom(temps_trajet_min, temps_trajet_max));
            end loop;
            --depot des velos au depot
            sites(site_depot).deposerVeloDepot(nb_velos);
            --temps de pause
            delay Duration(temps_pause);
            --on verifie si on doit s'arreter
            select
               accept termine;
               exit;
            else
               null;
            end select;
         end loop;
      end Camionnette;

   begin --start
      --initialisation des sites
      for s in sites'range loop
         sites(s) := new Site(s);
      end loop;
      --initialisation des habitants
      for h in habitants'range loop
         habitants(h) := new Habitant(h);
      end loop;
      --initialisation de la camionnette
      camion := new Camionnette;
   end start;
   
   nb_sites    : Natural;
   nb_bornes   : Natural;
   nb_velos    : Natural;
   nb_habitants: Natural;
   
begin --Labo2
   Reset(gen);
   saisie(nb_sites, nb_bornes, nb_velos, nb_habitants);
   start(nb_sites, nb_bornes, nb_velos, nb_habitants);
end Labo2;