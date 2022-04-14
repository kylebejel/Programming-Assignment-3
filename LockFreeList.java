import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;


public class LockFreeList<T> {

    final Node<T> head;
    final Node<T> tail;
    
    static class Node<T> {
        int key;
        AtomicMarkableReference<Node<T>> next;
               
        Node(int key) {
            this.key = key;
            this.next = new AtomicMarkableReference<Node<T>>(null, false);
        }
    }
    
    // window class as seen in textbook
    static class Window<T> {
        public Node<T> pred, curr;
        
        Window(Node<T> myPred, Node<T> myCurr) {
            pred = myPred; 
            curr = myCurr;
        }
    }
    
    public LockFreeList() {
        tail = new Node<T>(Integer.MAX_VALUE);
        head = new Node<T>(Integer.MIN_VALUE);
        head.next.set(tail, false);
    }
    
    // lock free add as seen in textbook
    // action #1 for the servant
    public boolean add(int key) {
        while (true) {
            Window<T> window = find(key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key == key) { 
                return false;
            } else {
                Node<T> node = new Node<T>(key);
                node.next.set(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }       
    }

    // remove function taken from textbook
    // action #2 for the servant
    public boolean remove(int key) {
        boolean snip;
        while (true) {
            Window<T> window = find(key);         
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key != key) {
                return false;
            } else {
                Node<T> succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if (!snip) {
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    // find function taken from textbook
    public Window<T> find(int key) {
        Node<T> pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry: 
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) continue retry;
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key) {
                    return new Window<T>(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    // contains as seen in textbook
    // action #3 for the servant
    public boolean contains(int key) {
        boolean[] marked = {false};
        Node<T> curr = head.next.getReference();
        curr.next.get(marked);
        while(curr.key < key){
            curr = curr.next.getReference();
            Node<T> succ = curr.next.get(marked);
        }
        return (curr.key == key && !marked[0]); 
    }

    public static class servantThread implements Runnable {

        private ArrayList<Integer> oldGifts;
        private LockFreeList giftChain;

        public servantThread(ArrayList<Integer> old, LockFreeList chain){
            oldGifts = old;
            giftChain = chain;
        }
        @Override
        public void run() {
            Random rand = new Random();
            rand.setSeed(System.currentTimeMillis() + rand.nextInt(50));
            // generate random number for choice
            while(true){
                int choice = rand.nextInt(3);
                switch (choice) {
                    // option 1 add
                    case 0:
                        this.giftChain.add(this.oldGifts.remove(0));
                        break;
                    // option 2 remove
                    case 1:
                        this.giftChain.remove(/*insert logic*/);
                        break;
                    
                    // option 3 contains
                    case 3:
                        break;
            
                    default:
                        break;
                }
            }            
        }
        
    }
    public static void main(String[] args) {
        ArrayList<Integer> originalGiftChain = new ArrayList<>();
        LockFreeList giftChain = new LockFreeList<>();
        // create 500000 presents
        for(int i = 0; i < 500000; i++){
            originalGiftChain.add(i);
        }
        // shuffle so we get random order of gifts
        Collections.shuffle(originalGiftChain);
        // create servant threads
        Thread servants[] = new Thread[4];
        for(int i = 0; i < 4; i++){
            servants[i] = new Thread(new servantThread(originalGiftChain, giftChain));
        }

        for(int i = 0; i < 4; i++){
            servants[i].start();
        }

        for(int i = 0; i < 4; i++){
            try {
                servants[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}