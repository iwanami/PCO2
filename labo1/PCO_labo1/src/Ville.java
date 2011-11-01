/**
 * PCO2 - Laboratoire 1 - Modelisation de moniteurs en java
 * 
 * @author Numa Trezzini
 * @version 1.0
 */
 

import java.awt.Dimension;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Nom           : Ville
 * But           : Permet de representer une ville, disposant de sites ou l'on peut deposer des velos sur des bornes.
 *                 Des habitants utilisent ces velos pour se déplacer de site en site. Une camionnette effectue une
 *                 tournée afin d'equilibrer le nombre de velos a disposition sur les bornes. Il est egalement possible 
 *                 d'ajouter des velos au depot et d'en retirer d'un site. L'arret du systeme est aussi possible.
 */
public class Ville {
    
    //nombre de sites que contient la ville
    private int nb_sites;
    
    //nombre d'habitants se deplacant entre les sites
    private int nb_habitants;
    
    //nombre de bornes disponibles a chaque site
    private int nb_bornes;
    
    //tableau contenant les moniteurs modelisant des sites
    private Site[] sites;
    
    //tableau contenant les threads modelisant les habitants
    private Habitant[] habitants;
    
    //thread modelisant la camionnette
    private Camionnette camionnette;
    
    //thread de controle, permettant d'ajouter et supprimer des velos
    //permet egalement de terminer le programme
    private ControlThread controle;
    
    //temps minimum et maximum des activites pour un habitant
    private final static int temps_min_activite = 2000;
    
    private final static int temps_max_activite = 4000;
    
    //temps minimum et maximum de trajet pour un habitant
    private final static int temps_min_trajet = 1000;
    
    private final static int temps_max_trajet = 3000;
    
    //temps d'arrete de la camionnette au depot
    private final static int temps_pause_camionnette = 5000;
    
    //nombre minimum de sites disponbles dans une ville
    private final static int nb_min_sites = 2;
    
    //nombre minimum de bornes diposnibles a chaque site
    private final static int nb_min_bornes = 4;
    
    //nombre minimum d'habitants se deplacant entre les sites
    private final static int nb_min_habitants = 5;
    
    //variable permettant l'acces direct au depot
    //le site du depot a comme valeur le nombre de sites
    //etant donne que les sites accessibles aux habitants sont
    //numerotes de 0 a nb_sites-1, cette valeur permet d'isoler le depot
    //dans les boucles
    private int site_depot;
    
    /**
     * Nom           : Habitant
     * But           : Un habitant se deplace entre les differents sites d'une ville en velo. La ville disposant de
     *                 velibs, l'habitant doit prendre un velo pour partir d'un site et le deposer a son arrivee.
     *                 Si le site de depart ne dispose pas de velo libre, il doit en attendre un. Si le site d'arrivee
     *                 ne dispose pas de borne libre, il doit en attendre une.
     * Fonctionnement: Un habitant part du site_courant, y prend un velo, puis se deplace vers un autre site
     *                 (site_suivant), ou il depose son velo. Il vaque ensuite a ses occupations avant de repartir
     *                 vers un autre site.
     *                 Les durées de trajet et d'activité sont aleatoires.
     * Remarques     : les elements Swing servent au debug
     * 
     */
    private class Habitant extends Thread{
        
        //flag permettant l'arret "en douceur" du thread
        private boolean interrupted = false;
        
        //identificateur de l'habitant
        private final int id;
        //site ou se trouve couramment l'habitant
        private int site_courant;
        
        //permet a chaque habitant d'avoir une fenetre d'affichage propre
        private JFrame fenetre_console = new JFrame();
        private JScrollPane scroll_pane;
        private JTextArea console = new JTextArea(); 
         
        
        /**
         * Nom: Habitant
         * @param id numero d'identifiaction de l'habitant
         * @param site_courant site de depart de l'habitant
         */
        public Habitant(int id, int site_courant){
            super();
            this.id = id;
            this.site_courant = site_courant;
            this.scroll_pane = new JScrollPane(console);
            this.fenetre_console.setTitle("habitant "+this.id);
            this.fenetre_console.add(scroll_pane);
            this.fenetre_console.setPreferredSize(new Dimension(300, 500));
            this.fenetre_console.setVisible(true);
            this.fenetre_console.pack();
            
        }/*end Habitant*/
        
