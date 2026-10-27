package com.keyboardhtml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts imported keyboard .zip packages into the app's internal
 * layouts folder. Pure java, so the rules are unit tested without
 * Android. One zip file (or a zip wrapping a single folder, macOS
 * Finder-style) becomes one layout folder below the root.
 */
public final class ZipLayoutExtractor {

    /** Raised when the zip is unreadable, malformed, or unsafe. */
    static final class InvalidLayout extends RuntimeException {
        InvalidLayout(String message) {
            super(message);
        }
    }

    /** Outcome of a successful import: the layout identifier to select. */
    static final class Result {
        private final String layoutId;

        Result(String layoutId) {
            this.layoutId = layoutId;
        }

        String layoutId() {
            return layoutId;
        }
    }

    private ZipLayoutExtractor() {
    }

    /**
     * Extracts zip into {@code layoutsRoot/<layoutId>}, replacing any
     * previous layout with the same id. Requires an index.html at the zip
     * root or inside a single wrapping folder.
     */
    static Result extract(File zipFile, File layoutsRoot) throws IOException {
        String layoutId = layoutIdFrom(zipFile);
        File layoutDir = new File(layoutsRoot, layoutId);
        deleteRecursively(layoutDir);
        if (!layoutDir.mkdirs() && !layoutDir.isDirectory()) {
            throw new InvalidLayout("cannot create layout folder " + layoutDir);
        }
        try (ZipInputStream zip = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)))) {
            forEachEntry(zip, layoutDir);
        } catch (IOException e) {
            throw new InvalidLayout("unreadable zip: " + e.getMessage());
        }
        flattenSingleWrappingFolder(layoutDir);
        if (!new File(layoutDir, "index.html").isFile()) {
            throw new InvalidLayout("zip has no index.html at its root");
        }
        return new Result(layoutId);
    }

    private static String layoutIdFrom(File zipFile) {
        String name = zipFile.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String sanitized = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isEmpty() || sanitized.equals(".") || sanitized.equals("..")) {
            throw new InvalidLayout("zip file name does not map to a valid layout id: " + name);
        }
        return sanitized;
    }

    private static void forEachEntry(ZipInputStream zip, File layoutDir) throws IOException {
        String layoutPath = layoutDir.getCanonicalPath() + File.separator;
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            String name = entry.getName().replace('\\', '/');
            if (name.isEmpty() || name.endsWith("/") || name.startsWith("__MACOSX")
                    || name.startsWith("/")) {
                continue;
            }
            File target = new File(layoutDir, name);
            if (!target.getCanonicalPath().startsWith(layoutPath)) {
                throw new InvalidLayout("zip entry escapes the layout folder: " + name);
            }
            File parent = target.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new InvalidLayout("cannot create folder " + parent);
            }
            try (OutputStream out = new FileOutputStream(target)) {
                copy(zip, out);
            }
            zip.closeEntry();
        }
    }

    /**
     * Zips produced from a selected folder wrap the files in one folder; in
     * that case the wrapping folder is dissolved. Deeper nesting is rejected.
     */
    private static void flattenSingleWrappingFolder(File layoutDir) throws IOException {
        File[] children = layoutDir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory() && new File(child, "index.html").isFile()) {
                moveRecursively(child, layoutDir);
                deleteRecursively(child);
                return;
            }
        }
    }

    private static void forEachChildMove(File source, File destinationFolder) throws IOException {
        File[] children = source.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            File destination = new File(destinationFolder, child.getName());
            if (child.isDirectory()) {
                if (!destination.mkdir() && !destination.isDirectory()) {
                    throw new IOException("cannot create folder " + destination);
                }
                forEachChildMove(child, destination);
            } else {
                try (InputStream in = new FileInputStream(child);
                     OutputStream out = new FileOutputStream(destination)) {
                    copy(in, out);
                }
            }
        }
    }

    private static void moveRecursively(File source, File destinationFolder) throws IOException {
        forEachChildMove(source, destinationFolder);
    }

    private static void deleteRecursively(File file) throws IOException {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("cannot delete " + file);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
