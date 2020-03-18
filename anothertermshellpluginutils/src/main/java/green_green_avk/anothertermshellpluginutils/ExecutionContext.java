package green_green_avk.anothertermshellpluginutils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Set;

public interface ExecutionContext {

    /**
     * Verify a caller against a specified set of <em>signature / package name</em> pairs.
     *
     * @param trusted Set to check against.
     * @return {@link true} if identity matches.
     * @see BaseShellService#trustedClients
     * @see BaseShellService#trustedClientsDebug
     */
    boolean verify(@NonNull final Set<String> trusted);

    /**
     * @return A signal from the caller or {@link Protocol#SIG_NONE} if there is no signal.
     * @throws IOException If caller's connection lost.
     */
    int readSignalNoBlock() throws IOException;

    /**
     * @return A signal from the caller signal or blocks if there is no signal to read.
     * @throws IOException If caller's connection lost.
     */
    int readSignal() throws IOException;
}
