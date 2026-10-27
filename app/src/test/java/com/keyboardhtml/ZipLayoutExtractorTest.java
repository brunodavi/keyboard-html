package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** TDD for importing keyboard .zip packages: extraction rules and safety. */
public class ZipLayoutExtractorTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File target;

    @Before
    public void prepare() throws IOException {
        target = tmp.newFolder("layouts");
    }

    @Test
    public void extractsIndexHtmlFromFlatZipIntoLayoutFolder() throws IOException {
        File zip = new ZipFileBuilder(new File(tmp.getRoot(), "myzip.zip"))
                .add("index.html", "<html>QWERTY</html>")
                .add("kbd.css", "button { width: 40px; }").close().file();

        ZipLayoutExtractor.Result result = ZipLayoutExtractor.extract(zip, target);

        assertEquals("<html>QWERTY</html>", read(new File(target, "myzip/index.html")));
        assertTrue(new File(target, "myzip/kbd.css").exists());
        assertEquals("layout id from the zip file name", "myzip", result.layoutId());
    }

    @Test
    public void flattensAZipThatWrapsItsFilesInASingleFolder() throws IOException {
        File zip = new ZipFileBuilder(new File(tmp.getRoot(), "myzip.zip"))
                .add("myzip/index.html", "<html>QWERTY</html>")
                .add("myzip/kbd.css", "button {}").close().file();

        ZipLayoutExtractor.Result result = ZipLayoutExtractor.extract(zip, target);

        assertEquals("<html>QWERTY</html>", read(new File(target, "myzip/index.html")));
        assertTrue(new File(target, "myzip/kbd.css").exists());
        assertEquals("myzip", result.layoutId());
    }

    @Test
    public void reimportingTheSameNameOverwritesPreviousFiles() throws IOException {
        File zip = new File(tmp.getRoot(), "myzip.zip");
        ZipLayoutExtractor.extract(
                new ZipFileBuilder(zip).add("index.html", "v1").close().file(), target);
        ZipLayoutExtractor.extract(
                new ZipFileBuilder(zip).add("index.html", "v2").close().file(), target);

        assertEquals("latest import wins", "v2", read(new File(target, "myzip/index.html")));
    }

    @Test
    public void rejectsZipThatHasNoIndexHtml() throws IOException {
        File zip = new ZipFileBuilder(tmp.newFile("flat.zip"))
                .add("docs/readme.txt", "hi").close().file();

        expectInvalid(() -> ZipLayoutExtractor.extract(zip, target));
    }

    @Test
    public void rejectsZipSlipEntriesOutsideTheLayoutFolder() throws IOException {
        File zip = new ZipFileBuilder(tmp.newFile("evil.zip"))
                .add("index.html", "<html>x</html>")
                .add("../evil.java", "class Evil {}").close().file();

        expectInvalid(() -> ZipLayoutExtractor.extract(zip, target));
    }

    @Test
    public void rejectsNestedParentTraversalOutsideTheLayoutFolder() throws IOException {
        File zip = new ZipFileBuilder(tmp.newFile("evil.zip"))
                .add("index.html", "<html>x</html>")
                .add("kbd/../../../tmp/evil.txt", "!").close().file();

        expectInvalid(() -> ZipLayoutExtractor.extract(zip, target));
    }

    @Test
    public void rejectsIndexHtmlNestedDeeperThanOneLevel() throws IOException {
        File zip = new ZipFileBuilder(tmp.newFile("flat.zip"))
                .add("a/b/c/index.html", "<html>deep</html>").close().file();

        expectInvalid(() -> ZipLayoutExtractor.extract(zip, target));
    }

    /** Thunk around ZipLayoutExtractor calls that may throw IOException. */
    private interface FileCall {
        void run() throws IOException;
    }

    private void expectInvalid(FileCall call) {
        try {
            call.run();
            fail("expected ZipLayoutExtractor.InvalidLayout");
        } catch (ZipLayoutExtractor.InvalidLayout expected) {
            // ok
        } catch (IOException unexpected) {
            fail("unexpected IOException: " + unexpected.getMessage());
        }
    }

    private static String read(File file) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line).append('\n');
        }
        reader.close();
        if (out.length() > 0 && out.charAt(out.length() - 1) == '\n') {
            out.setLength(out.length() - 1);
        }
        return out.toString();
    }

    /** Tiny helper to build zips for the tests. */
    static final class ZipFileBuilder {
        private final File file;
        private final ZipOutputStream zip;

        ZipFileBuilder(File file) throws IOException {
            this.file = file;
            this.zip = new ZipOutputStream(new FileOutputStream(file));
        }

        ZipFileBuilder add(String name, String content) throws IOException {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(content.getBytes("UTF-8"));
            zip.closeEntry();
            return this;
        }

        ZipFileBuilder close() throws IOException {
            zip.close();
            return this;
        }

        File file() {
            return file;
        }
    }
}
