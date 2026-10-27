package com.keyboardhtml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Installed html keyboard layouts live as folders under the internal
 * layouts root, each with an index.html. The current selection is kept
 * in a tiny marker file. Pure java so it is unit tested without Android.
 */
final class LayoutRepository {

    /** Marker file holding the selected layout id, next to the layout folders. */
    static final String SELECTION_FILE = "_current";

    private final File root;

    LayoutRepository(File root) {
        this.root = root;
    }

    /** Layout folder names containing an index.html, sorted alphabetically. */
    List<String> listLayouts() {
        List<String> layouts = new ArrayList<>();
        File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory() && new File(child, "index.html").isFile()) {
                    layouts.add(child.getName());
                }
            }
        }
        Collections.sort(layouts);
        return layouts;
    }

    /** The selected layout id, or null when nothing was selected yet. */
    String currentLayoutId() {
        File selection = new File(root, SELECTION_FILE);
        if (!selection.isFile()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(selection), StandardCharsets.UTF_8))) {
            String id = reader.readLine();
            return id == null || id.isEmpty() ? null : id;
        } catch (IOException e) {
            return null;
        }
    }

    /** Persists the selection; only ids that name an installed layout pass. */
    void currentLayoutId(String layoutId) {
        if (!listLayouts().contains(layoutId)) {
            throw new IllegalArgumentException("unknown layout: " + layoutId);
        }
        File selection = new File(root, SELECTION_FILE);
        try (Writer out = new OutputStreamWriter(
                new FileOutputStream(selection), StandardCharsets.UTF_8)) {
            out.write(layoutId);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write layout selection", e);
        }
    }
}
