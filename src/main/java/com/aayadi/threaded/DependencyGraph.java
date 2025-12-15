package com.aayadi.threaded;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DependencyGraph {

    private final Lock lock = new ReentrantLock();

    private final Map<String, Set<String>> remainingDeps;

    private final Map<String, Set<String>> reverseDeps;

    public DependencyGraph(Map<String, Set<String>> remainingDeps, Map<String, Set<String>> reverseDeps) {
        this.remainingDeps = remainingDeps;
        this.reverseDeps = reverseDeps;
    }

    public void registerJob(Job job) {
        lock.lock();
        try {
            String jobId = job.getId();

            remainingDeps.putIfAbsent(
                    jobId,
                    new HashSet<>(job.getDependencies())
            );

            for (String dep : job.getDependencies()) {
                reverseDeps
                        .computeIfAbsent(dep, k -> new HashSet<>())
                        .add(jobId);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isReady(Job job) {
        lock.lock();

        try {
            return remainingDeps
                    .getOrDefault(job.getId(), Collections.emptySet())
                    .isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public Set<String> markCompleted(String jobId) {
        lock.lock();
        try {
            Set<String> dependents = reverseDeps.getOrDefault(jobId, Set.of());

            Set<String> newlyReady = new HashSet<>();

            for (String dependentId : dependents) {
                Set<String> deps = remainingDeps.get(dependentId);
                if (deps != null) {
                    deps.remove(jobId);
                    if (deps.isEmpty()) {
                        newlyReady.add(dependentId);
                    }
                }
            }

            remainingDeps.remove(jobId);
            reverseDeps.remove(jobId);

            return newlyReady;
        } finally {
            lock.unlock();
        }
    }
}

