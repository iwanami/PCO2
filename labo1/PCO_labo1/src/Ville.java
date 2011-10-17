/**
 * 
 * @author Numa Trezzini
 * @version 1.0
 */
 

import java.util.Scanner;

/**
 * Nom: Ville
 * But: Permet de representer une ville, disposant de sites ou l'on peut deposer des velos sur des bornes.
 *      Des habitants utilisent ces velos pour se déplacer de site en site. Une camionnette effectue une tournée
 *      afin d'equilibrer le nombre de velos a disposition sur les bornes. Il est egalement possible d'ajouter des
 *      velos au depot et d'en retirer d'un site. L'arret du systeme est aussi possible.
 * Fonctionnement: 
 * 
 */
public class Ville {
    
    private int nb_sites;
    
    private int nb_habitants;
    
    private int nb_bornes;
    
    private Site[] sites;
    
    private Habitant[] habitants;
    
    private Camionnette camionnette;
    
    private ControlThread controle;
    
    private final static int temps_min = 3000;
    
    private final static int temps_max = 10000;
    
    private final static int temps_pause_camionnette = 5000;
    
    private final static int nb_min_sites = 2;
    
    private final static int nb_min_bornes = 4;
    
    private final static int nb_min_habitants = 1;
    
    private final static int site_depot = 0;
    
    /**
     * Nom: Habitant
     */
    private class Habitant extends Thread{
        
        private int site_courant;
        
        /**
         * Nom: Habitant
         * @param site_courant 
         */
        public Habitant(int site_courant){
            super();
            this.site_courant = site_courant;
        }/*end Habitant*/
        
        @Override
        public void run(){
            int site_suivant;
            while(!this.isInterrupted()){
                sites[site_courant].retirerVelo();
                site_suivant = random(1, nb_sites);
                sites[site_suivant].deposerVelo();
                try{sleep(random(temps_min, temps_max));}
                catch(InterruptedException e){}
                site_courant = site_suivant;
            }
        }/*end run*/
    }/*end Habitant*/
    
    /**
     * Nom: Camionnette
     */
    private class Camionnette extends Thread{
        
        private final int site_depot;
        
        private int nb_velos_camionnette = 0;
        
        private int temps_pause_camionnette;
        
        /**
         * Nom: Camionnette
         * @param site_depot
         * @param temps_pause 
         */
        public Camionnette(int site_depot, int temps_pause){
            this.site_depot = site_depot;
            this.temps_pause_camionnette = temps_pause;
        }/*end camionnette*/
        
        
        /**
         * Nom: run
         */
        @Override
        public void run(){
            while(!this.isInterrupted()){
                //prendre les velos du depot
                for(int i = 0; i < Math.min(2, sites[site_depot].getNbVelos()); i++){
                    sites[site_depot].retirerVelo();
                    nb_velos_camionnette++;
                }
                //faire la tournee des sites
                for(int i = 1; i < nb_sites; i++){
                    sites[i].passageCamionnette(nb_velos_camionnette);
                    //TODO recuperer le nombre de velos dans la camionnette
                }
                //deposer les velos au depot
                for(int i = 0; i < nb_velos_camionnette; i++){
                    sites[site_depot].deposerVelo();
                }
                nb_velos_camionnette = 0;
                //faire une pause avant de repartir pour une tournee
                try{sleep(temps_pause_camionnette);}
                catch(InterruptedException e){}
            }
        }/*end run*/
    }/*end camionnette*/
    
    
    /**
     * Nom: ControlThread
     */
    private class ControlThread extends Thread{
        Scanner input = new Scanner(System.in);
        
        /**
         * Nom: run
         */
        @Override
        public void run(){
            int action;
            int action_velos;
            while(!this.isInterrupted()){
                System.out.print("Voulez-vous modifier le nombre de velos ou quitter le programme?\n"+
                                 "entrez '1' pour modifier le nombre de velos et '2' pour quitter: ");
                action = saisirInt(1, 2);

                if(action == 1){
                    System.out.print("le nombre de velos va etre modifie.\n"+
                                     "entrez '1' pour en ajouter et '2' pour en retirer: ");
                    action_velos = saisirInt(1, 2);

                    modifierVelos(action_velos);

                }
                else if(action == 2){
                    System.out.println("Les threads vont etre arretes et le programme terminé...");
                    for(int i = 0; i < nb_habitants; i++){
                        habitants[i].interrupt();
                    }
                    camionnette.interrupt();
                    this.interrupt();
                }
                else{
                    System.out.println("Commande non reconnue...");
                }
            }
        }/*end run*/

