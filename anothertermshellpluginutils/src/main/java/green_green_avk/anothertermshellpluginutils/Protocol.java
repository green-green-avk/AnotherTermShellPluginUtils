package green_green_avk.anothertermshellpluginutils;

import android.os.IBinder;

public final class Protocol {
    private Protocol() {
    }

    public static final int SIG_NONE = -1;
    /**
     * Requests exit ASAP
     */
    public static final int SIG_FINALIZE = 0;

    /**
     * Plain text
     */
    public static final int STRING_CONTENT_TYPE_PLAIN = 0;
    /**
     * HTML
     */
    public static final int STRING_CONTENT_TYPE_HTML = 1;
    /**
     * HTML as of <em>androidx.core.text.HtmlCompat</em>
     */
    public static final int STRING_CONTENT_TYPE_HTML_COMPAT_ANDROID = 2;
    /**
     * Another Term XML info
     */
    public static final int STRING_CONTENT_TYPE_XML_AT = 3;

    /**
     * Info page resource Id
     */
    public static final String META_KEY_INFO_RES_ID = "infoResId";
    /**
     * Info page resource type
     */
    public static final String META_KEY_INFO_RES_TYPE = "infoResType";

    static final int CODE_PROTO = IBinder.FIRST_CALL_TRANSACTION;
    static final int CODE_META = IBinder.FIRST_CALL_TRANSACTION + 1;
    static final int CODE_EXEC = IBinder.FIRST_CALL_TRANSACTION + 2;

    /**
     * For third-party use
     */
    public static final int CODE_CUSTOM_BEGIN = IBinder.FIRST_CALL_TRANSACTION + 2;
}
