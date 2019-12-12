package com.box.l10n.mojito.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.box.l10n.mojito.cli.ConsoleWriter;
import com.box.l10n.mojito.cli.command.localextraction.DiffLocalExtractionService;
import com.box.l10n.mojito.cli.command.localextraction.LocalExtractionsPaths;
import com.box.l10n.mojito.cli.command.localextraction.MissingExtractionDirectoryExcpetion;
import com.box.l10n.mojito.cli.command.param.Param;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * To check if some text units are added in the branch, jq can be used like this: jq -e \".addedTextUnits | length > 0 \" diff.json
 *
 * @author jaurambault
 */
@Component
@Scope("prototype")
@Parameters(commandNames = {"extract-diff"}, commandDescription = "Generate a diff between 2 local extractions")
public class LocalExtractionDiffCommand extends Command {

    /**
     * logger
     */
    static Logger logger = LoggerFactory.getLogger(LocalExtractionDiffCommand.class);

    @Autowired
    ConsoleWriter consoleWriter;

    @Parameter(names = {Param.EXTRACTION_NAME_LONG, Param.EXTRACTION_NAME_SHORT}, arity = 1, required = true, description = Param.EXTRACTION_NAME_DESCRIPTION)
    String name;

    @Parameter(names = {"--with-extraction", "-w"}, arity = 1, required = true, description = "the name of the extraction to compare with")
    String createDiffWithBranch;

    @Parameter(names = {Param.EXTRACTION_OUTPUT_LONG, Param.EXTRACTION_OUTPUT_SHORT}, arity = 1, required = false, description = Param.EXTRACTION_OUTPUT_DESCRIPTION)
    String outputDirectoryParam = LocalExtractionsPaths.DEFAULT_OUTPUT_DIRECTORY;

    @Parameter(names = {Param.EXTRACTION_INPUT_LONG, Param.EXTRACTION_INPUT_SHORT}, arity = 1, required = false, description = Param.EXTRACTION_INPUT_DESCRIPTION)
    String inputDirectoryParam;

    @Autowired
    DiffLocalExtractionService diffLocalExtractionService;

    @Override
    public void execute() throws CommandException {

        consoleWriter.newLine().a("Generate diff between extractions: ").fg(Ansi.Color.CYAN).a(name).reset()
                .a(" and: ").fg(Ansi.Color.CYAN).a(createDiffWithBranch).println();

        LocalExtractionsPaths localExtractionsPaths = new LocalExtractionsPaths(outputDirectoryParam, inputDirectoryParam);

        try {
            diffLocalExtractionService.computeBranchDiffAndSaveToJsonFile(name, createDiffWithBranch, localExtractionsPaths);
        } catch (MissingExtractionDirectoryExcpetion msobe) {
            throw new CommandException(msobe.getMessage());
        }

        consoleWriter.a("See the diff: ").fg(Ansi.Color.CYAN).a(localExtractionsPaths.diffPath().toString());

        consoleWriter.fg(Ansi.Color.GREEN).newLine().a("Finished").println(2);
    }

}
