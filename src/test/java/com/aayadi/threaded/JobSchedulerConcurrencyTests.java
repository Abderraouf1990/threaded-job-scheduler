package com.aayadi.threaded;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobSchedulerConcurrencyTests {

    @Test
    void shouldExecuteJobsConcurrently() throws InterruptedException {
        int jobCount = 1_000;
        int workers = 8;

        JobScheduler scheduler = new JobScheduler(workers,  Duration.ofSeconds(2), 0);

        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(jobCount);

        scheduler.getThreadPool().start();

        for (int i = 0; i < jobCount; i++) {
            Job job = new Job(
                    JobPriority.MEDIUM,
                    () -> {
                        counter.incrementAndGet();
                        latch.countDown();
                        return JobResult.success(null, 0);
                    },
                    Set.of()
            );
            scheduler.submit(job);
        }

        latch.await();
        scheduler.getThreadPool().shutdown();

        assertEquals(jobCount, counter.get());
        assertEquals(jobCount, scheduler.getMetrics().getCompleted());
    }

    @Test
    void shouldRespectJobDependencies() throws InterruptedException {
        JobScheduler scheduler = new JobScheduler(4, Duration.ofSeconds(2), 0);

        StringBuilder executionOrder = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(3);

        Job jobA = new Job(
                JobPriority.HIGH,
                () -> {
                    executionOrder.append("A");
                    latch.countDown();
                    return JobResult.success(null, 0);
                },
                Set.of()
        );

        Job jobB = new Job(
                JobPriority.MEDIUM,
                () -> {
                    executionOrder.append("B");
                    latch.countDown();
                    return JobResult.success(null, 0);
                },
                Set.of(jobA.getId())
        );

        Job jobC = new Job(
                JobPriority.LOW,
                () -> {
                    executionOrder.append("C");
                    latch.countDown();
                    return JobResult.success(null, 0);
                },
                Set.of(jobB.getId())
        );

        scheduler.getThreadPool().start();
        scheduler.submit(jobA);
        scheduler.submit(jobB);
        scheduler.submit(jobC);

        latch.await();
        scheduler.getThreadPool().shutdown();

        assertEquals("ABC", executionOrder.toString());
    }

    @Test
    void shouldRetryFailedJob() throws InterruptedException {
        JobScheduler scheduler = new JobScheduler(2, Duration.ofSeconds(2), 1);

        AtomicInteger attempts = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);

        Job job = new Job(
                JobPriority.HIGH,
                () -> {
                    if (attempts.incrementAndGet() == 1) {
                        throw new RuntimeException("fail first");
                    }
                    latch.countDown();
                    return JobResult.success(null, 0);
                },
                Set.of()
        );

        scheduler.getThreadPool().start();
        scheduler.submit(job);

        latch.await();
        scheduler.getThreadPool().shutdown();

        assertEquals(2, attempts.get());
        assertEquals(1, scheduler.getMetrics().getCompleted());
        assertEquals(1, scheduler.getMetrics().getRetried());
    }

    @Test
    void stressTestWithDependencies() throws InterruptedException {
        int jobCount = 500;
        JobScheduler scheduler =
                new JobScheduler(6, Duration.ofSeconds(3), 0);

        CountDownLatch latch = new CountDownLatch(jobCount);
        Job[] jobs = new Job[jobCount];

        for (int i = 0; i < jobCount; i++) {
            jobs[i] = new Job(
                    JobPriority.MEDIUM,
                    () -> {
                        latch.countDown();
                        return JobResult.success(null, 0);
                    },
                    Set.of()
            );
        }

        // dépendance en chaîne
        for (int i = 1; i < jobCount; i++) {
            jobs[i] = new Job(
                    JobPriority.MEDIUM,
                    jobs[i].getTask(),
                    Set.of(jobs[i - 1].getId())
            );
        }

        scheduler.getThreadPool().start();
        for (Job job : jobs) {
            scheduler.submit(job);
        }

        latch.await();
        scheduler.getThreadPool().shutdown();

        assertEquals(jobCount, scheduler.getMetrics().getCompleted());
    }

}

