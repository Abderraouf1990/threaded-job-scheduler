package com.aayadi.threaded;

import java.time.Duration;
import java.util.concurrent.*;

class Worker extends Thread {

    private final BlockingQueue<Job> readyQueue;

    private final JobScheduler scheduler;
    private final Duration timeout;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Worker(
            String name,
            BlockingQueue<Job> readyQueue,
            JobScheduler scheduler,
            Duration timeout
    ) {
        super(name);
        this.readyQueue = readyQueue;
        this.scheduler = scheduler;
        this.timeout = timeout;
    }

    public Worker(BlockingQueue<Job> queue, JobScheduler scheduler, Duration timeout) {
        this("Worker-" + Thread.currentThread().threadId(), queue, scheduler, timeout);
    }


    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Job job = readyQueue.take();
                execute(job);
            }
        } catch (InterruptedException e) {
            System.err.println(
                    getName() + " crashed while executing job " + e.getMessage()
            );
            e.printStackTrace();
            interrupt();
        } finally {
            executor.shutdownNow();
        }
    }

    private void execute(Job job) throws InterruptedException {
        if (!job.markRunning()) {
            return; // job déjà pris ailleurs ou annulé
        }

        job.incrementAttempts();
        long start = System.currentTimeMillis();

        try {
            Future<JobResult> future =
                    executor.submit(job.getTask());

            JobResult result = future.get(
                    timeout.toMillis(),
                    TimeUnit.MILLISECONDS
            );

            job.markSuccess();
            scheduler.onJobSuccess(job, result, elapsed(start));

        } catch (Exception ex) {
            job.markFailure();
            scheduler.onJobFailure(job, ex, elapsed(start));
        }
    }

    private long elapsed(long startMillis) {
        return System.currentTimeMillis() - startMillis;
    }
}

