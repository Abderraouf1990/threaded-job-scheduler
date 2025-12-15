package com.aayadi.threaded;

public enum JobPriority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int weight;

    JobPriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
