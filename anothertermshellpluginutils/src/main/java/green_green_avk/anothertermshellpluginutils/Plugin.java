package green_green_avk.anothertermshellpluginutils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The caller side plugin communication class.
 */
public final class Plugin {

    /**
     * The default time out for communication operations.
     */
    public static int timeout = 3000;

    private static final Set<WeakReference<Plugin>> stalled =
            Collections.newSetFromMap(new WeakHashMap<WeakReference<Plugin>, Boolean>());

    /**
     * Alas, we can't simply kill a plugin service thread,
     * call it to track the broken or malicious ones...
     */
    public static boolean isStalled(@NonNull final String pkgName) {
        for (final WeakReference<Plugin> r : stalled) {
            final Plugin p = r.get();
            if (p == null) continue;
            if (p.getPackageName().equals(pkgName)) return true;
        }
        return false;
    }

    /**
     * Returns a plugin service component name by package name,
     * effectively checking if an application is a plugin.
     *
     * @param ctx     Some context.
     * @param pkgName Package name.
     * @return Plugin service component name or null if not a plugin.
     */
    @Nullable
    public static ComponentName getComponent(@NonNull final Context ctx,
                                             @NonNull final String pkgName) {
        final PackageManager pm = ctx.getPackageManager();
        final ComponentName cm = new ComponentName(pkgName, pkgName + ".ShellService");
        try {
            final ServiceInfo si = pm.getServiceInfo(cm, 0);
            if (!si.enabled || !si.exported) return null;
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
        return cm;
    }

    /**
     * Creates a plugin communication instance.
     * Must be finalized by {@link #unbind()}.
     *
     * @param ctx        Some context.
     * @param pluginComp A plugin component name.
     * @return The plugin communication instance.
     * @throws IOException
     */
    @NonNull
    public static Plugin bind(@NonNull Context ctx,
                              @NonNull final ComponentName pluginComp)
            throws IOException {
        final ParcelFileDescriptor[] sigFd = ParcelFileDescriptor.createPipe();
        final Intent intent = new Intent().setComponent(pluginComp);
        ctx = ctx.getApplicationContext();
        final Plugin plugin = new Plugin(ctx, pluginComp, sigFd[0]);
        final boolean isBound;
        try {
            isBound = ctx.bindService(intent, plugin.conn, Context.BIND_AUTO_CREATE);
        } catch (final SecurityException e) {
            throw new IOException(e.getMessage());
        }
        if (!isBound)
            throw new IOException("Can't bind the plugin service");
        plugin.protocol = plugin.getProto();
        if (plugin.protocol != 1)
            throw new IOException("Unknown plugin protocol version: " + plugin.protocol);
        return plugin;
    }

    @NonNull
    private final ComponentName componentName;

    /**
     * @return The plugin's package name.
     */
    @NonNull
    public String getPackageName() {
        return componentName.getPackageName();
    }

    private int protocol = 0;

    public static final class Meta {
        /**
         * Info from plugin
         */
        @NonNull
        public final Bundle data;

        private Meta(@Nullable final Bundle data) {
            this.data = data == null ? Bundle.EMPTY : data;
        }
    }

    private Meta meta = null;

    private Plugin(@NonNull final Context context, @NonNull final ComponentName cn,
                   @NonNull final ParcelFileDescriptor sigFd) {
        this.ctx = context;
        this.componentName = cn;
        this.sigFd = sigFd;
    }

    private final Object lock = new Object();

    @NonNull
    private final Context ctx;
    @Nullable
    private volatile IBinder binder = null;
    @NonNull
    private final ParcelFileDescriptor sigFd;

    private final class ShellSrvConn implements ServiceConnection {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            synchronized (lock) {
                binder = service;
                lock.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            synchronized (lock) {
                binder = null;
            }
        }
    }

    private final ShellSrvConn conn = new ShellSrvConn();

    @NonNull
    private IBinder getBinder() throws InterruptedException, IOException {
        IBinder binder;
        synchronized (lock) {
            if ((binder = this.binder) == null) {
                lock.wait(timeout);
                if ((binder = this.binder) == null)
                    throw new IOException("Plugin response timed out");
            }
        }
        return binder;
    }

    /**
     * Finally unbinds.
     */
    public void unbind() {
        ctx.unbindService(conn);
    }

    /**
     * Sends a signal to the callee.
     *
     * @param signal A signal number.
     *               {@link Protocol#SIG_FINALIZE} is supposed to be used to request return ASAP.
     */
    public void signal(final int signal) {
        final DataOutputStream output =
                new DataOutputStream(new FileOutputStream(sigFd.getFileDescriptor()));
        try {
            output.writeInt(signal);
        } catch (final IOException ignored) {
        }
        if (signal == Protocol.SIG_FINALIZE) {
            execToken = beginTimedBlock();
        }
    }

    private final Handler hTimeout = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            stalled.add((WeakReference<Plugin>) msg.obj);
            return true;
        }
    });

    @NonNull
    private WeakReference<Plugin> beginTimedBlock() {
        final WeakReference<Plugin> obj = new WeakReference<>(this);
        hTimeout.sendMessageDelayed(Message.obtain(null, 0, obj), timeout);
        return obj;
    }

    private void endTimedBlock(@Nullable final WeakReference<Plugin> obj) {
        if (obj == null) return;
        hTimeout.removeMessages(0, obj);
    }

    private static void writeByteArrayArray(@NonNull final Parcel parcel,
                                            @NonNull final byte[][] v) {
        parcel.writeInt(v.length);
        for (final byte[] a : v)
            parcel.writeByteArray(a);
    }

    private static void writeFileDescriptorArray(@NonNull final Parcel parcel,
                                                 @NonNull final FileDescriptor[] v) {
        parcel.writeInt(v.length);
        for (final FileDescriptor f : v)
            parcel.writeFileDescriptor(f);
    }

    private int getProto() throws IOException {
        final IBinder binder;
        try {
            binder = getBinder();
        } catch (final InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        final Parcel data = Parcel.obtain();
        final Parcel reply = Parcel.obtain();
        final WeakReference<Plugin> token = beginTimedBlock();
        try {
            final boolean r;
            try {
                r = binder.transact(Protocol.CODE_PROTO, data, reply, 0);
            } catch (final RemoteException e) {
                throw new IOException(e.getMessage());
            }
            if (!r)
                throw new IOException("Transaction format error");
            return reply.readInt();
        } finally {
            endTimedBlock(token);
            data.recycle();
            reply.recycle();
        }
    }

    /**
     * @return The plugin metadata.
     * @throws IOException
     */
    @NonNull
    public Meta getMeta() throws IOException {
        if (meta != null) return meta;
        final IBinder binder;
        try {
            binder = getBinder();
        } catch (final InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        final Parcel data = Parcel.obtain();
        final Parcel reply = Parcel.obtain();
        final WeakReference<Plugin> token = beginTimedBlock();
        try {
            final boolean r;
            try {
                r = binder.transact(Protocol.CODE_META, data, reply, 0);
            } catch (final RemoteException e) {
                throw new IOException(e.getMessage());
            }
            if (!r)
                throw new IOException("Transaction format error");
            return new Meta(reply.readBundle());
        } finally {
            endTimedBlock(token);
            data.recycle();
            reply.recycle();
        }
    }

    private WeakReference<Plugin> execToken = null;

    /**
     * Execute a plugin.
     * <p class="note">Note: in the current implementation,
     * it's impossible to forcefully cancel this method:
     * there is a chance that a plugin will block it forever
     * and never close passed descriptors on its side.</p>
     *
     * @param args Arguments, supposedly from a shell.
     * @param fds  Usually {stdin, stdout, stderr}.
     * @return Exit status, supposedly for a shell.
     * @throws IOException If plugin fails.
     */
    public int exec(@NonNull final byte[][] args, @NonNull final FileDescriptor[] fds)
            throws IOException {
        final IBinder binder;
        try {
            binder = getBinder();
        } catch (final InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        final Parcel data = Parcel.obtain();
        final Parcel reply = Parcel.obtain();
        try {
            writeByteArrayArray(data, args);
            writeFileDescriptorArray(data, fds);
            data.writeFileDescriptor(sigFd.getFileDescriptor());
            final boolean r;
            try {
                r = binder.transact(Protocol.CODE_EXEC, data, reply, 0);
            } catch (final RemoteException e) {
                throw new IOException(e.getMessage());
            }
            if (!r)
                throw new IOException("Transaction format error");
            try {
                reply.readException();
            } catch (final Exception e) {
                throw new IOException(e.getMessage());
            }
            return reply.readInt();
        } finally {
            endTimedBlock(execToken);
            execToken = null;
            data.recycle();
            reply.recycle();
        }
    }
}
