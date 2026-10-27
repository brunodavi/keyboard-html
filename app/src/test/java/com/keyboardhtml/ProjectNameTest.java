package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
        File projectRoot = findProjectRoot();
        properties = new Properties();
        InputStream in = null;
        try {
            in = new java.io.FileInputStream(new File(projectRoot, "gradle.properties"));
            properties.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        manifest = readFile(new File(projectRoot, "app/src/main/AndroidManifest.xml"));
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

    private static File findProjectRoot() {
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (dir != null && !new File(dir, "gradle.properties").exists()) {
            dir = dir.getParentFile();
        }
        if (dir == null) {
            throw new IllegalStateException("Could not locate gradle.properties");
        }
        return dir;
    }

    private static String readFile(File file) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return out.toString();
    }
}
