package com.box.l10n.mojito.cli.command.localextraction;

import com.box.l10n.mojito.cli.command.LocalExtractionCommand;
import com.box.l10n.mojito.json.ObjectMapper;
import com.google.common.collect.Sets;
import com.ibm.icu.text.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to compute difference between local extractions
 */
@Component
public class DiffLocalExtractionService {

    static final String DIFF_JSON = "diff.json";

    /**
     * logger
     */
    static Logger logger = LoggerFactory.getLogger(LocalExtractionCommand.class);

    @Autowired
    @Qualifier("outputIndented")
    ObjectMapper objectMapper;

    /**
     * Computes the difference between 2 local extractions designated by their branch names.
     *
     * @param newBranchName
     * @param oldBranchName
     * @param localExtractionsPaths
     * @throws MissingExtractionDirectoryExcpetion
     */
    public void computeBranchDiffAndSaveToJsonFile(String newBranchName, String oldBranchName, LocalExtractionsPaths localExtractionsPaths) throws MissingExtractionDirectoryExcpetion {
        Diff diff = computeBranchesDiff(newBranchName, oldBranchName, localExtractionsPaths);
        Path diffPath = localExtractionsPaths.diffPath();
        objectMapper.createDirectoriesAndWrite(diffPath, diff);
    }

    Diff computeBranchesDiff(String newBranchName, String oldBranchName, LocalExtractionsPaths localExtractionsPaths) throws MissingExtractionDirectoryExcpetion {
        logger.debug("Compute differences between branches: {} and {}", newBranchName, oldBranchName);

        checkBranchDirectoryExists(oldBranchName, localExtractionsPaths);
        checkBranchDirectoryExists(newBranchName, localExtractionsPaths);

        List<Path> newBranchLocalExtractionPaths = localExtractionsPaths.findAllLocalExtractionPaths(newBranchName);
        List<Path> oldBranchLocalExtractionPaths = localExtractionsPaths.findAllLocalExtractionPaths(oldBranchName);

        Set<String> newBranchSourceFileNames = localExtractionsPaths.sourceFileMatchPaths(newBranchLocalExtractionPaths, newBranchName);
        Set<String> oldBranchSourceFileNames = localExtractionsPaths.sourceFileMatchPaths(oldBranchLocalExtractionPaths, oldBranchName);

        Sets.SetView<String> addedFilenames = Sets.difference(newBranchSourceFileNames, oldBranchSourceFileNames);
        Sets.SetView<String> removedFilenames = Sets.difference(oldBranchSourceFileNames, newBranchSourceFileNames);

        if (!addedFilenames.isEmpty()) {
            addedFilenames.forEach(p -> logger.debug("Added files: {}", p));
        }

        if (!removedFilenames.isEmpty()) {
            removedFilenames.forEach(p -> logger.debug("Removed files: {}", p));
        }

        logger.debug("Same file names, check the file content");

        Set<LocalTextUnitWithAssetPath> newBranchTextUnits = getTextUnitsFromFile(newBranchLocalExtractionPaths, newBranchName, localExtractionsPaths);
        Set<LocalTextUnitWithAssetPath> oldlBranchTextUnits = getTextUnitsFromFile(oldBranchLocalExtractionPaths, oldBranchName, localExtractionsPaths);
        Sets.SetView<LocalTextUnitWithAssetPath> addedTextUnitWithFile = Sets.difference(newBranchTextUnits, oldlBranchTextUnits);
        Sets.SetView<LocalTextUnitWithAssetPath> removedTextUnitWithFile = Sets.difference(oldlBranchTextUnits, newBranchTextUnits);

        Diff diff = new Diff();
        diff.setAddedFiles(addedFilenames);
        diff.setRemovedFiles(removedFilenames);
        diff.setAddedTextUnits(addedTextUnitWithFile);
        diff.setRemovedTextUnits(removedTextUnitWithFile);

        return diff;
    }

    void checkBranchDirectoryExists(String branchName, LocalExtractionsPaths localExtractionsPaths) throws MissingExtractionDirectoryExcpetion {
        Path branchPath = localExtractionsPaths.extractionPath(branchName);

        if (!branchPath.toFile().exists()) {
            String msg = MessageFormat.format("There is no directory for branch: {0}, can't compare", branchName);
            throw new MissingExtractionDirectoryExcpetion(msg);
        }
    }

    Set<LocalTextUnitWithAssetPath> getTextUnitsFromFile(List<Path> localExtractionPaths, String branchName, LocalExtractionsPaths localExtractionsPaths) {
        return localExtractionPaths.stream().map(localExtractionPath -> {
            LocalExtraction localExtraction = objectMapper.readValueUnchecked(localExtractionPath.toFile(), LocalExtraction.class);
            return localExtraction.getLocalTextUnits().stream().map(t -> {
                LocalTextUnitWithAssetPath textUnitWithFile = new LocalTextUnitWithAssetPath();
                textUnitWithFile.setName(t.getName());
                textUnitWithFile.setSource(t.getSource());
                textUnitWithFile.setComments(t.getComments());
                textUnitWithFile.setAssetPath(localExtractionsPaths.sourceFileMatchPath(localExtractionPath, branchName));
                return textUnitWithFile;
            });
        }).flatMap(Function.identity()).collect(
                Collectors.toCollection(
                        () -> new TreeSet<>(LocalTextUnitComparators.BY_FILENAME_NAME_SOURCE_COMMENTS)
                )
        );
    }

}
