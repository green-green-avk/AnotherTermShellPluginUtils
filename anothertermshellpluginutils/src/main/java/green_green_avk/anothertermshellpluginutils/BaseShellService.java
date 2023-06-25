package green_green_avk.anothertermshellpluginutils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * The callee side plugin class.
 * To be inherited and exported as {@code green_green_avk.anothertermshellplugin.ShellService}.
 */
public abstract class BaseShellService extends Service {
    @Nullable
    private static byte[][] createByteArrayArray(@NonNull final Parcel parcel) {
        final int N = parcel.readInt();
        if (N < 0)
            return null;
        final byte[][] r = new byte[N][];
        for (int i = 0; i < N; i++) {
            r[i] = parcel.createByteArray();
        }
        return r;
    }

    @Nullable
    private static ParcelFileDescriptor[] createFileDescriptorArray(@NonNull final Parcel parcel) {
        final int N = parcel.readInt();
        if (N < 0)
            return null;
        final ParcelFileDescriptor[] fds = new ParcelFileDescriptor[N];
        for (int i = 0; i < N; i++) {
            fds[i] = parcel.readFileDescriptor();
        }
        return fds;
    }

    private static final class ExecutionContextImpl implements ExecutionContext {
        @NonNull
        private final Context ctx;
        @NonNull
        private final ParcelFileDescriptor sigFd;

        private ExecutionContextImpl(@NonNull final Context context,
                                     @NonNull final ParcelFileDescriptor sigFd) {
            this.ctx = context.getApplicationContext();
            this.sigFd = sigFd;
        }

        @Override
        public boolean verify(@NonNull final Set<String> trusted) {
            return Auth.verify(ctx, Binder.getCallingUid(), trusted);
        }

        private int readSignal(@NonNull final InputStream s) throws IOException {
            final DataInputStream ds = new DataInputStream(s);
            return ds.readInt();
        }

        @Override
        public int readSignalNoBlock() throws IOException {
            final InputStream s = new FileInputStream(sigFd.getFileDescriptor());
            try {
                if (s.available() < Integer.BYTES)
                    return Protocol.SIG_NONE;
                return readSignal(s);
            } finally {
                try {
                    s.close();
                } catch (final Throwable ignored) {
                }
            }
        }

        @Override
        public int readSignal() throws IOException {
            final InputStream s = new FileInputStream(sigFd.getFileDescriptor());
            try {
                return readSignal(s);
            } finally {
                try {
                    s.close();
                } catch (final Throwable ignored) {
                }
            }
        }
    }

    @Override
    @Nullable
    public IBinder onBind(final Intent intent) {
        return new ShellBinder();
    }

    private final class ShellBinder extends Binder {
        @SuppressLint("Recycle") // Correct
        @Override
        protected boolean onTransact(final int code,
                                     @NonNull final Parcel data, @Nullable Parcel reply,
                                     final int flags) {
            switch (code) {
                case Protocol.CODE_PROTO: {
                    if (reply == null)
                        return false;
                    reply.writeInt(1);
                    return true;
                }
                case Protocol.CODE_META: {
                    if (reply == null)
                        return false;
                    reply.writeBundle(onMeta());
                    return true;
                }
                case Protocol.CODE_EXEC: {
                    final int ret;
                    ParcelFileDescriptor statFd = null;
                    ParcelFileDescriptor sigFd = null;
                    ParcelFileDescriptor[] fds = null;
                    try {
                        try {
                            final byte[][] args = createByteArrayArray(data);
                            if (args == null)
                                return false;
                            fds = createFileDescriptorArray(data);
                            if (fds == null)
                                return false;
                            sigFd = data.readFileDescriptor();
                            if (sigFd == null)
                                return false;
                            if (reply == null) { // Async mode
                                statFd = data.readFileDescriptor();
                                if (statFd == null)
                                    return false;
                                reply = Parcel.obtain();
                            }
                            ret = onExec(
                                    new ExecutionContextImpl(BaseShellService.this, sigFd),
                                    args, fds
                            );
                        } catch (final Exception e) {
                            if (reply == null)
                                return false;
                            reply.writeException(e);
                            return true;
                        }
                        reply.writeNoException();
                        reply.writeInt(ret);
                    } finally {
                        if (statFd != null) {
                            if (reply != null) { // Async mode
                                final OutputStream statOutput =
                                        new FileOutputStream(statFd.getFileDescriptor());
                                try {
                                    statOutput.write(reply.marshall());
                                } catch (final IOException ignored) {
                                } finally {
                                    try {
                                        statOutput.close();
                                    } catch (final Throwable ignored) {
                                    }
                                }
                                reply.recycle();
                            }
                            Utils.closeNoError(statFd);
                        }
                        if (sigFd != null)
                            Utils.closeNoError(sigFd);
                        if (fds != null)
                            for (final ParcelFileDescriptor fd : fds)
                                if (fd != null)
                                    Utils.closeNoError(fd);
                    }
                    return true;
                }
                default:
                    return false;
            }
        }
    }

    /**
     * To be overridden by an actual command handler.
     * It's supposed to return ASAP on {@link Protocol#SIG_FINALIZE} signal.
     *
     * @param execCtx An execution context provides caller verification
     *                and signal handling functions.
     * @param args    Arguments from the shell. In case of termsh:
     *                {@code termsh plugin <pkg_name> <args>}
     * @param fds     Usually {stdin, stdout, stderr}.
     * @return An exit status to be returned to the shell.
     */
    protected abstract int onExec(@NonNull ExecutionContext execCtx,
                                  @NonNull byte[][] args, @NonNull ParcelFileDescriptor[] fds);

    /**
     * To be overridden.
     *
     * @return The plugin metadata.
     */
    @Nullable
    protected Bundle onMeta() {
        return null;
    }
}
