package com.aayadi.threaded;

import lombok.Getter;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class Job implements Comparable<Job> {

    private final String id;
    private final JobPriority priority;
    private final Callable<JobResult> task;
    private final Set<String> dependencies;

    private final AtomicReference<JobState> state = new AtomicReference<>(JobState.PENDING);

    private final AtomicInteger attempts = new AtomicInteger(0);

    public Job(String id, JobPriority priority, Callable<JobResult> task, Set<String> dependencies) {
        this.id = id;
        this.priority = priority;
        this.task = task;
        this.dependencies = dependencies;
    }

    public Job(
            JobPriority priority,
            Callable<JobResult> task,
            Set<String> dependencies
    ) {
        this.id = UUID.randomUUID().toString();
        this.priority = Objects.requireNonNull(priority, "priority");
        this.task = Objects.requireNonNull(task, "task");
        this.dependencies = dependencies == null
                ? Set.of()
                : Collections.unmodifiableSet(dependencies);
    }
    public JobState getState() {
        return state.get();
    }

    public boolean markReady() {
        return state.compareAndSet(JobState.PENDING, JobState.READY) ||
                state.compareAndSet(JobState.FAILED, JobState.READY);
    }

    public boolean markRunning() {
        return state.compareAndSet(JobState.READY, JobState.RUNNING);
    }

    public void markSuccess() {
        state.set(JobState.SUCCESS);
    }

    public void markFailure() {
        state.set(JobState.FAILED);
    }

    public void cancel() {
        state.set(JobState.CANCELLED);
    }

    public boolean isTerminal() {
        return state.get().isTerminal();
    }

    public int incrementAttempts() {
        return attempts.incrementAndGet();
    }

    @Override
    public int compareTo(Job other) {
        if (other == this) return 0;
        if (other == null) return -1;

        if (this.priority == null || other.priority == null) {
            throw new IllegalStateException("priority is null");
        }
        int byPriority = Integer.compare(
                other.priority.getWeight(),
                this.priority.getWeight()
        );

        if (byPriority != 0) {
            return byPriority;
        }

        return this.id.compareTo(other.id);
    }
}
