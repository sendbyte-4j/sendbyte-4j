package africa.sendbyte.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import africa.sendbyte.RetryPolicy;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class RetryPlannerTest {
    @Test
    void saturatesBackoffInsteadOfOverflowingAtHighAttemptCounts() {
        final RetryPolicy policy =
                RetryPolicy.builder()
                        .maxAttempts(40)
                        .initialBackoff(Duration.ofSeconds(10))
                        .maxBackoff(Duration.ofMinutes(5))
                        .jitterRatio(0.0d)
                        .build();
        final RetryPlanner planner = new RetryPlanner(policy, Clock.systemUTC(), () -> 0.5d);

        for (int attempt = 1; attempt <= 40; attempt++) {
            final Duration delay = planner.delay(null, attempt);
            assertTrue(
                    delay.compareTo(policy.maxBackoff()) <= 0,
                    "attempt " + attempt + " produced " + delay);
        }

        assertEquals(Duration.ofSeconds(10), planner.delay(null, 1));
        assertEquals(Duration.ofMinutes(5), planner.delay(null, 40));
    }

    @Test
    void capsBackoffThatCannotBeExpressedInNanoseconds() {
        final RetryPolicy policy =
                RetryPolicy.builder()
                        .maxAttempts(3)
                        .initialBackoff(Duration.ofDays(1_000_000))
                        .maxBackoff(Duration.ofDays(1_000_000))
                        .jitterRatio(0.0d)
                        .build();
        final RetryPlanner planner = new RetryPlanner(policy, Clock.systemUTC(), () -> 0.5d);

        assertEquals(Duration.ofDays(1_000_000), planner.delay(null, 2));
    }
}
