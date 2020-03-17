package green_green_avk.anothertermshellpluginutils;

import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
}
