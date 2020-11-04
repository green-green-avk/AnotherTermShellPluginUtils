package green_green_avk.anothertermshellpluginutils;

import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Purely helper class just to have an exit() function at the moment.
 */
@SuppressWarnings("unused")
public abstract class BaseMain {

    /**
     * To be thrown in
     * {@link BaseMain#main(ExecutionContext, byte[][], ParcelFileDescriptor[])}
     * to exit with a specified exit code and write a message (if not <b>null</b>)
     * to the <b>stderr</b>.
     */
    public static final class ExitException extends Error {
        private final int exitCode;

        public ExitException(final int exitCode, @Nullable final String message) {
            super(message);
            this.exitCode = exitCode;
        }

        public ExitException(final int exitCode, @Nullable final String message,
                             @Nullable final Throwable cause) {
            super(message, cause);
            this.exitCode = exitCode;
        }

        public ExitException(final int exitCode, @Nullable final Throwable cause) {
            super(cause);
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }

        @Override
        @NonNull
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * Finishes the plugin immediately.
     */
    public static void exit(final int exitCode) {
        throw new ExitException(exitCode, (String) null);
    }

    /**
     * To be overridden by an actual command handler.
     *
     * @see BaseShellService#onExec(ExecutionContext, byte[][], ParcelFileDescriptor[])
     */
    protected abstract int main(@NonNull ExecutionContext execCtx,
                                @NonNull byte[][] args, @NonNull ParcelFileDescriptor[] fds);

    /**
     * To be called from a
     * {@link BaseShellService#onExec(ExecutionContext, byte[][], ParcelFileDescriptor[])}
     * implementation.
     */
    public int exec(@NonNull final ExecutionContext execCtx,
                    @NonNull final byte[][] args, @NonNull final ParcelFileDescriptor[] fds) {
        try {
            return main(execCtx, args, fds);
        } catch (final ExitException e) {
            final String msg = e.getMessage();
            if (msg != null) {
                if (fds[2] == null)
                    throw new RuntimeException("No stderr");
                final OutputStream stderr =
                        new FileOutputStream(fds[2].getFileDescriptor());
                try {
                    stderr.write(Utils.toUTF8(msg + "\n"));
                } catch (final IOException ioException) {
                    throw new RuntimeException(ioException);
                } finally {
                    try {
                        stderr.close();
                    } catch (final Throwable ignored) {
                    }
                }
            }
            return e.getExitCode();
        }
    }
}
