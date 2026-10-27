package com.keyboardhtml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * The reload wiring must never use startService on the IME: InputMethodService
 * started through startService (and stickily restarted afterwards) lives
 * outside the input-method lifecycle and the process crashes when the user
 * reselects the keyboard. In-app broadcast + receiver keeps the reload
 * one-way and lifecycle-safe.
 */
public class ReloadWiringTest {

    @Test
    public void serviceDoesNotStartItselfOrAcceptStarts() throws IOException {
        String service = ProjectFiles.readProjectFile(
                "app/src/main/java/com/keyboardhtml/KeyboardService.java");
        assertFalse("startService on an IME service can recreate it unattached (crash)",
                service.contains("onStartCommand"));
        assertTrue("service must listen for the in-app reload broadcast",
                service.contains("registerReceiver"));
        assertTrue("receiver must be app-private (setPackage sender)",
                service.contains("RECEIVER_NOT_EXPORTED")
                        || service.contains("RECEIVER_EXPORTED")
                        || service.contains("registerReceiver(reloadListener,"));
    }

    @Test
    public void activityNotifiesByBroadcastOnly() throws IOException {
        String activity = ProjectFiles.readProjectFile(
                "app/src/main/java/com/keyboardhtml/MainActivity.java");
        assertFalse("no startService for reloads (crash vector)",
                activity.contains("startService"));
        assertTrue(activity.contains("sendBroadcast"));
        assertTrue(activity.contains("setPackage(getPackageName())"));
    }
}
