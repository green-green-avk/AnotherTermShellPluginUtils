package green_green_avk.anothertermshellpluginutils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseShellService extends Service {

    /**
     * Format: <em></>&lt;signature&gt;:&lt;package_name&gt;</em>
     * Signature as created by {@link Auth#getFingerprint}.
     */
    public static final Set<String> trustedClients = new HashSet<>();
    public static final Set<String> trustedClientsDebug = new HashSet<>();

    static {
        // Another Term author's own
        trustedClients.add("3082036130820249a003020102020421a3c842300d06092a864886f70d01010b05003061310b3009060355040613025553310b30090603550408130243413111300f0603550407130853616e204a6f7365310a3008060355040a13012d310a3008060355040b13012d311a301806035504031311416c656b73616e6472204b6973656c6576301e170d3138313230313037353530325a170d3433313132353037353530325a3061310b3009060355040613025553310b30090603550408130243413111300f0603550407130853616e204a6f7365310a3008060355040a13012d310a3008060355040b13012d311a301806035504031311416c656b73616e6472204b6973656c657630820122300d06092a864886f70d01010105000382010f003082010a02820101009eb8bad5f87c06f1c90639c732b51607d4e5d9c2cd326c8102a16bc89b7677f1d75899e0b198fa5b1e1fabb1457265d95ee051bc7cf3f1202de87eddadeb7af1f1e0ac626c91e2e31efe2bea0aaea751feb26ecd87732caf18d4eaad4165fb6a1d5a178bcf45682976cd9fd5716cd02fb22f7a74c1ae0d78f5b466f60aec0afddc63a2eaf5d0b32f4b00246438d7a08a61cb64623e811e1bcaa25d0d71ca06781e1159d470d263bfe23adb12a29d5f86b700fd02d9b64800c4a459862565f77344b98a63904538c6f2bc9581d11253b8cb0bffee12db729faaeb9bd6fad9444a858e54b58701b0a0015254e9c070ba04df16a9aded369ff41853bd2044396e410203010001a321301f301d0603551d0e04160414b1f88b0be43c2c22094255d8ce021898135279b1300d06092a864886f70d01010b050003820101001d475c6af413cae5ad30ec0a1eeb116c082ae887726e6dbf94e091f3757b6ad8f54b1c703750f43cc69c0944f6f7f5d444ad047be779c4424bab9c30ee2b78ebfb67571324e3627f8bebae4de1939ab69b4a60a192beafefed962813cd7f6c169285e28f217d2ecd3c71fec90b0304f476a00ed1a4d2a2e37c0a890320b6d68e4a1164c66490804be3367faca764603e51c9965a39dc0599215f61558469da57667a108f3ccc73e41c52576ff65bebb1bff85588922d43d8f9ef1ecb00aa3a9cc41df0ad9adb81c99c0be2bc9b33b45a61f481220ffba4fe056cd15528aea11425fa36bf43d5f9276399152cac9da78026da2ea6b619c6da46c201b6397c3edb:green_green_avk.anotherterm");
        // Another Term Google play
        trustedClients.add("308205873082036fa003020102021500f86f28b439e5fe7c66ac836c3870b71e184ae4ff300d06092a864886f70d01010b05003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964301e170d3139303332353035303932345a170d3439303332353035303932345a3074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f696430820222300d06092a864886f70d01010105000382020f003082020a0282020100a28b4330dff073d7f8ba8ac57e306d0124c7c3890f9889c4cb4d96a31a599abcfa677b06dd140cd7e1523ec16ade38f78f817df17030c4000f6d66e40c587d1cd7b11656f279cacf9399ab52603c8a9c9302041707ba8fb59572995fccc5538a0f15683f33d37312537487cc686dd743fc0fa9ec35cb2e9497805ddcdf3665dcbaceec4a4d1f928fca52ba5e24e9031a45b55a857edc738682af25e1da18c9fa86e8a915bee4f7824b9bd99683d5eb1449566a1bf7a93e846cc85026f4d8584127828ff62f2633be11a65a9ddf9dbbf9e3fc242742942d4c1e489cae472e80d772ee82a462282a8fec3e7668510bac5769e858b0026bac5104ce8d8929be9707dd91106935b78c256ad3b623cad3203127c6b338436730a55bcb0556424b5f7fb9919ad69b14ce2b0b0b91ab01db45a0d5d16c4045341fd5c129ccbf1ce43c1b41e2bc01da7511b80e43b10ae579e3025cfc578e8d724e55a6f45d63d7456149447c862e181a577d0a42acfce709c15206def9b1e4f98029aae44b57ce8f58b4e085946ccb8525e866ec3e2967c6a1aea738f340bbf0951fc5c5e6384b853c5d7c9ff1694cdcc2988fd367e9415d000b909a89cbd808e89dd114c0d10373f8a9e208583aabb7289ec7770f4b0a5d608531b065f092943c4efd517b9408e138803183f63c789be8c376b0b1cf02130529755f3a6c53a434ecf66a7176732a5cdd0203010001a310300e300c0603551d13040530030101ff300d06092a864886f70d01010b050003820201003994c5ee1e02245b3b893feed6cbfd3fdc70a8c47285023c347550747d107b3153812c7a3039ef94638301de7e67585f44268fd9ed93b9546be47eba66fcb4db409e352afe0cb64072de5a26a0f8c2763508a92434f86f9df4fe18c5138aa865eba422f3530b45c83508d4b7b5c9da94cd7a773d6171c7ab20100d5e9f1263e33fbd478a8544faf46fa4256a752771070fc89cda2af92c0e45a8f150615ec4c9e4853fc7c8718242e2183f449ac534b7801c1f5f0669db3f67160e7e84ec54281aa6f9051164d2b7bde36693e6a0f69fa8f8150cc092d2bef7e56851fd055047f2b37f08931c2c84696f67188a46c2e38848834c8573c00d15af166085b49891d25eb7ca32653e66aff7c218fea90a6b12e3ae74abfa121160fcd221456308f9a56efb19a7a29654745a601c63033b4ae4926cf79abee8e07df1b80921909f480a99172fd858ca2ea8abb17ddfa9134883897c300f3fcf3f3b0fdfc2288b7fc6151e795b2fdddf270072e70fe9ecebcb70eeae71d7a0776cd4e59668329228de10470e5bc9dd13601356a549e5d20cdde9cc5e3ce879b603c0461778cbbd03d104180609819c39b8ea86bf47390856e8359a217a4dc1686f86aad635c80137ba2ca38fbe9c937474020c4231067e929165fe1ad75cdc14bc8ea97b4e307b7712c0929ec2beedcbcc11db26b143b08c0bc3fa569906b8df2c5884714e66d548d3:green_green_avk.anotherterm");
        trustedClientsDebug.addAll(trustedClients);
        // Default debug
        trustedClientsDebug.add("308201dd30820146020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3138313030313030303933315a170d3438303932333030303933315a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330819f300d06092a864886f70d010101050003818d003081890281810085933d27544c44697d1a8816cb5df4477182467eb08a8d8d3d7a29804eb27b903888955f5326a9f99e711b493085af51723f079aba72b3e4a44f9039e56bc066b2fec260b4c838e28a47cda0ce702bab9f32c2ed2f685f4c310df8f6644716f94495e671b0a35bf6134c62a3a7c15a35e0b92d21e1ea4379e578a27d36a2b0070203010001300d06092a864886f70d010105050003818100475831aeac8d96c18f5c6b6e6d41baebaa6a8a21897a6d845866e81f8156ab92b6453cba455151023711a2ab9a29feb04891bfa9a4bc7276048e84bc7ff5012b30ef7579662d00b572846d2bf602397867087f5129e5aeb6398c29dae2edc63662ed18a92e9bb94d9ada9a4fdc40ffd8c65e4ffc0aaafb2e044c8c951724a6bf:green_green_avk.anotherterm");
    }

    @Nullable
    private static byte[][] createByteArrayArray(@NonNull final Parcel parcel) {
        final int N = parcel.readInt();
        if (N < 0) return null;
        final byte[][] r = new byte[N][];
        for (int i = 0; i < N; i++) {
            r[i] = parcel.createByteArray();
        }
        return r;
    }

    @Nullable
    private static ParcelFileDescriptor[] createFileDescriptorArray(@NonNull final Parcel parcel) {
        final int N = parcel.readInt();
        if (N < 0) return null;
        final ParcelFileDescriptor[] fds = new ParcelFileDescriptor[N];
        for (int i = 0; i < N; i++) {
            fds[i] = parcel.readFileDescriptor();
        }
        return fds;
    }

    public static final class ExecutionContextImpl implements ExecutionContext {
        @NonNull
        private final Context ctx;
        @NonNull
        private final ParcelFileDescriptor sigFd;

        private ExecutionContextImpl(@NonNull final Context context,
                                     @NonNull final ParcelFileDescriptor sigFd) {
            this.ctx = context.getApplicationContext();
            this.sigFd = sigFd;
        }

        public boolean verify(@NonNull final Set<String> trusted) {
            return Auth.verify(ctx, Binder.getCallingUid(), trusted);
        }

        private int readSignal(@NonNull final InputStream s) throws IOException {
            final DataInputStream ds = new DataInputStream(s);
            return ds.readInt();
        }

        @Override
        public int peekSignal() throws IOException {
            final InputStream s = new FileInputStream(sigFd.getFileDescriptor());
            if (s.available() < 4) return Protocol.SIG_NONE;
            return readSignal(s);
        }

        @Override
        public int readSignal() throws IOException {
            return readSignal(new FileInputStream(sigFd.getFileDescriptor()));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return new ShellBinder();
    }

    private final class ShellBinder extends Binder {
        @Override
        protected boolean onTransact(final int code,
                                     @NonNull final Parcel data, @Nullable Parcel reply,
                                     final int flags) throws RemoteException {
            switch (code) {
                case Protocol.CODE_PROTO: {
                    if (reply == null) return false;
                    reply.writeInt(1);
                    return true;
                }
                case Protocol.CODE_META: {
                    return reply != null;
                    // Empty yet
                }
                case Protocol.CODE_EXEC: {
                    final int ret;
                    ParcelFileDescriptor statFd = null;
                    ParcelFileDescriptor sigFd = null;
                    ParcelFileDescriptor[] fds = null;
                    try {
                        try {
                            final byte[][] args = createByteArrayArray(data);
                            if (args == null) return false;
                            fds = createFileDescriptorArray(data);
                            if (fds == null) return false;
                            sigFd = data.readFileDescriptor();
                            if (sigFd == null) return false;
                            if (reply == null) { // Async mode
                                statFd = data.readFileDescriptor();
                                if (statFd == null) return false;
                                reply = Parcel.obtain();
                            }
                            ret = onExec(
                                    new ExecutionContextImpl(BaseShellService.this, sigFd),
                                    args, fds
                            );
                        } catch (final Exception e) {
                            if (reply == null) return false;
                            reply.writeException(e);
                            return true;
                        }
                        reply.writeNoException();
                        reply.writeInt(ret);
                    } finally {
                        if (statFd != null) { // Async mode
                            final OutputStream statOutput =
                                    new FileOutputStream(statFd.getFileDescriptor());
                            try {
                                statOutput.write(reply.marshall());
                            } catch (final IOException ignored) {
                            }
                            reply.recycle();
                            Utils.closeNoError(statFd);
                        }
                        if (sigFd != null) Utils.closeNoError(sigFd);
                        if (fds != null) for (final ParcelFileDescriptor fd : fds)
                            if (fd != null) Utils.closeNoError(fd);
                    }
                    return true;
                }
                default:
                    return false;
            }
        }
    }

    protected abstract int onExec(@NonNull ExecutionContext execCtx,
                                  @NonNull byte[][] args, @NonNull ParcelFileDescriptor[] fds);
}
