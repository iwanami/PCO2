/**
 * Nom           : Mutex
 * But           : Cette classe a pour objectif la modelisation d'un mutex tel que vu aux cours
 * Fonctionnement: Le mutex se compose d'un compteur, permettant de savoir si une tache l'a deja bloque, et d'un objet,
 *                 servant de file d'attente. la fonction get permet de verouiller le mutex et free de le liberer.
 * Remarques     : code repris d'un exercice fait en classe
 * @author Numa Trezzini
 */
public class Mutex {
    
    private int nb_attente = 0;
    
    private final Object mutex = new Object();
    
    /**
     * Nom: get
     * But: permet de mettre en attente les taches venant apres la premiere, qui est bloquante, avant de commencer les
     *      operations venant apres l'appel du verrou. Faire l'increment dans le test de la condition permet de bloquer
     *      les taches suivantes.
     */
    public void get(){
        synchronized(mutex){
            if(nb_attente++ > 0){
                try{mutex.wait();}
                catch(InterruptedException e){}
                
            }
        }
    }/*end get*/
    
    
    /**
     * Nom: free
     * But: Libere une des taches mises en attente sur le mutex, apres avoir decremente le compteur d'attente
     */
    public void free(){
        synchronized(mutex){
            nb_attente--;
            mutex.notify();
        }
    }/*end free*/
    
}
