package com.aayadi.threaded;

import lombok.Getter;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Getter
public class JobScheduler {

    private final BlockingQueue<Job> readyQueue = new PriorityBlockingQueue<>();

    private final DependencyGraph graph = new DependencyGraph(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    private final ThreadPool threadPool;

    private final Map<String, Job> allJobs = new ConcurrentHashMap<>();
    private final SchedulerMetrics metrics = new SchedulerMetrics();
    private final int maxRetries;


    public JobScheduler(int poolSize, Duration timeout, int maxRetries) {
        this.maxRetries = maxRetries;
        this.threadPool = new ThreadPool(poolSize, readyQueue, this, timeout);
    }


    /* =======================
       Job submission
       ======================= */

    public void submit(Job job) {
        synchronized (this) {
            allJobs.put(job.getId(), job);
            graph.registerJob(job);
        }

        metrics.incrementSubmitted();

        if (graph.isReady(job)) {
            job.markReady();
            readyQueue.offer(job);
        }
    }

    public void onJobSuccess(Job job, JobResult result, long elapsed) {
        metrics.incrementCompleted(elapsed);

        Set<String> newlyReady = graph.markCompleted(job.getId());

        for (String jobId : newlyReady) {
            Job dependent = allJobs.get(jobId);
            if (dependent != null && dependent.markReady()) {
                readyQueue.offer(dependent);
            }
        }
    }

    public void onJobFailure(Job job, Throwable  error, long elapsed) {

     if (job.getAttempts().get() <= maxRetries) {
         job.markReady();
         readyQueue.offer(job);
         metrics.incrementRetried();
    } else {
        job.markFailure();
        metrics.incrementFailed(elapsed);
    }

    }
}

