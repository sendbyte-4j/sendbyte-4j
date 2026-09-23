package africa.sendbyte.marketing;

import java.util.List;

/** Brand collection with account quota metadata. */
public final class BrandPage {
    private final List<Brand> data;
    private final Integer cap;
    private final int used;

    BrandPage(final List<Brand> data, final Integer cap, final int used) {
        this.data = WireSupport.immutable(data);
        this.cap = cap;
        this.used = used;
    }

    public List<Brand> data() {
        return this.data;
    }

    public Integer cap() {
        return this.cap;
    }

    public int used() {
        return this.used;
    }
}
