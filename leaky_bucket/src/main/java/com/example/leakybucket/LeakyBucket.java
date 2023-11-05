package com.example.leakybucket;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class LeakyBucket {
    private int id = 0;
    private Random rand = new Random();
    private static final int UNIT_TIME = 1000; //1000 milli seconds
    private int REQUESTSTOPROCESS; //Requests processed per leak
    private int INTERVALBETWEENLEAK; //interval between two successive leaks
    private final LinkedBlockingQueue<Request> bucket; //Threadsafe

    public LeakyBucket(int rate, int capacity) {
        bucket = new LinkedBlockingQueue<>(capacity);
        this.REQUESTSTOPROCESS = 1;
        this.INTERVALBETWEENLEAK = 1; //in milliseconds
        if (rate <= UNIT_TIME) {
            INTERVALBETWEENLEAK = UNIT_TIME / rate; //In this case requestsToProcess == 1 request per leak
        } else {
            REQUESTSTOPROCESS = rate / UNIT_TIME; //In this case intervalToleak == 1 milliseconds
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LeakyBucket limiter = new LeakyBucket(2, 6);
        Thread t = new Thread() {
            public void run() {
                try {
                    limiter.process();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
        while (true) {
            Request req = new Request(System.currentTimeMillis(), limiter.id++);
            System.out.println("request accepted " + limiter.acceptRequest(req));
            Thread.sleep(limiter.rand.nextInt(40) + 800);
        }
    }

    /**
     * Main thread will invoke it
     *
     * @param req request
     * @return true if accepted else false
     */
    public boolean acceptRequest(Request req) {
        return bucket.offer(req); //Add at the tail. If capacity is full drop the request and return false
    }

    /**
     * Second thread will invoke it
     */
    public void process() throws InterruptedException {
        int requestsForProcess = REQUESTSTOPROCESS; //Number of requests to leak
        while (true) {
            while (requestsForProcess > 0) {
                Request req = bucket.peek();
                if (req != null) {
                    System.out.println("id: " + req.id + " add time: " + req.timeMillis + " delay: " + (System.currentTimeMillis() - req.timeMillis));
                    bucket.poll(); //Leak it
                    requestsForProcess--;
                } else { //bucket is empty
                    System.out.println("Bucket is empty");
                    break;
                }
            }
            requestsForProcess = REQUESTSTOPROCESS;
            Thread.sleep(INTERVALBETWEENLEAK); //Wait now
        }
    }
}

class Request {
    final long timeMillis;
    final int id;

    Request(long time, int id) {
        this.timeMillis = time;
        this.id = id;
    }
}
