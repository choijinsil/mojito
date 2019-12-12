package com.box.l10n.mojito.cli.command.localextraction;

import com.box.l10n.mojito.cli.filefinder.FileMatch;
import com.box.l10n.mojito.io.Files;
import com.google.common.base.MoreObjects;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains logic to generate paths for local extractions and convert them back to original filenames
 */
public class LocalExtractionsPaths {

    public static final String DEFAULT_OUTPUT_DIRECTORY = ".mojito/extractions";
    static final String JSON_FILE_EXTENSION = ".json";
    static final String DIFF_OUTPUT = "diff.json";

    String inputDirectory;
    String outputDirectory;

    public  LocalExtractionsPaths(String outputDirectory) {
        this(outputDirectory, null);
    }

    public LocalExtractionsPaths(String outputDirectory, String inputDirectory) {
        this.outputDirectory = outputDirectory;
        this.inputDirectory = MoreObjects.firstNonNull(inputDirectory, outputDirectory);
    }

    public Path inputDirectory() {
        return Paths.get(inputDirectory);
    }

    public Path outputDirectory() {
        return Paths.get(outputDirectory);
    }

    public Path diffPath() {
        return Paths.get(outputDirectory, DIFF_OUTPUT);
    }

    Path extractionPath(String extractionName) {
        return Paths.get(outputDirectory, extractionName);
    }

    Path localExtractionPath(FileMatch sourceFileMatch, String extractionName) {
        return Paths.get(extractionPath(extractionName).toString(), sourceFileMatch.getSourcePath() + JSON_FILE_EXTENSION);
    }

    String sourceFileMatchPath(Path localExtractionPath, String extractionName) {
        String relativePath = extractionPath(extractionName).relativize(localExtractionPath).toString();
        String withoutExtension = relativePath.substring(0, relativePath.length() - JSON_FILE_EXTENSION.length());
        return withoutExtension;
    }

    Set<String> sourceFileMatchPaths(List<Path> localExtractionPaths, String extractionName) {
        return localExtractionPaths.stream().map(p -> sourceFileMatchPath(p, extractionName)).collect(Collectors.toSet());
    }

    List<Path> findAllLocalExtractionPaths(String extractionName) {
        return Files.find(
                extractionPath(extractionName),
                100,
                (p, f) -> p.toString().endsWith(JSON_FILE_EXTENSION)).
                collect(Collectors.toList());
    }
}
