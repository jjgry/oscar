package oscar;

public class SegmentQueue<T>  {
    static final int MAX_QUEUE_LENGTH = 25;
// A FIFO queue of items of type T
private static class Link<L> {
    L val;
    Link<L> next;

    Link(L val) {
        this.val = val;
        this.next = null;
    }
}

    private Link<T> first = null;
    private Link<T> last = null;

    private int len = 0;




    public synchronized void put(T val) {//Put an object on the end of the queue.
        while (len >= MAX_QUEUE_LENGTH){
            try{
                this.wait(100);
            }
            catch(InterruptedException ie){
                //ignored exception
            }
        }

        Link<T> nextLink  = new Link<>(val);
        if(first==null) {
            first = nextLink;
            last = nextLink;
        }
        else{
            last.next = nextLink;
            last = nextLink;
        }
        len++;
        this.notify();
    }

    public synchronized T take() {
        while (first == null) { // use a loop to block thread until data is available
            try {
                this.wait(100);
            } catch (InterruptedException ie) {
                // Ignored exception
            }
        }
        //we have something to take. remove head of list and return.
        T popVal = first.val;
        first = first.next;
        len --;
        return popVal;
    }

    public int NumWaiting(){
        return len;
    }
}
