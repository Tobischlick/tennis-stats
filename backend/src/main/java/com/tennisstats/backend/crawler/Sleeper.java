package com.tennisstats.backend.crawler;

import java.time.Duration;

@FunctionalInterface
interface Sleeper {

    void sleep(Duration duration);

    static Sleeper threadSleep() {
        return duration -> {
            if (duration.isZero() || duration.isNegative()) {
                return;
            }
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting between requests", e);
            }
        };
    }
}