        /**
         * Nom           : run
         * But           : methode faisant boucler les actions d'un habitant:
         *                  - prendre un velo au site courant
         *                  - se deplacer jusqu'a un autre site, pris au hasard. le temps de deplacement est aleatoire
         *                  - deposer le velo au nouveau site
         *                  - faire une activite (dormir...)
         *                  - le nouveau site devient le site courant
         * Fonctionnement: Les actions specifiees ci-dessus sont effectuees en boucle tant que la tache n'est pas
         *                 interrompue par le thread de controle. 
         */
        @Override
        public void run(){
            int site_suivant;
            //on boucle tant que le thread n'est pas interrompu
            while(!interrupted){
                console.append("-------------------------------\n");
                console.append("j'arrive au site "+site_courant+"\n");
                //prendre un velo
                sites[site_courant].retirerVelo(console);
                //determination du prochain site. Cette facon de faire permet a coup sur de se deplacer vers un autre
                //site que celui courant
                site_suivant = (site_courant+random(1, nb_sites-1))%nb_sites;
                //"trajet" de temps aleatoire
                try{sleep(random(temps_min_trajet, temps_max_trajet));}
                catch(InterruptedException e){}
                console.append("j'arrive au site "+site_suivant+"\n");
                //deposer le velo au site d'arrivee
                sites[site_suivant].deposerVelo(console);
                console.append("-------------------------------\n");
                //"activite" de temps aleatoire
                try{sleep(random(temps_min_activite, temps_max_activite));}
                catch(InterruptedException e){}
                //le nouveau site devient le site courant
                site_courant = site_suivant;
            }
        }/*end run*/
        
        /**
         * Nom: interrupt
         * But: methode permettant d'arreter un thread a la fin de l'iteration courante
         */
        @Override
        public void interrupt(){
            this.interrupted = true;
        }
        
    }/*end Habitant*/
    
    /**
     * Nom           : Camionnette
     * But           : La camionnette deplace les velos de site en site afin d'eviter les penuries ou les engorgements
     *                 aux divers sites. Cette tache a donc pour but de repartir les velos entre les sites
     * Fonctionnement: La camionnette visite tour a tour chaque site et prend ou depose des velos afin que,
     *                 dans la mesure du possible, il reste toujours 2 bornes libres. Le transport de velos est
     *                 limite par la taille de la camionnette, qui ne peut emporter que 4 velos a la fois.
     *                 apres chaque tournee, la camionnette retourne au depot, depose tous les velos qu'elle contient,
     *                 fait une pause d'une duree aleatoire, et repart avec au plus 2 velos dans son coffre.
     * Remarques     : l'algorithme remplissant (ou vidant) la camionnette est detaillee dans la fonction 
     *                 passageCamionnette() de la classe Site 
     * @see: Site
     */
    private class Camionnette extends Thread{
        
        //flag permettant l'arret "en douceur" du thread
        private boolean interrupted = false;
        
        //indique le nombre de velos presents dans la camionnette
        private int nb_velos_camionnette = 0;
        
        //permet a la camionnette d'avoir sa propre fenetre d'affichage
        private JTextArea console = new JTextArea();
        private JScrollPane scroll_pane;
        private JFrame fenetre_console = new JFrame();
        
        /**
         * Nom: Camionnette
         */
        public Camionnette(){
            this.scroll_pane = new JScrollPane(console);
            this.fenetre_console.setPreferredSize(new Dimension(300, 500));
            this.fenetre_console.add(this.scroll_pane);
            this.fenetre_console.setTitle("Camionnette");
            this.fenetre_console.setVisible(true);
            this.fenetre_console.pack();
        }/*end camionnette*/
        
        
        /**
         * Nom: run
         * But           : methode faisant boucler les actions de la camionnette:
         *                  - prendre au plus deux velos au depot
         *                  - se deplacer entre tous les sites. le temps de deplacement est aleatoire
         *                  - a chaque site, prendre ou deposer des velos en fonction de l'algorithme specifie par la
         *                    methode passageCamionnette() de Site
         *                  - une fois tous les sites visites, retourner au depot
         *                  - deposer tous les velos contenus dans la camionnette
         * Fonctionnement: Les actions specifiees ci-dessus sont effectuees en boucle tant que la tache n'est pas
         *                 interrompue par le thread de controle. 
         */
        @Override
        public void run(){
            //on boucle tant que la tache n'est pas interrompue par le thread de controle
            while(!interrupted){
                //prendre au plus 2 velos du depot
                console.append("-------------------------------\n");
                console.append("je prends les velos du depot\n");
                for(int i = 0; i < Math.min(2, sites[site_depot].getNbVelos()); i++){
                    sites[site_depot].retirerVelo(console);
                    nb_velos_camionnette++;
                }
                //deplacement vers le premier site
                try{sleep(random(temps_min_trajet, temps_max_trajet));}
                catch(InterruptedException e){}
                //faire la tournee des sites
                for(int i = 0; i < nb_sites; i++){
                    console.append("-------------------------------\n");
                    console.append("j'arrive au site "+i+"\n");
                    //a chaque site, deposer ou prendre des velos
                    nb_velos_camionnette += sites[i].passageCamionnette(nb_velos_camionnette);
                    console.append("j'ai "+nb_velos_camionnette+" velos\n");
                    console.append("-------------------------------\n");
                    //trajet vers le site suivant
                    try{sleep(random(temps_min_trajet, temps_max_trajet));}
                    catch(InterruptedException e){}
                }
                //une fois la tournee finie, deposer tous les velos au depot
                for(int i = 0; i < nb_velos_camionnette; i++){
                    sites[site_depot].deposerVelo(console);
                }
                nb_velos_camionnette = 0;
                //faire une pause avant de repartir pour une tournee
                console.append("PAUSE!!\n");
                console.append("-------------------------------\n");
                try{sleep(temps_pause_camionnette);}
                catch(InterruptedException e){}
            }
        }/*end run*/
        
