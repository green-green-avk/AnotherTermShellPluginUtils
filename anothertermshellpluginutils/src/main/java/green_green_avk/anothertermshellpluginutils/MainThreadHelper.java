package green_green_avk.anothertermshellpluginutils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public final class MainThreadHelper {
    private MainThreadHelper() {
    }

    public static <T> T run(@NonNull final Callable<T> callable)
            throws ExecutionException, InterruptedException {
        final Handler h = new Handler(Looper.getMainLooper());
        final FutureTask<T> t = new FutureTask<>(callable);
        h.post(new Runnable() {
            @Override
            public void run() {
                t.run();
            }
        });
        return t.get();
    }
}
