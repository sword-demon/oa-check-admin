package com.oa.admin.generator.engine;

import com.oa.admin.generator.config.GeneratorConfig;
import com.oa.admin.generator.model.EntityDefinition;
import com.oa.admin.generator.model.EnumDefinition;
import com.oa.admin.generator.model.GenerationContext;
import com.oa.admin.generator.parser.TypeMapper;
import com.oa.admin.generator.parser.YamlDefinitionParser;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    private final FreeMarkerEngine templateEngine = new FreeMarkerEngine();
    private final FlywayVersionResolver flywayResolver = new FlywayVersionResolver();

    public void generate(Path definitionFile, Path projectRoot, String targetModule,
                         boolean dryRun, List<String> entityFilter, boolean flywayOnly) throws IOException, TemplateException {
        YamlDefinitionParser parser = new YamlDefinitionParser();
        YamlDefinitionParser.ParseResult result = parser.parse(definitionFile);
        GeneratorConfig config = result.config();
        String packageName = config.getFullPackage();
        String packagePath = packageName.replace('.', '/');
        Path javaSrcRoot = projectRoot.resolve(targetModule + "/src/main/java/" + packagePath);

        System.out.println("=== OA Code Generator ===");
        System.out.println("Module: " + config.getModule());
        System.out.println("Package: " + packageName);
        System.out.println("Target: " + javaSrcRoot);
        System.out.println("Entities: " + result.entities().stream().map(EntityDefinition::getName).toList());
        System.out.println("Enums: " + result.enums().keySet());
        System.out.println();

        if (!flywayOnly) {
            generateEnums(result.enums(), config, packageName, javaSrcRoot, dryRun);
            generateEntities(result.entities(), entityFilter, config, result.enums(), packageName, javaSrcRoot, dryRun);
        }

        generateFlyway(result.entities(), config, projectRoot, targetModule, dryRun);

        System.out.println();
        if (dryRun) {
            System.out.println("=== Dry run complete. No files written. ===");
        } else {
            System.out.println("=== Generation complete! ===");
            printPostInstructions(config, targetModule);
        }
    }

    private void generateEnums(Map<String, EnumDefinition> enums, GeneratorConfig config,
                               String packageName, Path javaSrcRoot, boolean dryRun) throws IOException, TemplateException {
        for (var entry : enums.entrySet()) {
            var ctx = GenerationContext.builder()
                    .config(config)
                    .enumDef(entry.getValue())
                    .packageName(packageName)
                    .build();
            String code = templateEngine.render("enum.ftl", buildDataModel(ctx));
            Path outputPath = javaSrcRoot.resolve("enums/" + entry.getKey() + ".java");
            writeOrPreview(outputPath, code, dryRun);
        }
    }

    private void generateEntities(List<EntityDefinition> entities, List<String> entityFilter,
                                  GeneratorConfig config, Map<String, EnumDefinition> enums,
                                  String packageName, Path javaSrcRoot, boolean dryRun) throws IOException, TemplateException {
        for (var entity : entities) {
            if (entityFilter != null && !entityFilter.isEmpty() && !entityFilter.contains(entity.getName())) {
                continue;
            }
            var ctx = GenerationContext.builder()
                    .config(config)
                    .entity(entity)
                    .enums(enums)
                    .packageName(packageName)
                    .build();

            Map<String, Object> dataModel = buildDataModel(ctx);

            writeOrPreview(javaSrcRoot.resolve("entity/" + entity.getName() + ".java"),
                    templateEngine.render("entity.ftl", dataModel), dryRun);
            writeOrPreview(javaSrcRoot.resolve("mapper/" + entity.getMapperName() + ".java"),
                    templateEngine.render("mapper.ftl", dataModel), dryRun);
            writeOrPreview(javaSrcRoot.resolve("service/" + entity.getServiceName() + ".java"),
                    templateEngine.render("service.ftl", dataModel), dryRun);
            writeOrPreview(javaSrcRoot.resolve("service/impl/" + entity.getServiceImplName() + ".java"),
                    templateEngine.render("serviceImpl.ftl", dataModel), dryRun);
            writeOrPreview(javaSrcRoot.resolve("controller/" + entity.getControllerName() + ".java"),
                    templateEngine.render("controller.ftl", dataModel), dryRun);
        }
    }

    private void generateFlyway(List<EntityDefinition> entities, GeneratorConfig config,
                                Path projectRoot, String targetModule, boolean dryRun) throws IOException, TemplateException {
        Path migrationDir = projectRoot.resolve(targetModule + "/src/main/resources/db/migration");
        String nextVersion = flywayResolver.nextVersion(migrationDir);
        String desc = config.getModule() + "_module";

        var ctx = GenerationContext.builder()
                .config(config)
                .allEntities(entities)
                .packageName(config.getFullPackage())
                .flywayVersion(nextVersion)
                .flywayDescription(desc)
                .build();

        String sql = templateEngine.render("flyway.ftl", buildDataModel(ctx));
        Path sqlPath = migrationDir.resolve(nextVersion + "__" + desc + ".sql");
        writeOrPreview(sqlPath, sql, dryRun);
    }

    private Map<String, Object> buildDataModel(GenerationContext ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("ctx", ctx);
        try {
            model.put("TypeMapper", new TypeMapperStaticMethods());
        } catch (Exception e) {
            // ignore
        }
        return model;
    }

    private void writeOrPreview(Path path, String content, boolean dryRun) throws IOException {
        if (dryRun) {
            System.out.println("--- " + path + " ---");
            System.out.println(content);
            System.out.println();
        } else {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            System.out.println("  Generated: " + path);
        }
    }

    private void printPostInstructions(GeneratorConfig config, String targetModule) {
        System.out.println();
        System.out.println("=== Post-generation steps ===");
        System.out.println("1. Update @MapperScan in Application class to include:");
        System.out.println("   com.oa.admin." + config.getModule() + ".mapper");
        System.out.println("2. Add permission seed data in sys_permission table for:");
        System.out.println("   " + config.getModule() + ":" + "<resource>:list/query/add/edit/remove");
        System.out.println("3. Create Vue frontend pages for the module (not auto-generated yet)");
    }

    public static class TypeMapperStaticMethods {
        public String getSimpleJavaType(String yamlType) {
            return TypeMapper.getSimpleJavaType(yamlType);
        }
    }
}