        /**
         * Nom: interrupt
         * But: methode permettant d'arreter un thread a la fin de l'iteration courante
         */
        @Override
        public void interrupt(){
            this.interrupted = true;
        }
        
    }/*end camionnette*/
    
    
    /**
     * Nom           : ControlThread
     * But           : Cette tache a pour objectif un des 2 points suivants:
     *                 - modifier la quantite de velos
     *                 - arreter en douceur les threads pour terminer le programme
     * Fonctionnement: En premier lieu, cette tache demande de choisir entre la modification du nombre de velos
     *                 et l'arret du programme (sairir 1 ou 2 respectivement).
     *                 la modification du nombre de velos est detaille dans la fonction modifierVelos() ci-dessous.
     *                 l'arret du programme appelle sur chaque habitant, la camionnette et la tache de controle
     *                 elle-meme la methode interrupt(), mettant le flag d'interruption a true. Les boucles sont ainsi
     *                 quittees, et les threads se terminent.
     * Remaques      : Cette tache ne dispose pas d'une fenetre d'affichage propre comme la camionnette ou les habitants
     *                 la saisie se fera donc dans la console java.
     */
    private class ControlThread extends Thread{
        
        //flag permettant l'arret "en douceur" du thread
        private boolean interrupted = false;
        
        /**
         * Nom: run
         * But: effectue les demandes specifiees ci-dessus selon l'intervention de l'utilisateur
         */
        @Override
        public void run(){
            int action;
            while(!interrupted){
                System.out.print("Voulez-vous modifier le nombre de velos ou quitter le programme?\n"+
                                 "entrez '1' pour modifier le nombre de velos et '2' pour quitter: ");
                action = saisirInt(1, 2);
                //si l'utilisateur saisit 1, le nombre de velos est modifie.
                if(action == 1){
                    modifierVelos();
                }
                //si l'utilisateur saisit 2, les threads sont interrompus.
                else if(action == 2){
                    System.out.println("Les threads vont etre arretes et le programme terminé...");
                    //chaque habitant est interrompu
                    for(int i = 0; i < nb_habitants; i++){
                        habitants[i].interrupt();
                    }
                    //la camionnette est interrompue
                    camionnette.interrupt();
                    //la tache de controle est interrompue
                    this.interrupt();
                }
                else{
                    System.out.println("Commande non reconnue...");
                }
            }
            System.out.print("fin du programme...");
            System.exit(0);
        }/*end run*/

        /**
         * Nom           : modifierVelos
         * But           : demande a l'utilisateur s'il veut ajouter ou supprimer un velo
         * Fonctionnement: Si l'utilisateur choisit d'ajouter un velo, celui-ci sera ajoute au depot. S'il choisit d'en
         *                 supprimer un, il peut choisir de quel site le velo sera enleve.
         * Remarques     : lorsqu'un velo est ajoute, si le nombre de bornes disponibles est nul, aucun velo ne sera
         *                 ajoute. Lorsqu'un velo est supprime et que le site n'en dispose plus, l'action sera ignoree.
         *                 Les velos ne peuvent pas etre enleves du depot.
         */
        private void modifierVelos(){
            
            System.out.print("le nombre de velos va etre modifie.\n"+
                             "entrez '1' pour en ajouter et '2' pour en retirer: ");
            int action = saisirInt(1, 2);
            
            //on ajoute un velo au depot
            if(action == 1){
                System.out.println("ajout d'un velo au depot");
                sites[site_depot].ajouterVelo();
                System.out.println("velo ajoute");
            }
            //on retire un velo d'un site choisi par l'utilisateur
            else if(action == 2){
                int num_site;
                System.out.print("Un velo va etre volé.\n"+
                                 "Entrez un numero de site pour voler un velo (entre 0 et " + nb_sites +")");
                //les velos ne peuvent pas etre enleves du depot
                num_site = saisirInt(0, nb_sites-1);
                //pas besoin de notifier les habitants en attente d'une borne, le moniteur le fait lui-meme
                sites[num_site].volerVelo();
                System.out.println("velo vole!");
            }
            else{
                System.out.println("Mauvaise commande!");
            }
        }/*end modifierVelos*/
        
