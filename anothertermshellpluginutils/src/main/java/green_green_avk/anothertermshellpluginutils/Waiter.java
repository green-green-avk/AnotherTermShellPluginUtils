package green_green_avk.anothertermshellpluginutils;

import androidx.annotation.Nullable;

/**
 * The main purpose of this class is to wait for conditions from different sources.
 *
 * @param <T>
 */
@SuppressWarnings("WeakerAccess,unused")
public class Waiter<T> {
    private volatile boolean isSet = false;
    protected volatile T value = null;

    protected boolean onSet() {
        return true;
    }

    @Nullable
    public final synchronized T get() throws InterruptedException {
        if (!isSet) wait();
        return value;
    }

    public final synchronized void set(@Nullable final T v) {
        value = v;
        isSet = onSet();
        if (isSet) notifyAll();
    }

    public final synchronized void reset() {
        isSet = false;
        value = null;
    }
}
