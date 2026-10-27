package com.sinst.util;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Centralized, managed concurrency and worker execution utility for SINST Swing desktop UI.
 * Prevents unmanaged thread creation, enforces Event Dispatch Thread updates,
 * manages cursor and button loading states, and cancels superseded tasks to avoid stale UI state.
 */
public class AppExecutor {

    private static final int CORE_THREADS = 4;
    private static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(
        CORE_THREADS,
        CORE_THREADS * 2,
        60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "sinst-worker-thread-" + count.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        },
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private static final ConcurrentHashMap<String, SwingWorker<?, ?>> ACTIVE_WORKERS = new ConcurrentHashMap<>();

    private AppExecutor() {}

    /**
     * Executes a background task using SwingWorker with automated EDT callback dispatch,
     * loading state management (cursor & action components disabling), and cancellation tracking.
     *
     * @param taskKey           Unique task key for cancellation (if null, no cancellation tracking)
     * @param backgroundTask    Callable returning computed result on a background worker thread
     * @param onSuccess         Consumer executed on Event Dispatch Thread on successful completion
     * @param onError           Consumer executed on Event Dispatch Thread on failure
     * @param parentComponent   Parent UI component on which to show wait cursor (optional)
     * @param disableComponents Components to temporarily disable while background task runs
     * @param <T>               Result type
     * @return The created SwingWorker instance
     */
    public static <T> SwingWorker<T, Void> runWorker(
            String taskKey,
            Callable<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError,
            Component parentComponent,
            Component... disableComponents) {

        // If a task with this key is already running, cancel it to prevent stale UI state
        if (taskKey != null) {
            cancelTask(taskKey);
        }

        // Apply loading state on EDT before starting
        if (parentComponent != null) {
            parentComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        if (disableComponents != null) {
            for (Component c : disableComponents) {
                if (c != null) {
                    c.setEnabled(false);
                }
            }
        }

        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.call();
            }

            @Override
            protected void done() {
                // Restore loading states on EDT
                try {
                    if (taskKey != null) {
                        ACTIVE_WORKERS.remove(taskKey, this);
                    }
                } finally {
                    if (parentComponent != null) {
                        parentComponent.setCursor(Cursor.getDefaultCursor());
                    }
                    if (disableComponents != null) {
                        for (Component c : disableComponents) {
                            if (c != null) {
                                c.setEnabled(true);
                            }
                        }
                    }
                }

                // If cancelled, discard result to avoid stale UI state
                if (isCancelled()) {
                    return;
                }

                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (CancellationException ce) {
                    // Ignored on deliberate cancellation
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
                    if (onError != null) {
                        onError.accept(cause);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    if (onError != null) {
                        onError.accept(ie);
                    }
                } catch (Throwable t) {
                    if (onError != null) {
                        onError.accept(t);
                    }
                }
            }
        };

        if (taskKey != null) {
            ACTIVE_WORKERS.put(taskKey, worker);
        }

        worker.execute();
        return worker;
    }

    /**
     * Overloaded helper allowing parentComponent to be passed before onSuccess and onError consumers.
     */
    public static <T> SwingWorker<T, Void> runWorker(
            String taskKey,
            Callable<T> backgroundTask,
            Component parentComponent,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError,
            Component... disableComponents) {
        return runWorker(taskKey, backgroundTask, onSuccess, onError, parentComponent, disableComponents);
    }

    /**
     * Cancels any active task associated with the given key.
     */
    public static void cancelTask(String taskKey) {
        if (taskKey == null) return;
        SwingWorker<?, ?> existing = ACTIVE_WORKERS.remove(taskKey);
        if (existing != null && !existing.isDone()) {
            existing.cancel(true);
        }
    }

    /**
     * Cancels all active background tasks.
     */
    public static void cancelAllTasks() {
        for (String key : ACTIVE_WORKERS.keySet()) {
            cancelTask(key);
        }
    }

    /**
     * Submits a Runnable directly to the managed thread pool without SwingWorker overhead.
     */
    public static Future<?> submit(Runnable task) {
        return THREAD_POOL.submit(task);
    }

    /**
     * Gracefully shuts down the background thread pool and cancels active tasks.
     */
    public static void shutdown() {
        cancelAllTasks();
        THREAD_POOL.shutdown();
        try {
            if (!THREAD_POOL.awaitTermination(2, TimeUnit.SECONDS)) {
                THREAD_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            THREAD_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if the current thread is the Swing Event Dispatch Thread.
     */
    public static boolean isEdt() {
        return SwingUtilities.isEventDispatchThread();
    }

    /**
     * Returns the number of currently active registered tasks.
     */
    public static int getActiveTaskCount() {
        return ACTIVE_WORKERS.size();
    }
}
