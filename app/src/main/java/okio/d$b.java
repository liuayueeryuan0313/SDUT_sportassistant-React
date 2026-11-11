package okio;

import java.lang.reflect.Field;

/** Verifier-safe watchdog with the original idle shutdown behavior restored. */
final class d$b extends Thread {
    private static final Field QUEUE_HEAD;

    static {
        try {
            // Pinned original-third-party.jar: okio.d.j is the queue sentinel.
            QUEUE_HEAD = d.class.getDeclaredField("j");
            QUEUE_HEAD.setAccessible(true);
        } catch (NoSuchFieldException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    public d$b() {
        super("Okio Watchdog");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                d timedOut;
                synchronized (d.class) {
                    timedOut = d.k.c();
                    if (timedOut == null) continue;
                    if (timedOut == QUEUE_HEAD.get(null)) {
                        QUEUE_HEAD.set(null, null);
                        return;
                    }
                }
                timedOut.x();
            } catch (InterruptedException ignored) {
                // Retry waiting after interruption, as in the library.
            } catch (IllegalAccessException error) {
                throw new IllegalStateException("Cannot access Okio timeout queue", error);
            }
        }
    }
}
