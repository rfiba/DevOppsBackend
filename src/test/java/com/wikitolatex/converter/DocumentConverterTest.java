/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.wikitolatex.converter;

import com.wikitolatex.converter.Pandoc.DocumentConverter;
import com.wikitolatex.converter.Pandoc.InputFormat;
import com.wikitolatex.converter.Pandoc.OutputFormat;
import com.wikitolatex.converter.Pandoc.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class DocumentConverterTest {

    private DocumentConverter instance;


    private final File fromOdtFile = new File("src/test/java/com/wikitolatex/converter/test-docs/Con los ojos cerrados.odt");
    private final File fromDocxFile = new File("src/test/java/com/wikitolatex/converter/test-docs/Con los ojos cerrados.docx");
    private final File toAsciiFile = new File("src/test/java/com/wikitolatex/converter/test-docs/result.txt");
    private final File toHtmlFile = new File("src/test/java/com/wikitolatex/converter/test-docs/result.html");

    @Before
    public void setup() {
        instance = new DocumentConverter();
    }

    @After
    public void cleanup() {
        toAsciiFile.delete();
        toHtmlFile.delete();
    }
    
    @Test
    public void convert_docxToAscii_createsFile() throws IOException {
        instance.fromFile(fromDocxFile, InputFormat.DOCX)
                .toFile(toAsciiFile, OutputFormat.ASCIIDOC)
                .convert();

        assertTrue(toAsciiFile.isFile());
        assertTrue(toAsciiFile.length() > 11000);
        assertTrue(toAsciiFile.length() < 12000);
        assertFirstLineEquals(toAsciiFile, "Con los ojos cerrados");
    }

    @Test
    public void convert_odtToAscii_createsFile() throws IOException {
        instance.fromFile(fromOdtFile, InputFormat.ODT)
                .toFile(toAsciiFile, OutputFormat.ASCIIDOC)
                .convert();

        assertTrue(toAsciiFile.isFile());
        assertTrue(toAsciiFile.length() > 11000);
        assertTrue(toAsciiFile.length() < 12000);
        assertFirstLineEquals(toAsciiFile, "Con los ojos cerrados");
    }
    
    @Test
    public void convert_odtToHtml_createsFile() throws IOException {
        instance.fromFile(fromOdtFile, InputFormat.ODT)
                .toFile(toHtmlFile, OutputFormat.HTML5)
                .convert();

        assertTrue(toHtmlFile.isFile());
        assertTrue(toHtmlFile.length() > 11000);
        assertTrue(toHtmlFile.length() < 12000);
        assertFirstLineEquals(toHtmlFile, "<p>Con los ojos cerrados</p>");
    }

    @Test
    public void convert_docxToHtmlWithStandaloneOption_createsFile() throws IOException {
        instance.fromFile(fromDocxFile, InputFormat.DOCX)
                .toFile(toHtmlFile, OutputFormat.HTML5)
                .addOption("-s")
                .convert();

        assertTrue(toHtmlFile.isFile());
        assertTrue(toHtmlFile.length() > 12000);
        assertTrue(toHtmlFile.length() < 13000);
        assertFirstLineEquals(toHtmlFile, "<!DOCTYPE html>");
    }

    @Test(expected = UncheckedIOException.class)
    public void convert_pandocIsNotInstalled_throwsException() {
        Settings settings = new Settings();
        settings.setPandocExec("pandoc_exec_that_does_not_exist.exe");
        instance = new DocumentConverter(settings);

        instance.fromFile(fromDocxFile, InputFormat.DOCX)
                .toFile(toHtmlFile, OutputFormat.DOCX)
                .convert();
    }

    @Test(expected = RuntimeException.class)
    public void convert_inputFileDoesNotExist_throwsException() {
        instance.fromFile(new File("file_that_does_not_exist.odt"), InputFormat.DOCX)
                .toFile(toHtmlFile, OutputFormat.DOCX)
                .convert();
    }

    /**
     * Asserts if the first line in the file is equals to the given parameter.
     */
    private void assertFirstLineEquals(File file, String expected) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            assertEquals(expected, firstLine);
        } catch (IOException ex) {
            fail("Could not read file");
            throw new UncheckedIOException(ex);
        }
    }

}
