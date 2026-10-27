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

    private void write(File folder, String relative, String content) throws IOException {
        File target = new File(folder, relative);
        target.getParentFile().mkdirs();
        Writer out = new OutputStreamWriter(new FileOutputStream(target), "UTF-8");
        out.write(content);
        out.close();
    }
}
