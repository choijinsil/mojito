package com.box.l10n.mojito.cli.command.localextraction;

import com.box.l10n.mojito.cli.command.CommandException;
import com.box.l10n.mojito.cli.command.CommandHelper;
import com.box.l10n.mojito.cli.filefinder.FileMatch;
import com.box.l10n.mojito.io.Files;
import com.box.l10n.mojito.json.ObjectMapper;
import com.box.l10n.mojito.okapi.FilterConfigIdOverride;
import com.box.l10n.mojito.okapi.RawDocument;
import com.box.l10n.mojito.okapi.asset.AssetPathToFilterConfigMapper;
import com.box.l10n.mojito.okapi.asset.FilterConfigurationMappers;
import com.box.l10n.mojito.okapi.asset.UnsupportedAssetFilterTypeException;
import com.box.l10n.mojito.okapi.filters.FilterOptions;
import com.box.l10n.mojito.okapi.steps.CheckForDoNotTranslateStep;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class LocalExtractionService {

    /**
     * logger
     */
    static Logger logger = LoggerFactory.getLogger(LocalExtractionService.class);

    @Autowired
    @Qualifier("outputIndented")
    ObjectMapper objectMapper;

    @Autowired
    CommandHelper commandHelper;

    @Autowired
    AssetPathToFilterConfigMapper assetPathToFilterConfigMapper;

    @Autowired
    FilterConfigurationMappers filterConfigurationMappers;

    public void convertAndSaveToJsonFile(FileMatch sourceFileMatch, List<String> filterOptions,
                                         String localExtractionName, LocalExtractionsPaths localExtractionsPaths) throws CommandException {
        LocalExtraction localExtraction = convertFileMatch(sourceFileMatch, filterOptions, localExtractionName);
        Path localExtractionPath = localExtractionsPaths.localExtractionPath(sourceFileMatch, localExtractionName);
        objectMapper.createDirectoriesAndWrite(localExtractionPath, localExtraction);
    }

    public void deleteTargetDirectoryForBranch(LocalExtractionsPaths localExtractionsPaths, String localExtractionName) {
        logger.debug("Delete the directory that will contain the local export for that specific branch");
        if (localExtractionsPaths.extractionPath(localExtractionName).toFile().exists()) {
            Files.deleteRecursivelyIfExists(localExtractionsPaths.extractionPath(localExtractionName));
        }
    }

    LocalExtraction convertFileMatch(FileMatch sourceFileMatch, List<String> filterOptions, String localExtractionName) throws CommandException {
        List<LocalTextUnit> localTextUnits = getTextUnitsForSourceFileMatch(sourceFileMatch, filterOptions);

        LocalExtraction localExtraction = new LocalExtraction();
        localExtraction.setLocalTextUnits(localTextUnits);
        localExtraction.setName(localExtractionName);
        localExtraction.setFilterOptions(filterOptions);
        localExtraction.setFileType(sourceFileMatch.getFileType().getClass().getSimpleName());

        return localExtraction;
    }

    List<LocalTextUnit> getTextUnitsForSourceFileMatch(FileMatch sourceFileMatch, List<String> filterOptions) {
        String sourcePath = sourceFileMatch.getSourcePath();
        String assetContent = commandHelper.getFileContentWithXcodePatch(sourceFileMatch);
        FilterConfigIdOverride filterConfigIdOverride = sourceFileMatch.getFileType().getFilterConfigIdOverride();

        try {
            return getLocalTextUnitsForAsset(sourcePath, assetContent, filterConfigIdOverride, filterOptions);
        } catch (UnsupportedAssetFilterTypeException uasft) {
            throw new RuntimeException("Source file match must be for a supported file type", uasft);
        }
    }

    List<LocalTextUnit> getLocalTextUnitsForAsset(String assetPath,
                                                  String assetContent,
                                                  FilterConfigIdOverride filterConfigIdOverride,
                                                  List<String> filterOptions) throws UnsupportedAssetFilterTypeException {

        logger.debug("Configuring pipeline");

        IPipelineDriver driver = new PipelineDriver();

        driver.addStep(new RawDocumentToFilterEventsStep());
        driver.addStep(new CheckForDoNotTranslateStep());
        ToLocalTextUnitsStep toLocalTextUnitsStep = new ToLocalTextUnitsStep();
        driver.addStep(toLocalTextUnitsStep);

        logger.debug("Adding all supported filters to the pipeline driver");
        driver.setFilterConfigurationMapper(filterConfigurationMappers.getConfiguredFilterConfigurationMapper());

        RawDocument rawDocument = new RawDocument(assetContent, LocaleId.ENGLISH);

        String filterConfigId = null;

        if (filterConfigIdOverride != null) {
            filterConfigId = filterConfigIdOverride.getOkapiFilterId();
        } else {
            filterConfigId = assetPathToFilterConfigMapper.getFilterConfigIdFromPath(assetPath);
        }

        rawDocument.setFilterConfigId(filterConfigId);
        logger.debug("Set filter config {} for asset {}", filterConfigId, assetPath);

        logger.debug("Filter options: {}", filterOptions);
        rawDocument.setAnnotation(new FilterOptions(filterOptions));

        driver.addBatchItem(rawDocument);

        logger.debug("Start processing batch");
        driver.processBatch();

        return toLocalTextUnitsStep.getLocalTextUnits();
    }

}
