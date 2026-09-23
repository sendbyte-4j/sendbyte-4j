package africa.sendbyte.marketing;

import java.util.List;

/** Subscriber collection with list/account quota metadata. */
public final class SubscriberPage {
    private final List<Subscriber> data;
    private final int total;
    private final Integer cap;
    private final int used;

    SubscriberPage(
            final List<Subscriber> data, final int total, final Integer cap, final int used) {
        this.data = WireSupport.immutable(data);
        this.total = total;
        this.cap = cap;
        this.used = used;
    }

    public List<Subscriber> data() {
        return this.data;
    }

    public int total() {
        return this.total;
    }

    public Integer cap() {
        return this.cap;
    }

    public int used() {
        return this.used;
    }
}