        /**
         * Nom: modifierVelos
         * @param action 
         */
        private void modifierVelos(int action){
            //on ajoute des velos
            if(action == 1){
                System.out.println("ajout d'un velo au depot");
                sites[site_depot].ajouterVelo();
                System.out.println("velo ajoute");
            }
            //on retire des velos
            else if(action == 2){
                int num_site;
                System.out.print("Un velo va etre volé.\n"+
                                 "Entrez un numero de site pour voler un velo (entre 1 et " + nb_sites +")");
                num_site = saisirInt(1, nb_sites);
                sites[num_site].volerVelo();
                System.out.println("velo vole!");
            }
            else{
                System.out.println("Mauvaise commande!");
            }
        }/*end modifierVelos*/
    }/*end ControlThread*/
    
    /**
     * Nom: saisirInt
     * @param min
     * @return 
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
     * @param min
     * @param max
     * @return 
     */
    private int saisirInt(int min, int max){
        Scanner input = new Scanner(System.in);
        int saisie;
        do{
            //TODO read line
            saisie = input.nextInt();
        }while(saisie < min && saisie > max);
        return saisie;
    }/*end saisirInt*/
    
    
    /**
     * Nom: init
     */
    private void init(){
        
        
        int nb_velos_min;
        int nb_velos;
        
        //lecture du nombre de sites
        System.out.print("entrez le nombre de sites (doit etre au moins " + Ville.nb_min_sites + "): ");
        this.nb_sites = saisirInt(Ville.nb_min_sites);
        //ajout du depot
        this.nb_sites++;
        
        //lecture du nombre de bornes a chaque site
        System.out.print("entrez le nombre de bornes (doit etre au moins " + Ville.nb_min_bornes + "): ");
        this.nb_bornes = saisirInt(Ville.nb_min_bornes);
        
        //lecture du nombre de velos
        //le depot n'est pas pris en compte dans le calcul
        nb_velos_min = (nb_sites-1)*(nb_bornes-2)+3;
        System.out.print("entrez le nombre de velos (doit etre au moins " + nb_velos_min + "): ");
        nb_velos = saisirInt(nb_velos_min);
        
        //lecture du nombre d'habitants
        System.out.print("entrez le nombre d'habitants (doit etre au moins " + Ville.nb_min_habitants + "): ");
        this.nb_habitants = saisirInt(Ville.nb_min_habitants);
        
        //initialisation du tableau de sites
        this.sites = new Site[this.nb_sites];
        for(int i = 0; i < this.nb_sites; i++){
            this.sites[i] = new Site(i, this.nb_bornes);
        }
        
        //modification du site designe comme depot
        this.sites[0].setNbVelos(nb_velos-((nb_bornes-2)*nb_sites));
        
        //initialisation du tableau d'habitants
        //et demarrage des taches
        this.habitants = new Habitant[this.nb_habitants];
        for(int i = 0; i < this.nb_habitants; i++){
            this.habitants[i] = new Habitant(random(1, this.nb_sites));
            this.habitants[i].start();
        }
        
        //creation de la camionnette
        this.camionnette = new Camionnette(site_depot, temps_pause_camionnette);
        this.camionnette.start();
        
        //creation du thread de controle
        this.controle = new ControlThread();
        this.controle.start();
        
        
    }/*end init*/
    
    /**
     * Nom: Labo1
     */
    public Ville(){
        init();
    }/*end Labo1*/
    
    public void interruptExpecter(){
        for(int i = 0; i < nb_habitants; i++){
            try{this.habitants[i].join();}
            catch(InterruptedException e){}
        }
        try{
            this.camionnette.join();
            this.controle.join();
        }
        catch(InterruptedException e){}
        
    }/*end interruptExpecter*/
    
    
    /**
     * Nom: main
     * @param args 
     */
    public static void main(String[] args) {
        Ville lab = new Ville();
        lab.interruptExpecter();
        System.out.print("fin du programme...");
    }/*end main*/
    
    
    /**
     * Nom: random
     * @param min
     * @param max
     * @return 
     */
    public int random(int min, int max){
        return (int)(Math.random() * (max-min)) + min;
    }/*end random*/
}
