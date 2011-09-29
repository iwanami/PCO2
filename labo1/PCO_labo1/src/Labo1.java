/**
 * 
 * @author Numa Trezzini
 * @author
 * @version 
 */
 

/**
 *
 * @author Numa Trezzini
 */
public class Labo1 {
    
    private int nb_sites;
    
    private int nb_habitants;
    
    private Site[] sites;
    
    private Habitant[] habitants;
    
    private class Habitant extends Thread{
        
        int site_courant;
        
        public Habitant(int site_courant){
            super();
            this.site_courant = site_courant;
        }
        
        @Override
        public void run(){
        
        }
        
    }
    
    public void init(){
        
    }
    
    public static void main(String[] args) {
        
    }
    
    public int random(int min, int max){
        return 1;
    }
}
