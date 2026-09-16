package africa.sendbyte.internal;

import java.time.Duration;

@FunctionalInterface
interface Sleeper {
    void sleep(Duration delay) throws InterruptedException;
}
