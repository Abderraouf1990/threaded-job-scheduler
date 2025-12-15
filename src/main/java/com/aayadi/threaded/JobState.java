package com.aayadi.threaded;

public enum JobState {
    PENDING,    // job created
    READY,      // dependencies resolved
    RUNNING,    // processing ongoing
    SUCCESS,    // finished successfully
    FAILED,     // finished with error
    CANCELLED;  // cancelled before or during execution

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
