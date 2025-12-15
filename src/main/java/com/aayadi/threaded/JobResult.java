package com.aayadi.threaded;


public record JobResult(boolean success, Object result, Throwable error, long executionTimeMillis) {

    public static JobResult success(Object result, long executionTimeMillis) {
        return new JobResult(true, result, null, executionTimeMillis);
    }

    public static JobResult failure(Throwable error, long executionTimeMillis) {
        return new JobResult(false, null, error, executionTimeMillis);
    }
}
