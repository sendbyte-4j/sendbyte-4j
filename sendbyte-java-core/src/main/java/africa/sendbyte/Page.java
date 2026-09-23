package africa.sendbyte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable cursor/list page returned by collection endpoints. */
public final class Page<T> {
    private final List<T> data;
    private final boolean hasMore;

    public Page(final List<T> data, final boolean hasMore) {
        this.data =
                data == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(data));
        this.hasMore = hasMore;
    }

    public List<T> data() {
        return this.data;
    }

    public boolean hasMore() {
        return this.hasMore;
    }
}
