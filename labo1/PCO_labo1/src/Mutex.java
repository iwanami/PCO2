/**
 *
 * @author Numa Trezzini
 * @author
 * @version 
 */

/**
 * Classe Mutex
 * @see 
 */
public class Mutex {
    
    private int nb_attente = 0;
    
    private Object mutex = new Object();
    
    //public Mutex(){}
    
    /**
     * Nom: get
     */
    public void get(){
        synchronized(mutex){
            if(nb_attente > 0){
                nb_attente++;
                try{mutex.wait();}
                catch(InterruptedException e){}
            }
        }
    }/*end get*/
    
    
    /**
     * Nom: free
     */
    public void free(){
        synchronized(mutex){
            nb_attente--;
            mutex.notify();
        }
    }/*end free*/
    
}
