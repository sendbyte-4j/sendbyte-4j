package africa.sendbyte.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

/**
 * Internal test-support controls for deterministic runtime tests.
 *
 * <p>This type is not a stable application API and may change between SDK versions.
 */
public final class TestExecutionControls {
    private final MutableClock clock;
    private final AtomicLong nanos = new AtomicLong();
    private final Sleeper sleeper;
    private final DoubleSupplier random;

    private TestExecutionControls(final Instant start, final double randomValue) {
        this.clock = new MutableClock(start);
        this.random = () -> randomValue;
        this.sleeper =
                delay -> {
                    this.clock.advance(delay);
                    this.nanos.addAndGet(delay.toNanos());
                };
    }

    public static TestExecutionControls fixed(final Instant start, final double randomValue) {
        return new TestExecutionControls(start, randomValue);
    }

    Clock clock() {
        return this.clock;
    }

    Ticker ticker() {
        return this.nanos::get;
    }

    Sleeper sleeper() {
        return this.sleeper;
    }

    DoubleSupplier random() {
        return this.random;
    }

    public void advance(final Duration duration) {
        this.clock.advance(duration);
        this.nanos.addAndGet(duration.toNanos());
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong epochMillis;

        private MutableClock(final Instant start) {
            this.epochMillis = new AtomicLong(start.toEpochMilli());
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(this.epochMillis.get());
        }

        private void advance(final Duration duration) {
            this.epochMillis.addAndGet(duration.toMillis());
        }
    }
}
