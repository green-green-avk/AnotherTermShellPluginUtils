package green_green_avk.anothertermshellpluginutils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@SuppressWarnings("unused")
public final class MainThreadHelper {
    private MainThreadHelper() {
    }

    /**
     * A way to synchronously post a function to the main thread for a result.
     *
     * @param callable A function to run.
     * @return The function result.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static <T> T run(@NonNull final Callable<T> callable)
            throws ExecutionException, InterruptedException {
        final Handler h = new Handler(Looper.getMainLooper());
        final FutureTask<T> t = new FutureTask<>(callable);
        h.post(t);
        return t.get();
    }
}
