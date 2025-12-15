package com.aayadi.threaded;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.util.Set;

@SpringBootApplication
public class ThreadedJobSchedulerApplication {

    public static void main(String[] args) {

        // Create a JobScheduler with a thread pool size of 4
        JobScheduler scheduler = new JobScheduler(2, Duration.ofSeconds(2), 2);

        // Create and submit a simple job
        Job job1 = new Job("job1", JobPriority.LOW, () -> {
            System.out.println("Executing job1");
			Thread.sleep(1000);
            return new JobResult(true, null, null, 10);
        }, Set.of());
		scheduler.submit(job1);

		Job job2 = new Job("job2", JobPriority.MEDIUM, () -> {
			System.out.println("Executing job2");
			Thread.sleep(5000);
			return new JobResult(true, null, null, 10000);
		}, Set.of());
        scheduler.submit(job2);

		Job job3 = new Job("job3", JobPriority.HIGH, () -> {
			System.out.println("Executing job3");
			Thread.sleep(10000);
			return new JobResult(true, null, null, 10000);
		}, Set.of());
		scheduler.submit(job3);

		Job job4 = new Job("job4", JobPriority.HIGH, () -> {
			System.out.println("Executing job4");
			Thread.sleep(10000);
			return new JobResult(true, null, null, 10000);
		}, Set.of());
		scheduler.submit(job4);

		Job job5 = new Job("job5", JobPriority.MEDIUM, () -> {
			System.out.println("Executing job5");
			Thread.sleep(5000);
			return new JobResult(true, null, null, 10000);
		}, Set.of("job1"));
		scheduler.submit(job5);

        // Start the thread pool
        scheduler.getThreadPool().start();

		SpringApplication.run(ThreadedJobSchedulerApplication.class, args);

	}
}
