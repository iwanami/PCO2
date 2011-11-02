/**
 * PCO2 - Laboratoire 1 - Modelisation de moniteurs en java
 * Nom: Site
 * But: Cette classe implemente un moniteur permettant de modeliser un site d'une ville, mettant a 
 *      disposition des velos a ses habitants
 * Fonctionnement: Un site est moniteur fonctionnant sur le principe du producteur/consommateur.
 *                 Il dispose ainsi de:
 *                 - mutex: un mutex permettant de verouiller l'acces au moniteur
 *                 - borne_mutex: un "mutex" sur les bornes, servant de condition a l'attente d'une borne
 *                 - velo_mutex: un "mutex" sur les velos, servant de condition a l'attente d'un velo
 *                 - nb_attente_borne: un compteur d'attente sur une borne, permettant de savoir s'il convient de 
 *                   transmettre le controle du moniteur
 *                 - nb_attente_velo: un compteur d'attente sur un velo, permettant de savoir s'il convient de
 *                   transmettre le controle du moniteur
 *                 - nb_velos: un compteur de velo, permettant de savoir si le "buffer" du moniteur est plein ou vide
 *                 - nb_bornes: la taille du "buffer", soit le nombre de bornes disponibles
 *                 - liste_attente_velos: liste permettant de stocker l'ordre d'arrivee des habitants attendant un velo
 *                 - liste_attente_borne: liste permettant de stocker l'ordre d'arrivee des habitants attendant une
 *                   borne
 *                 
 *                 Le moniteur dispose de plusieur fonctions d'entree:
 *                 - deposerVelo, permettant aux habitants de "produire" un velo
 *                 - retirerVelo, permettant aux habitants de "consommer" un velo
 *                 - volerVelo, permettant au thread de controle de "consommer" un velo
 *                 - ajouterVelo, permettant au thread de controle de "produire" un velo
 *                 - passageCamionnette, qui depose ou retire des velos en fonction du nombre de bornes disponibles
 *                 - deposerVelosDepot, permettant a la camionnette de deposer ses velos au depot (ne tient pas compte
 *                   du nombre de bornes et depose plusieurs velos a la fois)
 *                 - retirerVelosDepot, permettant a la camionnette de retirer les velos du depot (permet de retirer 
 *                   plusieurs velos a la fois)
 *                 
 *                 Afin d'eviter les acces concurrents, un mutex est verouille au debut de chaque fonction d'entree et
 *                 normalement relache a la fin. Si les bornes sont toutes occupees dans le cas d'un depot ou toutes
 *                 libres dans le cas d'un retrait, la tache tentant d'acceder au moniteur est suspendue sur la
 *                 condition respective. Lorsqu'une tache doit attendre la liberation du buffer, elle rend le mutex et
 *                 effectue un wait sur la condition appropriee. Afin d'eviter que plusieurs taches ne sorent de
 *                 l'attente en meme temps, celle-ci est encapsulee dans une boucle while. Pour eviter les problemes de
 *                 preemption dans la boucle, celle-ci est synchronisee sur la condition correspondante. Une fois la
 *                 boucle passee, le nombre de velos est modifie. Une fois que le nombre de velos a ete modifie, la
 *                 methode verifie s'il y a des taches en attente sur la condition contraire. Si c'est le cas, elles
 *                 sont reveillees, et le mutex leur est transmis. Si personne n'attend, alors le mutex est relache.
 * 
 *                 Dans le cas des methodes d'acces au moniteur utilisees par la tache de controle (ajouterVelo et
 *                 volerVelo), le mutex est verouille, on verifie s'il y a une borne ou un velo respectivement, on
 *                 verifie si des taches sont en attente de la condition concernee et on les reveille si c'est le cas.
 *                 S'il y a des taches a reveiller, alors le mutex et transmis et la methode est terminee: elle retourne
 *                 donc directement. Sinon, le mutex est relache.
 * 
 *                 Lors de l'appel de passageCamionnette, on verouille le mutex puis on verifie le nombre de bornes
 *                 disponibles. Si ce nombre correspond aux conditions (plus grand ou plus petit que le nombre de bornes
 *                 moins 2), alors des velos sont retires ou deposes, respectivement, selon la capacite de la
 *                 camionnette (min 0 velos, max 4). Si des taches sont en attente sur la condition correspondante, 
 *                 elles sont reveillees et la fonction retourne le nombre de velos ajoutes ou enleves, sans liberer le
 *                 mutex, afin de le transmettre. Si le nombre de velos est egal au nombre de bornes moins 2, la
 *                 camionnette ne fait rien et le mutex est relache.
 * 
 *                 dans le cas des methodes d'acces au depot, comme les habitants n'y ont pas acces, les conditions
 *                 ne sont pas verifiees. Il suffit ainsi de verouiller le mutex.
 *                 Dans le cas du retrait, il faut encore verifier qu'on prenne au maximum 4 velos du depot.
 * 
 *                 
 * @author Numa Trezzini
 */

