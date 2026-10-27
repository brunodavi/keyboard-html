package com.keyboardhtml;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * Guards version consistency between the project files and the project
 * itself: gradle.properties declares the app version, and the files that
 * describe releases (fe. CHANGELOG.md) must match it. The release pipeline
 * additionally verifies the git tag against APP_VERSION.
 */
public class VersionConsistencyTest {

    /** Release tags are vX.Y.Z, so APP_VERSION must be semver X.Y.Z. */
    private static final Pattern THREE_PART_VERSION = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    private Properties properties;
    private String changelog;

    @Before
    public void loadProjectFiles() throws IOException {
        properties = ProjectFiles.loadGradleProperties();
        changelog = ProjectFiles.readProjectFile("CHANGELOG.md");
    }

    @Test
    public void appVersionIsThreePartSemanticVersion() {
        String version = properties.getProperty("APP_VERSION");
        if (version == null || !THREE_PART_VERSION.matcher(version.trim()).matches()) {
            fail("APP_VERSION must be X.Y.Z to match release tags vX.Y.Z, but was '"
                    + version + "'");
        }
    }

    @Test
    public void appVersionCodeIsPositiveInteger() {
        String code = properties.getProperty("APP_VERSION_CODE");
        if (code == null || !code.trim().matches("^\\d+$") || Integer.parseInt(code.trim()) < 1) {
            fail("APP_VERSION_CODE must be a positive integer, but was '" + code + "'");
        }
    }

    @Test
    public void changelogTracksFilesWithTheProjectVersion() {
        String version = properties.getProperty("APP_VERSION", "").trim();
        assertTrue(
                "CHANGELOG.md must hold an 'Unreleased' section or a '## [" + version + "]' section",
                changelog.startsWith("# Changelog")
                        && (changelogContainsSection(changelog, "Unreleased")
                                || changelogContainsSection(changelog, version)));
    }

    private static boolean changelogContainsSection(String changelog, String section) {
        return changelog.contains("## [" + section + "]");
    }
}
