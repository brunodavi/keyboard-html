package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

/** TDD for listing installed html keyboard layouts and the current selection. */
public class LayoutRepositoryTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File root;
    private LayoutRepository repository;

    @Before
    public void prepare() throws IOException {
        root = tmp.newFolder("layouts");
        repository = new LayoutRepository(root);
    }

    @Test
    public void listsLayoutFoldersThatContainIndexHtmlSorted() throws IOException {
        write(root, "zeta/index.html", "zeta keyboard");
        write(root, "alpha/index.html", "alpha keyboard");
        write(root, "incomplete/index.css", "just css"); // missing index.html
        write(root, "_current", "zeta");
        write(root, ".DS_Store", "noise");

        assertEquals(Arrays.asList("alpha", "zeta"), repository.listLayouts());
    }

    @Test
    public void missingRootMeansEmptyListInsteadOfCrashing() throws IOException {
        assertEquals(0, new LayoutRepository(new File(tmp.getRoot(), "nope")).listLayouts().size());
    }

    @Test
    public void currentSelectionStartsUnselected() throws IOException {
        assertNull(repository.currentLayoutId());
    }

    @Test
    public void savingThenReadingTheCurrentLayoutRoundTrips() throws IOException {
        write(root, "zeta/index.html", "zeta");
        repository.currentLayoutId("zeta");
        assertEquals("zeta", repository.currentLayoutId());
    }

    @Test
    public void unknownLayoutIdCannotBeSelected() throws IOException {
        try {
            repository.currentLayoutId("ghost");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // ok
        }
        assertNull("no selection written", repository.currentLayoutId());
    }

    @Test
    public void selectionFileHoldsExactlyTheSelectedId() throws IOException {
        write(root, "alpha/index.html", "alpha");
        repository.currentLayoutId("alpha");

        File current = new File(root, LayoutRepository.SELECTION_FILE);
        FileReader reader = new FileReader(current);
        StringBuilder out = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            out.append((char) c);
        }
        reader.close();
        assertEquals("no extra bytes beyond the id", "alpha", out.toString());
    }

    @Test
    public void clearingSelectionReturnsToTheBuiltInDefault() throws IOException {
        write(root, "alpha/index.html", "alpha");
        repository.currentLayoutId("alpha");
        assertEquals("alpha", repository.currentLayoutId());

        repository.clearSelection();

        assertNull("back to unselected (built-in default)", repository.currentLayoutId());
        assertFalse("marker file removed", new File(root, LayoutRepository.SELECTION_FILE).exists());
    }

    @Test
    public void clearingWithoutSelectionIsANoop() throws IOException {
        repository.clearSelection();
        assertNull(repository.currentLayoutId());
    }

    @Test
    public void layoutNameComesFromTheOptionalManifestJson() throws IOException {
        write(root, "alpha/index.html", "alpha");
        write(root, "alpha/manifest.json", "{\"name\":\"Alpha Pro\",\"author\":\"b\"}");

        assertEquals("Alpha Pro", repository.layoutName("alpha"));
    }

    @Test
    public void layoutNameFallsBackToTheFolderId() throws IOException {
        write(root, "alpha/index.html", "alpha");

        assertEquals("alpha", repository.layoutName("alpha"));
    }

    @Test
    public void malformedManifestStillFallsBackToTheFolderId() throws IOException {
        write(root, "alpha/index.html", "alpha");
        write(root, "alpha/manifest.json", "{ not json ");

        assertEquals("alpha", repository.layoutName("alpha"));
    }

    @Test
    public void unknownLayoutIdCannotBeNamed() throws IOException {
        try {
            repository.layoutName("ghost");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

    @Test
    public void currentLayoutUriPointsToTheSelectedLayoutIndex() throws IOException {
        write(root, "alpha/index.html", "alpha");
        repository.currentLayoutId("alpha");

        assertEquals(new File(root, "alpha/index.html").toURI().toString(),
                repository.currentLayoutUri());
    }

    @Test
    public void currentLayoutUriIsNullWithoutASelection() throws IOException {
        assertNull(repository.currentLayoutUri());
    }

    @Test
    public void currentLayoutUriIsNullWhenTheSelectedIndexIsGone() throws IOException {
        write(root, "alpha/index.html", "alpha");
        repository.currentLayoutId("alpha");
        deleteRecursively(new File(root, "alpha"));

        assertNull(repository.currentLayoutUri());
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    private void write(File folder, String relative, String content) throws IOException {
        File target = new File(folder, relative);
        target.getParentFile().mkdirs();
        Writer out = new OutputStreamWriter(new FileOutputStream(target), "UTF-8");
        out.write(content);
        out.close();
    }
}
