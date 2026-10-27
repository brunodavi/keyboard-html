package com.keyboardhtml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Shared helper for tests that inspect project files (gradle.properties etc.). */
final class ProjectFiles {

    private ProjectFiles() {
    }

    /** Loads gradle.properties from the project root directory. */
    static Properties loadGradleProperties() throws IOException {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(new File(findProjectRoot(), "gradle.properties"));
            properties.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return properties;
    }

    /** Reads a file located in the project root directory (path must use '/'). */
    static String readProjectFile(String relativePath) throws IOException {
        File file = new File(findProjectRoot(), relativePath.replace('/', File.separatorChar));
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
}
