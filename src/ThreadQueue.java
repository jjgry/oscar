/*
 * Copyright 2019 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, S.P. Vickers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package oscar;

public const int MAX_QUEUE_LENGTH = 25;
// A FIFO queue of items of type T
public class ThreadQueue<T> {

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



///<summary> Put an object on the end of the queue.<\summary>
    public synchronized void put(T val) {
        while (len >= MAX_QUEUE_LENGTH){
            try{
                this.wait(100);
            }
            catch(InterruptedException ie){
                //ignored exception
            }
        }

        Link<T> nextLink  = new Link(val);
        if(first==null) {
            first = nextLink;
            last = nextLink;
        }
        else{
            last.next = nextLink;
            last = nextLink;
        }
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
        return popVal;
    }
}