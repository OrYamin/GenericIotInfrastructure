package server.rps.executor;

import java.util.concurrent.*;

public class ThreadPool implements Executor {
    private WaitablePQueue<Task<?>> taskWaitablePQueue = new WaitablePQueue<>();
    private int originalNumberOfThreads;
    private volatile boolean isPaused = false;
    private boolean isShutdown = false;
    private final Object poolLock = new Object();
    private volatile int activeThreads;
    private final int HIGHEST_PRIORITY = Priority.HIGH.getValue() + 1;
    private final int LOWEST_PRIORITY = Priority.LOW.getValue() - 1;

    public ThreadPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ThreadPool(int numberOfThreads) {
        setNumOfThreads(numberOfThreads);
    }

    @Override
    public void execute(Runnable runnable) {
        submit(runnable, Priority.MEDIUM);
    }

    public Future<?> submit(Runnable command){
        return submit(command, Priority.MEDIUM);
    }

    public Future<?> submit(Runnable command, Priority p){
        return submit(Executors.callable(command), p);
    }

    public <T> Future<T> submit(Runnable command, Priority p, T value){
        return submit(Executors.callable(command, value), p);
    }

    public <T> Future<T> submit(Callable<T> command){
        return submit(command, Priority.MEDIUM);
    }

    public <T> Future<T> submit(Callable<T> command, Priority p){
        if(null == command){
            throw new NullPointerException();
        }

        if(isShutdown){
            throw new RejectedExecutionException("Cannot add new tasks");
        }

        Task<T> newTask = new Task<>(command, p.getValue());
        taskWaitablePQueue.enqueue(newTask);
        return newTask.future;
    }

    // if threads are removed, they should be the first threads that not running
    public void setNumOfThreads(int numOfThreads){
        if(0 >= numOfThreads){
            throw new IllegalArgumentException();
        }

        if(numOfThreads > originalNumberOfThreads){
            int diff = numOfThreads - originalNumberOfThreads;
            for (int i = 0; i < diff; ++i) {
                new Thread(this::workerThreadLoop).start();
                synchronized (poolLock){
                    ++activeThreads;
                }
            }
        } else if(numOfThreads < originalNumberOfThreads){
            int diff = originalNumberOfThreads - numOfThreads;
            for (int i = 0; i < diff; i++) {
                Task<?> poison = new Task<>(()-> null, HIGHEST_PRIORITY);
                poison.isPoison = true;
                taskWaitablePQueue.enqueue(poison);
            }
        }
        originalNumberOfThreads = numOfThreads;
    }

    public void pause(){
        if(!isPaused) {
            synchronized (poolLock) {
                if(!isPaused) {
                    isPaused = true;
                    for (int i = 0; i < originalNumberOfThreads; i++) {
                        taskWaitablePQueue.enqueue(new SleepingPill());
                    }
                }
            }
        }
    }

    public void resume(){
        synchronized (poolLock) {
            isPaused = false;
            poolLock.notifyAll(); // notify all sleeping pill threads
        }
    }

    public void shutdown(){
        isShutdown = true;
        for (int i = 0; i < originalNumberOfThreads; i++) {
            Task<?> poison = new Task<>(()-> null, LOWEST_PRIORITY);
            poison.isPoison = true;
            taskWaitablePQueue.enqueue(poison);
        }
    }

    public void awaitTermination() throws InterruptedException{
        if(!isShutdown){
            throw new RejectedExecutionException();
        }

        synchronized (poolLock) {
            while (0 < activeThreads) {
                poolLock.wait();  // Wait for all tasks to complete
                if(Thread.currentThread().isInterrupted()){
                    throw new InterruptedException("Current thread was interrupted while waiting");
                }
            }
        }
    }

    public boolean awaitTermination(long timeout,TimeUnit unit) throws InterruptedException{
        if(!isShutdown){
            throw new RejectedExecutionException();
        }

        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        synchronized (poolLock) {
            while (0 < activeThreads) {
                long remainingTime = deadline - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    return false;  // Timeout exceeded
                }
                poolLock.wait(remainingTime);  // Wait for tasks or timeout
            }
            return true;
        }
    }

    private class Task<E> implements Comparable<Task<E>> {
        private final Callable<E> command;
        private final int priority;
        private final Future<E> future = new TaskFuture();
        private E result;
        private boolean isDone = false;
        private boolean isCancelled = false;
        private boolean hasStarted = false;
        private volatile boolean isPoison = false;
        private Exception executionException;
        private final Object taskLock = new Object();

        public Task(Callable<E> command, int priority) {
            this.command = command;
            this.priority = priority;
        }

        @Override
        public int compareTo(Task<E> task){
            return task.priority - this.priority;
        }

        private void executeTask() {
            try {
                hasStarted = true;
                result = command.call();
            } catch (Exception e) {
                executionException = e;
            } finally {
                isDone = true;
                synchronized (taskLock) {
                    taskLock.notifyAll(); // Notify any waiting thread on get() that task is complete
                }
            }
        }

        private class TaskFuture implements Future<E> {
            @Override
            public boolean cancel(boolean b) {
                synchronized (taskLock) {
                    if (isDone || isCancelled || hasStarted) {
                        return false; // Task already finished or canceled or running
                    }
                    ThreadPool.this.taskWaitablePQueue.remove(this);
                    isCancelled = true;
                    isDone = true;
                    taskLock.notifyAll(); // Notify waiting threads in case they are blocked in get()
                    return true;
                }
            }

            @Override
            public boolean isCancelled() {
                return isCancelled;
            }

            @Override
            public boolean isDone() {
                return isDone;
            }

            @Override
            public E get() throws InterruptedException, ExecutionException {
                synchronized (taskLock) {
                    while (!isDone){
                        taskLock.wait(); // Wait until the task is done
                        if(Thread.currentThread().isInterrupted()){
                            throw new InterruptedException("Current thread was interrupted while waiting");
                        }
                    }

                    if (isCancelled) {
                        throw new CancellationException();
                    }

                    if (executionException != null) {
                        throw new ExecutionException(executionException);
                    }

                    return result;
                }
            }

            @Override
            public E get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                long remainingTime = timeUnit.toMillis(timeout);  // Convert timeout to milliseconds
                long deadline = System.currentTimeMillis() + remainingTime;  // Calculate the deadline

                synchronized (taskLock) {
                    while (!isDone) {
                        long waitTime = deadline - System.currentTimeMillis();
                        if (waitTime <= 0) {
                            throw new TimeoutException("Timeout exceeded while waiting for the task to complete.");
                        }
                        taskLock.wait(waitTime);  // Wait for the task to complete or timeout

                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException("Current thread was interrupted while waiting");
                        }
                    }

                    if (isCancelled) {
                        throw new CancellationException();
                    }

                    if (executionException != null) {
                        throw new ExecutionException(executionException);
                    }

                    return result;
                }
            }
        }
    }

    private void workerThreadLoop(){
        boolean isPoison = false;
        while(!isPoison) {
            Task<?> task = taskWaitablePQueue.dequeue();
            task.executeTask();
            isPoison = task.isPoison;
        }
        synchronized (poolLock){
            --activeThreads;
            if(0 == activeThreads) {
                poolLock.notifyAll(); // for awaitTermination waiters
            }
        }
    }

    private class SleepingPill extends Task<Void> {
        public SleepingPill() {
            super(() -> {
                synchronized (poolLock) {
                    while (ThreadPool.this.isPaused) {
                        poolLock.wait(); // lock to avoid busy waiting
                    }
                }
                return null;
            }, HIGHEST_PRIORITY);
        }
    }
}