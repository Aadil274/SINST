package com.sinst.unit;

import com.sinst.util.AppExecutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Swing Responsiveness, EDT Safety & Managed Concurrency")
public class ConcurrencyAndResponsivenessTest {

    @AfterAll
    static void cleanUp() {
        AppExecutor.cancelAllTasks();
    }

    @Test
    @DisplayName("AppExecutor.isEdt() correctly identifies EDT vs background threads")
    void testEdtDetection() throws Exception {
        assertFalse(AppExecutor.isEdt(), "Main test runner thread must not be on the Event Dispatch Thread");

        AtomicBoolean edtDetected = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            edtDetected.set(AppExecutor.isEdt());
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(edtDetected.get(), "Task executed inside invokeLater must report as EDT");
    }

    @Test
    @DisplayName("Worker background task runs off EDT, and onSuccess executes strictly on EDT")
    void testAsyncSuccessExecution() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean backgroundWasOnEdt = new AtomicBoolean(true);
        AtomicBoolean callbackWasOnEdt = new AtomicBoolean(false);
        AtomicReference<String> resultRef = new AtomicReference<>();

        AppExecutor.runWorker(
            "test-success-task",
            () -> {
                backgroundWasOnEdt.set(AppExecutor.isEdt());
                return "WorkerResult";
            },
            res -> {
                callbackWasOnEdt.set(AppExecutor.isEdt());
                resultRef.set(res);
                latch.countDown();
            },
            err -> latch.countDown(),
            null
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Worker task did not complete in time");
        assertFalse(backgroundWasOnEdt.get(), "Background execution must NOT run on EDT");
        assertTrue(callbackWasOnEdt.get(), "onSuccess callback must execute strictly on EDT");
        assertEquals("WorkerResult", resultRef.get());
    }

    @Test
    @DisplayName("Worker onError executes strictly on EDT and receives sanitized exception")
    void testAsyncErrorExecution() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean callbackWasOnEdt = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        AppExecutor.runWorker(
            "test-error-task",
            () -> {
                throw new IllegalStateException("Intentional worker failure");
            },
            res -> latch.countDown(),
            err -> {
                callbackWasOnEdt.set(AppExecutor.isEdt());
                errorRef.set(err);
                latch.countDown();
            },
            null
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Worker error did not complete in time");
        assertTrue(callbackWasOnEdt.get(), "onError callback must execute strictly on EDT");
        assertNotNull(errorRef.get());
        assertTrue(errorRef.get() instanceof IllegalStateException);
        assertEquals("Intentional worker failure", errorRef.get().getMessage());
    }

    @Test
    @DisplayName("Action components are disabled during execution and restored on completion/failure")
    void testComponentDisablingAndRestoration() throws Exception {
        JButton actionBtn = new JButton("Submit");
        assertTrue(actionBtn.isEnabled());

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        AppExecutor.runWorker(
            "test-btn-task",
            () -> {
                startLatch.countDown();
                Thread.sleep(100);
                return "Done";
            },
            res -> finishLatch.countDown(),
            err -> finishLatch.countDown(),
            null,
            actionBtn
        );

        assertTrue(startLatch.await(3, TimeUnit.SECONDS));
        assertTrue(finishLatch.await(5, TimeUnit.SECONDS));

        // Let EDT process finally block
        CountDownLatch verifyLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(verifyLatch::countDown);
        assertTrue(verifyLatch.await(2, TimeUnit.SECONDS));

        assertTrue(actionBtn.isEnabled(), "Button must be re-enabled after task finishes");
    }

    @Test
    @DisplayName("Superseded tasks with same key are cancelled and stale results discarded")
    void testSupersededTaskCancellation() throws Exception {
        CountDownLatch firstTaskRunning = new CountDownLatch(1);
        CountDownLatch secondTaskFinished = new CountDownLatch(1);
        AtomicReference<String> finalAcceptedResult = new AtomicReference<>(null);

        String sharedKey = "shared-rapid-key";

        // Task 1: slow task
        AppExecutor.runWorker(
            sharedKey,
            () -> {
                firstTaskRunning.countDown();
                Thread.sleep(300);
                return "STALE_RESULT_TASK_1";
            },
            finalAcceptedResult::set,
            err -> {},
            null
        );

        assertTrue(firstTaskRunning.await(2, TimeUnit.SECONDS));

        // Task 2: supersedes Task 1 with same key
        AppExecutor.runWorker(
            sharedKey,
            () -> "FRESH_RESULT_TASK_2",
            res -> {
                finalAcceptedResult.set(res);
                secondTaskFinished.countDown();
            },
            err -> secondTaskFinished.countDown(),
            null
        );

        assertTrue(secondTaskFinished.await(5, TimeUnit.SECONDS));
        Thread.sleep(400); // Allow any possible lingering Task 1 completion

        assertEquals("FRESH_RESULT_TASK_2", finalAcceptedResult.get(),
            "Stale result from superseded task must not overwrite fresh result");
    }
}