import javax.swing.JTextArea;
 

/**
 * Classe Site
 */
public class Site {
    
    //identificateur du site
    private final int id_site;
    
    //nombre de velos actuellement disponibles
    private int nb_velos;
    
    //nombre de taches en attent d'un velo
    private int nb_attente_velo = 0;
    
    //nombre de taches en attente d'une borne
    private int nb_attente_borne = 0;
    
    //nombre de bornes a disposition. represente la taille du buffer dans le paradigme du prod/conso
    private int nb_bornes;
    
    //mutex du moniteur
    private Mutex mutex = new Mutex();
    
    //"condition" d'attente d'un velo
    private final Object velo_mutex = new Object();
    
    //"condition" d'attente d'une borne
    private final Object borne_mutex = new Object();
    
    /**
     * Nom: Site
     * @param id_site
     * @param nb_bornes 
     */
    public Site(int id_site, int nb_bornes){
        this.id_site = id_site;
        this.nb_bornes = nb_bornes;
        this.nb_velos = this.nb_bornes-2;
    }/*end Site*/
    
    /**
     * Nom: deposerVelo
     * But: permet de deposer un velo et ainsi d'occuper une borne. Cette fonction est synchrone.
     */
    public void deposerVelo(JTextArea ta){
        ta.append("je depose un velo au site "+id_site+"...\n");
        //verouillage du moniteur
        mutex.get();
        //verification du contenu du buffer: les bornes ne doivent pas etre toutes occupees
        synchronized(borne_mutex){
            while(this.nb_velos == this.nb_bornes){
                //increment du nombre d'habitants en attente d'une borne
                this.nb_attente_borne++;
                //on libere le mutex, comme on va attendre
                mutex.free();
                ta.append("j'attends une borne...\n");
                //attente sur une borne
                try{borne_mutex.wait();}
                catch(InterruptedException e){}
                //decrement du nombre d'habitants en attente d'une borne
                this.nb_attente_borne--;
            }
        }
        //il y a des bornes disponibles
        //increment du nombre de velos
        this.nb_velos++;
        //on libere soit le premier habitants en attente d'un velo (et on leur transmet le mutex)
        synchronized(velo_mutex){
            if(this.nb_attente_velo > 0){
                velo_mutex.notify();
            }
            //sinon on libere le mutex.
            else{
                mutex.free();
            }
        }
        ta.append("j'ai fini de deposer un velo au site "+id_site+"...\n");
    }/*end deposerVelo*/
    
    
    /**
     * Nom: retirerVelo
     * But: permet de retirer un velo et ainsi de liberer une borne
     */
    public void retirerVelo(JTextArea ta){
        ta.append("je prends un velo au site "+id_site+"...\n");
        //verouillage du mutex du moniteur
        mutex.get();
        synchronized(velo_mutex){
            //verification s'il y a des velos disponibles
            while(this.nb_velos == 0){
                //increment du compteur d'attente d'un velo
                this.nb_attente_velo++;
                //liberation du mutex pour que la prochaine tache puisse acceder au moniteur
                mutex.free();
                ta.append("j'attends un velo...\n");
                //attente sur la condition
                try{velo_mutex.wait();}
                catch(InterruptedException e){}
                this.nb_attente_velo--;
            }
        }
        //il y a des velos disponibles
        //decrement du nombre de velos
        this.nb_velos--;
        synchronized(borne_mutex){
            //on verifie si des taches attendent des bornes libres et on reveille la premiere
            if(this.nb_attente_borne > 0){
                borne_mutex.notify();
            }
            //sinon, on libere le mutex
            else{
                mutex.free();
            }
        } 
        ta.append("j'ai fini de prendre un velo au site "+id_site+"...\n");
    }/*end retirerVelo*/
    
    
    /**
     * Nom: volerVelo
     * But: supprime un velo s'il en reste
     */
    public void volerVelo(){
        mutex.get();
        synchronized(borne_mutex){
            //s'il y a des velos, on en prend un
            if(this.nb_velos > 0){
                this.nb_velos--;
                //il y a maintenant des bornes libres, on verifie donc si quelqu'un en attend une
                //et on lui transfere le mutex. La fonctione se termine donc
                if(this.nb_attente_borne > 0){
                    borne_mutex.notify();
                    return;
                }
            }
        }
        mutex.free();
    }/*end volerVelo*/
    
    
    /**
     * Nom: ajouterVelo
     * But: ajoute un velo au depot
     */
    public void ajouterVelo(){
        mutex.get();
        synchronized(velo_mutex){
            this.nb_velos++;
            //si quelqu'un attend un velo, on le reveille
            //normalement, cette verification est inutile, car personne ne va au depot, en dehors de la camionnette
            if(this.nb_attente_velo > 0){
                velo_mutex.notify();
                return;
            }
        }
        mutex.free();
    }/*end ajouterVelo*/
    
    
    /**
     * Nom: passageCamionnette
     * @param nb_velos_camionnette
     * @return int: le nombre de velos ajoutes (retour positif) ou retires (retour negatif). nul si rien n'a bouge
     * Remarques: le nombre renvoye est le nombre de velos deplaces entre le site et la camionnette. positif si la
     *            camionnette a pris des velos, negatif si elle en a depose et 0 sinon.
     */
    public int passageCamionnette(int nb_velos_camionnette){
        int c = 0;
        //on verouille le mutex du moniteur
        mutex.get();
        //s'il y a moins de 2 bornes disponibles, on prend des velos dans la mesure de la capacite de la camionnette
        if(nb_velos > nb_bornes-2){
            synchronized(borne_mutex){
                c = Math.min(nb_velos-(nb_bornes-2), 4-nb_velos_camionnette);
                this.nb_velos -= c;
                //on reveille les taches en attente d'une borne s'il y a lieu
                //on transfere le mutex dans ce cas. Sinon, on libere le mutex
                //puis la fonction se termine
                if(this.nb_attente_borne > 0){
                    borne_mutex.notify();
                }
                else{
                    mutex.free();
                }
                return c;
            }
        }
        //s'il y plus de 3 bornes disponibles, on ajoute des velos, dans la mesure de la capacite de la camionnette
        else if(nb_velos < nb_bornes-2){
            synchronized(velo_mutex){
                c = Math.min((nb_bornes-2)- nb_velos, nb_velos_camionnette);
                this.nb_velos += c;
                //on reveille les taches en attente d'un velo s'il y a lieu
                //on transfere le mutex dans ce cas. Sinon, on libere le mutex
                //puis la fonction se termine
                if(this.nb_attente_velo > 0){
                    velo_mutex.notify();
                }
                else{
                    mutex.free();
                }
                return -c;
            }
        }
        //si rien n'a ete fait, on libere le mutex et on termine la fonction
        mutex.free();
        return c;
    }/*end passageCamionnette*/
    
    /**
     * Nom: deposerVelosDepot
     */
    public void deposerVelosDepot(int nb_velos_camionnette){
        this.mutex.get();
        this.nb_velos += nb_velos_camionnette;
        this.mutex.free();
    
    }/*end deposerVelosDepot*/
    
    /**
     * Nom: retirerVelosDepot
     */
    public int retirerVelosDepot(){
        int nb_velos_camionnette;
        this.mutex.get();
        nb_velos_camionnette = Math.min(this.nb_velos, 4);
        this.nb_velos -= nb_velos_camionnette;
        this.mutex.free();
        return nb_velos_camionnette;
    }/*end retirerVelosDepot*/
    
    /**
     * Nom: getNbVelos
     * @return int: le nombre de velos disponibles sur le site
     */
    synchronized int getNbVelos(){return this.nb_velos;}/*end getNbVelos*/
    
    /**
     * Nom: setNbVelos
     * @param nb_velos 
     */
    synchronized void setNbVelos(int nb_velos){
        if(nb_velos > this.nb_bornes)
            this.nb_velos = this.nb_bornes;
        else
            this.nb_velos = nb_velos;
    }/*end setNbVelos*/
}
