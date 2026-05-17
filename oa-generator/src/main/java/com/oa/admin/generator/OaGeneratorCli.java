package com.oa.admin.generator;

import com.oa.admin.generator.engine.CodeGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "oa-generator",
        mixinStandardHelpOptions = true,
        description = "OA Code Generator - generate backend CRUD code from YAML definition",
        version = "0.1.0")
/**
 * @author wxvirus
 */
public class OaGeneratorCli implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "YAML definition file path")
    private String definitionFile;

    @Option(names = {"-p", "--project"},
            description = "Project root directory (default: current directory)",
            defaultValue = ".")
    private String projectRoot;

    @Option(names = {"-m", "--target-module"},
            description = "Target Maven module (default: oa-app)",
            defaultValue = "oa-app")
    private String targetModule;

    @Option(names = {"--dry-run"},
            description = "Preview only, do not write files")
    private boolean dryRun;

    @Option(names = {"--flyway-only"},
            description = "Only generate Flyway migration script")
    private boolean flywayOnly;

    @Option(names = {"--frontend"},
            description = "Also generate Vue 3 frontend pages")
    private boolean frontend;

    @Option(names = {"--entity"},
            description = "Only generate specified entities (comma separated)",
            split = ",")
    private List<String> entityFilter;

    @Override
    public Integer call() {
        try {
            var generator = new CodeGenerator();
            generator.generate(
                    Path.of(definitionFile).toAbsolutePath(),
                    Path.of(projectRoot).toAbsolutePath(),
                    targetModule,
                    dryRun,
                    entityFilter,
                    flywayOnly,
                    frontend
            );
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new OaGeneratorCli()).execute(args));
    }
}
