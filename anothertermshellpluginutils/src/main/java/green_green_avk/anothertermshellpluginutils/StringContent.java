package green_green_avk.anothertermshellpluginutils;

import androidx.annotation.NonNull;

public final class StringContent {
    @NonNull
    public final String text;
    public final int type;

    StringContent(@NonNull final String text, final int type) {
        this.text = text;
        this.type = type;
    }
}