        /**
         * Nom: interrupt
         * But: methode permettant d'arreter un thread a la fin de l'iteration courante
         */
        @Override
        public void interrupt(){
            this.interrupted = true;
        }
    }/*end ControlThread*/
    
    /**
     * Nom: saisirInt
     * But: lit un int, verifie qu'il ne soit pas trop petit et le renvoie
     * @param min: valeur minimum de la saisie
     * @return int: la valeur saise
     */
    private int saisirInt(int min){
        Scanner input = new Scanner(System.in);
        int saisie;
        do{
            saisie = input.nextInt();
        }while(saisie < min);
        return saisie;
    }/*end saisirInt*/
    
    /**
     * Nom: saisirInt
     * But: lit un int, verifie qu'il soit bien dans les bornes et le renvoie
     * @param min: valeur minimum de la saisie
     * @param max: valeur maximum de la saisie
     * @return int: la valeur saise
     */
    private int saisirInt(int min, int max){
        Scanner input = new Scanner(System.in);
        int saisie;
        do{
            saisie = input.nextInt();
        }while(saisie < min && saisie > max);
        return saisie;
    }/*end saisirInt*/
    
    
    /**
     * Nom           : init
     * But           : Permet d'initialiser les valeurs des differentes limites de la ville:
     *                 - le nombre de sites d'ou prendre et deposer des velos
     *                 - le nombre de bornes disponibles a chaque site
     *                 - le nombre de velos disponibles initialement dans la ville
     *                 - le nombre d'habitants parcourant la ville de site en site
     *                 Les moniteurs (sites) et les taches sont ensuite crees et demarres
     * Fonctionnement: La methode demande de saisir les informations specifiees ci-dessus, avec les limites donnees
     *                 Les sites sont ensuite initialises (depot compris). Le nombre de velos a chaque site est, a la
     *                 creation, egal au nombre de bornes moins 2. Le depot recupere le surplus (cependant, pas plus
     *                 de nombre_bornes velos). Pour finir, les taches habitants sont creees et lancees, ainsi que la
     *                 tache de la camionnette et la tache de controle.
     * Remarques     : le depot ne peut pas accueillir plus de nombre_borne velos par choix d'implementation 
     */
    private void init(){
        
        
        int nb_velos_min;
        int nb_velos;
        
        //lecture du nombre de sites
        System.out.print("entrez le nombre de sites (doit etre au moins " + Ville.nb_min_sites + "): ");
        this.nb_sites = saisirInt(Ville.nb_min_sites);
        //mise en place de la valeur d'acces au depot
        this.site_depot = this.nb_sites;

        
        //lecture du nombre de bornes a chaque site
        System.out.print("entrez le nombre de bornes (doit etre au moins " + Ville.nb_min_bornes + "): ");
        this.nb_bornes = saisirInt(Ville.nb_min_bornes);
        
        //lecture du nombre de velos
        nb_velos_min = nb_sites*(nb_bornes-2)+3;
        System.out.print("entrez le nombre de velos (doit etre au moins " + nb_velos_min + "): ");
        nb_velos = saisirInt(nb_velos_min);
        
        //lecture du nombre d'habitants
        System.out.print("entrez le nombre d'habitants (doit etre au moins " + Ville.nb_min_habitants + "): ");
        this.nb_habitants = saisirInt(Ville.nb_min_habitants);
        
        //initialisation du tableau de sites
        this.sites = new Site[this.nb_sites+1];
        for(int i = 0; i < this.nb_sites+1; i++){
            this.sites[i] = new Site(i, this.nb_bornes);
        }
        
        //le depot recupere le surplus de velos
        this.sites[this.site_depot].setNbVelos(nb_velos-((nb_bornes-2)*nb_sites));
        
        //initialisation du tableau d'habitants
        //et demarrage des taches
        this.habitants = new Habitant[this.nb_habitants];
        for(int i = 0; i < this.nb_habitants; i++){
            this.habitants[i] = new Habitant(i, random(1, this.nb_sites));
            this.habitants[i].start();
        }
        
        //creation de la camionnette
        this.camionnette = new Camionnette();
        this.camionnette.start();
        
        //creation du thread de controle
        this.controle = new ControlThread();
        this.controle.start();
        
        
    }/*end init*/
    
    /**
     * Nom: Ville
     * But: cree une instance d'une ville
     */
    public Ville(){
        init();
    }/*end Labo1*/    
    
    /**
     * Nom: main
     * but: methode principale du labo 
     */
    public static void main(String[] args) {
        Ville lab = new Ville();
    }/*end main*/
    
    
    /**
     * Nom: random
     * @param min valeur minimum du generateur
     * @param max valeur maximum du generateur
     * @return int: un nombre aleatoire entre min et max
     */
    public int random(int min, int max){
        return (int)(Math.random() * (max-min)) + min;
    }/*end random*/
}
