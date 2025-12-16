package com.localcollab.platform.domain;

import java.time.Instant;

public class DriverStatus {

    public enum State {
        HEALTHY,
        RETRYING,
        PAUSED
    }

    private final int maxRetries;
    private int consecutiveFailures;
    private Instant lastFailureAt;
    private String lastFailureReason;
    private State state;

    public DriverStatus() {
        this(3, 0, null, null, State.HEALTHY);
    }

    public DriverStatus(int maxRetries, int consecutiveFailures, Instant lastFailureAt, String lastFailureReason, State state) {
        this.maxRetries = maxRetries;
        this.consecutiveFailures = consecutiveFailures;
        this.lastFailureAt = lastFailureAt;
        this.lastFailureReason = lastFailureReason;
        this.state = state;
    }

    public static DriverStatus healthy() {
        return new DriverStatus();
    }

    public DriverStatus recordFailure(String reason) {
        consecutiveFailures++;
        lastFailureAt = Instant.now();
        lastFailureReason = reason == null ? "" : reason.trim();
        if (consecutiveFailures >= maxRetries) {
            state = State.PAUSED;
        } else {
            state = State.RETRYING;
        }
        return this;
    }

    public DriverStatus recordRecovery() {
        consecutiveFailures = 0;
        lastFailureAt = null;
        lastFailureReason = null;
        state = State.HEALTHY;
        return this;
    }

    public DriverStatus asPaused() {
        state = State.PAUSED;
        return this;
    }

    public DriverStatus reset() {
        return recordRecovery();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public Instant getLastFailureAt() {
        return lastFailureAt;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public State getState() {
        return state;
    }
}
