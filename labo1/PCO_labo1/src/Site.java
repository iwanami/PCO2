/**
 *
 * @author Numa Trezzini
 * @author
 * @version 
 */
 

/**
 * Classe Site
 * @see 
 */
public class Site {
    
    private final int id_site;
    
    private int nb_velos;
    
    private int nb_attente_velo = 0;
    
    private int nb_attente_borne = 0;
    
    //private boolean[] bornes;
    
    private int nb_bornes;
    
    private Mutex mutex = new Mutex();
    
    private Object velo_mutex = new Object();
    
    private Object borne_mutex = new Object();
    
    /**
     * Nom: Site
     * @param id_site
     * @param nb_bornes 
     */
    public Site(int id_site, int nb_bornes){
        this.id_site = id_site;
        this.nb_bornes = nb_bornes;
        //bornes = new boolean[nb_bornes];
        //TODO voir s'il faut initialiser a false le tableau
        this.nb_velos = this.nb_bornes-2;
        /*for(int i = 0; i < this.nb_velos; i++){
            bornes[i] = true;
        }*/
    }/*end Site*/
    
    /**
     * Nom: deposerVelo
     */
    public void deposerVelo(){
        System.out.println("je depose un velo...");
        mutex.get();
        synchronized(this){
            while(this.nb_velos == this.nb_bornes){
                this.nb_attente_borne++;
                mutex.free();
                System.out.println("j'attends un velo...");
                try{borne_mutex.wait();}
                catch(InterruptedException e){}
                //hum...
                this.nb_attente_borne--;
            }
            //this.bornes[this.nb_velos] = true;
            this.nb_velos++;
            if(this.nb_attente_velo > 0){
                velo_mutex.notifyAll();
            }
            else{
                mutex.free();
            }
        }
        System.out.println("j'ai fini de deposer un velo...");
    }/*end deposerVelo*/
    
    
    /**
     * Nom: retirerVelo
     */
    public void retirerVelo(){
        System.out.println("je prends un velo...");
        mutex.get();
        synchronized(this){
            while(this.nb_velos == 0){
                this.nb_attente_velo++;
                mutex.free();
                System.out.println("j'attends une borne...");
                try{velo_mutex.wait();}
                catch(InterruptedException e){}
                //hum...
                this.nb_attente_velo--;
            }
            this.nb_velos--;
            if(this.nb_attente_borne > 0){
                borne_mutex.notifyAll();
            }
            else{
                mutex.free();
            }
        }
        System.out.println("j'ai fini de prendre un velo...");
    }/*end retirerVelo*/
    
    
    /**
     * Nom: volerVelo
     */
    public void volerVelo(){
        mutex.get();
        synchronized(this){
            if(this.nb_velos > 0){
                this.nb_velos--;
                if(this.nb_attente_borne > 0){
                    this.borne_mutex.notifyAll();
                    return;
                }
            }
            mutex.free();
        }
    }/*end volerVelo*/
    
    
    /**
     * Nom: ajouterVelo
     */
    public void ajouterVelo(){
        mutex.get();
        synchronized(this){
            if(this.nb_velos < this.nb_bornes){
                this.nb_velos++;
                if(this.nb_attente_velo > 0){
                    this.velo_mutex.notifyAll();
                    return;
                }
            }
        }
        mutex.free();
    }/*end ajouterVelo*/
    
    
    /**
     * Nom: passageCamionnette
     * @param nb_velos_camionnette
     * @return 
     */
    public int passageCamionnette(int nb_velos_camionnette){
        int c;
        mutex.get();
        synchronized(this){
            //TODO enlever les velos du site
            if(nb_velos > nb_bornes-2){
                c = Math.min(nb_velos-(nb_bornes-2), 4-nb_velos_camionnette);
                this.nb_velos -= c;
            }
            else if(nb_velos < nb_bornes-2){
                c = Math.min((nb_bornes-2)- nb_velos, nb_velos_camionnette);
                this.nb_velos += c;
            }
            else{
                c = 0;
            }
        }
        mutex.free();
        return c;
    }/*end passageCamionnette*/
    
    /**
     * Nom: getNbVelos
     * @return 
     */
    public synchronized int getNbVelos(){return this.nb_velos;}/*end getNbVelos*/
    
    /**
     * Nom: setNbVelos
     * @param nb_velos 
     */
    synchronized void setNbVelos(int nb_velos){this.nb_velos = nb_velos;}/*end setNbVelos*/
}
