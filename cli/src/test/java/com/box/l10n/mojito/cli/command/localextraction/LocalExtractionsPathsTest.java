package com.box.l10n.mojito.cli.command.localextraction;

import com.box.l10n.mojito.cli.filefinder.FileMatch;
import com.box.l10n.mojito.test.IOTestBase;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LocalExtractionsPathsTest extends IOTestBase {

    static final String outputDirectory = "someoutput";
    static final String extractName = "testExtract";

    @Test
    public void outputDirectory() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);
        Assert.assertEquals(Paths.get(outputDirectory), localExtractionsPaths.outputDirectory());
    }

    @Test
    public void inputDirectory() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);
        Assert.assertEquals(
                "If not provided the input directory will be the same as the output directory",
                Paths.get(outputDirectory),
                localExtractionsPaths.inputDirectory());
    }

    @Test
    public void diffPath() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);
        Assert.assertEquals(Paths.get(outputDirectory, "diff.json"), localExtractionsPaths.diffPath());
    }

    @Test
    public void branchPath() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);
        Assert.assertEquals(Paths.get(outputDirectory, extractName), localExtractionsPaths.extractionPath(extractName));
    }

    @Test
    public void localExtractionPath() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);

        FileMatch sourceFileMatch = Mockito.mock(FileMatch.class);
        Mockito.when(sourceFileMatch.getSourcePath()).thenReturn("LC_MESSAGES/messages.pot");

        Assert.assertEquals(
                Paths.get(outputDirectory, extractName, "LC_MESSAGES/messages.pot.json"),
                localExtractionsPaths.localExtractionPath(sourceFileMatch, extractName));
    }

    @Test
    public void sourceFileMatchPath() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);
        localExtractionsPaths.sourceFileMatchPath(Paths.get(outputDirectory, extractName, "LC_MESSAGES/messages.pot.json"), extractName);
    }

    @Test
    public void sourceFileMatchPaths() {
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectory);
        Set<String> sourceFileMatchPaths = localExtractionsPaths.sourceFileMatchPaths(
                Arrays.asList(
                        Paths.get(outputDirectory, extractName, "LC_MESSAGES/messages.pot.json"),
                        Paths.get(outputDirectory, extractName, "LC_MESSAGES/messages2.pot.json")),
                extractName
        );

        Assert.assertEquals(Sets.newHashSet("LC_MESSAGES/messages.pot", "LC_MESSAGES/messages2.pot"), sourceFileMatchPaths);
    }

    @Test
    public void findAllLocalExtractionPaths() {
        File inputResourcesTestDir = getInputResourcesTestDir();
        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(inputResourcesTestDir.getPath());

        List<Path> allLocalExtractionPaths = localExtractionsPaths.findAllLocalExtractionPaths(extractName);
        Assert.assertEquals(
                Sets.newHashSet(
                        inputResourcesTestDir.toPath().resolve(Paths.get("testExtract/LC_MESSAGES/messages.pot.json")),
                        inputResourcesTestDir.toPath().resolve(Paths.get("testExtract/LC_MESSAGES/messages2.pot.json")))
                ,
                new HashSet<>(allLocalExtractionPaths));
    }
}
