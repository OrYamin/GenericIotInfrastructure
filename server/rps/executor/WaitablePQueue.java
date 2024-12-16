package server.rps.executor;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WaitablePQueue<E> {

    private Semaphore semaphore = new Semaphore(0);
    private PriorityQueue<E> pq;

    public WaitablePQueue() {
        pq = new PriorityQueue<>();
    }

    public WaitablePQueue(Comparator<E> comparator) {
        pq = new PriorityQueue<>(comparator);
    }

    public void enqueue(E e) {
        synchronized (pq) {
            pq.add(e);
        }
        semaphore.release();
    }

    public E dequeue() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e){
            System.out.println(e.getMessage());
        }

        synchronized (pq) {
            return pq.poll();
        }
    }

    public E dequeue(long timeout, TimeUnit unit) {
        try {
            if(semaphore.tryAcquire(timeout, unit)){
                synchronized (pq) {
                    return pq.poll();
                }
            }
        } catch (InterruptedException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean remove(Object element){
        boolean removed;
        synchronized (pq) {
            removed =  pq.remove(element);
        }
        if(removed){
            try {
                semaphore.acquire();
            } catch (InterruptedException ignored){}
        }
        return removed;
    }

    public E peek(){
        synchronized (pq) {
            return pq.peek();
        }
    }

    public int size(){
        synchronized (pq) {
            return pq.size();
        }
    }

    public boolean isEmpty(){
        synchronized (pq) {
            return pq.isEmpty();
        }
    }
}