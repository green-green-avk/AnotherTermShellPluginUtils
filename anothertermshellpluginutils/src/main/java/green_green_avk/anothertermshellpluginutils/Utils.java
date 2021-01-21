package green_green_avk.anothertermshellpluginutils;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("WeakerAccess,unused")
public final class Utils {
    private Utils() {
    }

    public static final Charset UTF8 = Build.VERSION.SDK_INT < 19
            ? Charset.forName("UTF8") : StandardCharsets.UTF_8;

    @NonNull
    public static String fromUTF8(@NonNull final byte[] buf) {
        return new String(buf, UTF8);
    }

    @NonNull
    public static byte[] toUTF8(@NonNull final String v) {
        return v.getBytes(UTF8);
    }

    public static void write(@NonNull final OutputStream to, @NonNull final String v) {
        try {
            to.write(toUTF8(v));
        } catch (final IOException ignored) {
        }
    }

    public static void write(@NonNull final FileChannel to, @NonNull final String v) {
        try {
            to.write(ByteBuffer.wrap(toUTF8(v)));
        } catch (final IOException ignored) {
        }
    }

    public static void closeNoError(@NonNull final ParcelFileDescriptor v) {
        try {
            v.close();
        } catch (final IOException ignored) {
        }
    }

    /**
     * Binds a {@link Waiter} to receive signal notifications.
     *
     * @param ec     - a notifications source.
     * @param waiter - a {@link Waiter} to bind.
     * @deprecated
     */
    @Deprecated
    public static void bindSignal(@NonNull final ExecutionContext ec,
                                  @NonNull final Waiter<Object> waiter) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        waiter.set(new Signal(ec.readSignal()));
                    } catch (final IOException e) {
                        waiter.set(new Signal(Protocol.SIG_FINALIZE));
                        return; // When a client disconnects and closes the signal pipe
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * Binds an {@link OnSignal} to receive signal notifications.
     *
     * @param ec       - a notifications source.
     * @param onSignal - an {@link OnSignal} listener to bind.
     * @param looper   - where to execute the listener.
     */
    public static void bindSignal(@NonNull final ExecutionContext ec,
                                  @NonNull final OnSignal onSignal,
                                  @NonNull final Looper looper) {
        final Handler h = new Handler(looper);
        final Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    Signal signal = new Signal(Protocol.SIG_FINALIZE);
                    try {
                        signal = new Signal(ec.readSignal());
                    } catch (final IOException e) {
                        return; // When a client disconnects and closes the signal pipe
                    } finally {
                        final Signal _signal = signal;
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                onSignal.onSignal(_signal);
                            }
                        });
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
}
