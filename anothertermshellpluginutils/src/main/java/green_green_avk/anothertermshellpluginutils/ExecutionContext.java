package green_green_avk.anothertermshellpluginutils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Set;

public interface ExecutionContext {
    boolean verify(@NonNull final Set<String> trusted);

    int peekSignal() throws IOException;

    int readSignal() throws IOException;
}
