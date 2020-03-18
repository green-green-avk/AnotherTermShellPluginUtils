package green_green_avk.anothertermshellpluginutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import androidx.annotation.NonNull;

import java.util.Set;

public final class Auth {
    private Auth() {
    }

    public static String getFingerprint(@NonNull final Signature s) {
        return s.toCharsString();
    }

    public static boolean verify(@NonNull final Context context, final int uid,
                                 @NonNull final Set<String> trusted) {
        final PackageManager pm = context.getPackageManager();
        final String[] pkgs = pm.getPackagesForUid(uid);
        if (pkgs == null || pkgs.length <= 0) return false;
        for (final String pkg : pkgs)
            if (!verify(context, pkg, trusted)) return false;
        return true;
    }

    @SuppressLint("PackageManagerGetSignatures") // So, check'em all...
    public static boolean verify(@NonNull final Context context, @NonNull final String pkgName,
                                 @NonNull final Set<String> trusted) {
        final PackageManager pm = context.getPackageManager();
        final PackageInfo info;
        try {
            info = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
        if (info.signatures == null || info.signatures.length <= 0) return false;
        for (final Signature s : info.signatures)
            if (!trusted.contains(getFingerprint(s) + ":" + pkgName)) return false;
        return true;
    }
}
