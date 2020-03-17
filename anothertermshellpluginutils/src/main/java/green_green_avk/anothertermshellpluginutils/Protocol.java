package green_green_avk.anothertermshellpluginutils;

import android.os.IBinder;

public final class Protocol {
    private Protocol() {
    }

    public static final int SIG_NONE = -1;
    public static final int SIG_FINALIZE = 0;

    static final int CODE_PROTO = IBinder.FIRST_CALL_TRANSACTION;
    static final int CODE_META = IBinder.FIRST_CALL_TRANSACTION + 1;
    static final int CODE_EXEC = IBinder.FIRST_CALL_TRANSACTION + 2;
}
