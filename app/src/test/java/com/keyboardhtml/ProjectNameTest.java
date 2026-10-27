package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Guards the project name wiring: the display name and package must be
 * defined in gradle.properties and referenced by the Android manifest
 * instead of being hardcoded.
 */
public class ProjectNameTest {

    private static final String PROJECT_PACKAGE = "com.keyboardhtml";
    private static final String DISPLAY_NAME = "Keyboard HTML";

    private Properties properties;
    private String manifest;

    @Before
    public void loadProjectFiles() throws IOException {
        properties = ProjectFiles.loadGradleProperties();
        manifest = ProjectFiles.readProjectFile("app/src/main/AndroidManifest.xml");
    }

    @Test
    public void appPackageMatchesProjectName() {
        assertEquals(PROJECT_PACKAGE, properties.getProperty("APP_PACKAGE"));
    }

    @Test
    public void appDisplaySimpleNameIsDefinedInProperties() {
        assertEquals(DISPLAY_NAME, properties.getProperty("APP_NAME"));
    }

    @Test
    public void manifestLabelComesFromProperties() {
        assertTrue(
                "Manifest label must reference the APP_NAME placeholder",
                manifest.contains("android:label=\"${APP_NAME}\""));
    }
}
