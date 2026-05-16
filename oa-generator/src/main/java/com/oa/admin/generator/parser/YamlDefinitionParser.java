package com.oa.admin.generator.parser;

import com.oa.admin.generator.config.GeneratorConfig;
import com.oa.admin.generator.model.EntityDefinition;
import com.oa.admin.generator.model.EnumDefinition;
import com.oa.admin.generator.model.EnumValueDefinition;
import com.oa.admin.generator.model.FieldDefinition;
import com.oa.admin.generator.model.IndexDefinition;
import com.oa.admin.generator.util.NamingUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlDefinitionParser {

    private final Yaml yaml = new Yaml();

    public record ParseResult(
            GeneratorConfig config,
            List<EntityDefinition> entities,
            Map<String, EnumDefinition> enums
    ) {
    }

    public ParseResult parse(Path yamlFile) throws IOException {
        String content = Files.readString(yamlFile);
        Map<String, Object> root = yaml.load(content);
        return parse(root);
    }

    @SuppressWarnings("unchecked")
    public ParseResult parse(Map<String, Object> root) {
        GeneratorConfig config = parseGlobal((Map<String, Object>) root.get("global"));
        Map<String, EnumDefinition> enums = parseEnums((Map<String, Object>) root.get("enums"));
        List<EntityDefinition> entities = parseEntities(
                (List<Map<String, Object>>) root.get("entities"), config, enums);

        validateEntityEnumRefs(entities, enums);
        return new ParseResult(config, entities, enums);
    }

    @SuppressWarnings("unchecked")
    private GeneratorConfig parseGlobal(Map<String, Object> global) {
        GeneratorConfig config = new GeneratorConfig();
        if (global != null) {
            config.setModule((String) global.get("module"));
            config.setTablePrefix((String) global.get("tablePrefix"));
            config.setAuthor((String) global.get("author"));
            if (global.get("basePackage") != null) {
                config.setBasePackage((String) global.get("basePackage"));
            }
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    private Map<String, EnumDefinition> parseEnums(Map<String, Object> enumsSection) {
        Map<String, EnumDefinition> result = new LinkedHashMap<>();
        if (enumsSection == null) {
            return result;
        }
        for (Map.Entry<String, Object> entry : enumsSection.entrySet()) {
            Map<String, Object> enumMap = (Map<String, Object>) entry.getValue();
            EnumDefinition enumDef = new EnumDefinition();
            enumDef.setName(entry.getKey());
            enumDef.setType((String) enumMap.getOrDefault("type", "int"));

            Map<String, Object> valuesMap = (Map<String, Object>) enumMap.get("values");
            List<EnumValueDefinition> values = new ArrayList<>();
            if (valuesMap != null) {
                for (Map.Entry<String, Object> vEntry : valuesMap.entrySet()) {
                    EnumValueDefinition evd = new EnumValueDefinition();
                    evd.setName(vEntry.getKey());
                    if (vEntry.getValue() instanceof Map) {
                        Map<String, Object> valProps = (Map<String, Object>) vEntry.getValue();
                        evd.setCode(valProps.get("code"));
                        evd.setLabel((String) valProps.get("label"));
                    } else {
                        evd.setCode(vEntry.getValue());
                    }
                    values.add(evd);
                }
            }
            enumDef.setValues(values);
            result.put(entry.getKey(), enumDef);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EntityDefinition> parseEntities(
            List<Map<String, Object>> entitiesSection,
            GeneratorConfig config,
            Map<String, EnumDefinition> enums) {
        List<EntityDefinition> result = new ArrayList<>();
        if (entitiesSection == null) {
            return result;
        }
        for (Map<String, Object> entityMap : entitiesSection) {
            EntityDefinition entity = new EntityDefinition();
            for (Map.Entry<String, Object> e : entityMap.entrySet()) {
                entity.setName(e.getKey());
                Map<String, Object> props = (Map<String, Object>) e.getValue();
                entity.setTableName(resolveTableName(
                        (String) props.get("tableName"), e.getKey(), config));
                entity.setComment((String) props.get("comment"));
                entity.setFields(parseFields(
                        (List<Map<String, Object>>) props.get("fields")));
                entity.setIndexes(parseIndexes(
                        (List<Map<String, Object>>) props.get("indexes")));
                break;
            }
            result.add(entity);
        }
        return result;
    }

    private String resolveTableName(String explicitName, String entityName, GeneratorConfig config) {
        if (explicitName != null && !explicitName.isEmpty()) {
            return explicitName;
        }
        String prefix = config.getTablePrefix() != null ? config.getTablePrefix() : "";
        return prefix + NamingUtils.camelToSnake(entityName);
    }

    @SuppressWarnings("unchecked")
    private List<FieldDefinition> parseFields(List<Map<String, Object>> fieldsSection) {
        List<FieldDefinition> result = new ArrayList<>();
        if (fieldsSection == null) {
            return result;
        }
        for (Map<String, Object> fieldMap : fieldsSection) {
            FieldDefinition field = new FieldDefinition();
            field.setName((String) fieldMap.get("name"));
            field.setType((String) fieldMap.get("type"));
            field.setColumn(resolveColumn(
                    (String) fieldMap.get("column"), field.getName()));
            field.setSqlType((String) fieldMap.get("sqlType"));
            field.setNullable(!Boolean.FALSE.equals(fieldMap.get("nullable")));
            field.setDefaultValue(fieldMap.get("defaultValue") != null
                    ? fieldMap.get("defaultValue").toString() : null);
            field.setComment((String) fieldMap.get("comment"));
            field.setSearchable(Boolean.TRUE.equals(fieldMap.get("searchable")));
            field.setEnumRef((String) fieldMap.get("enum"));
            field.setJsonFormat(Boolean.TRUE.equals(fieldMap.get("jsonFormat")));
            validateFieldType(field);
            result.add(field);
        }
        return result;
    }

    private String resolveColumn(String explicitColumn, String fieldName) {
        if (explicitColumn != null && !explicitColumn.isEmpty()) {
            return explicitColumn;
        }
        return NamingUtils.camelToSnake(fieldName);
    }

    private void validateFieldType(FieldDefinition field) {
        if (!TypeMapper.isValidType(field.getType())) {
            throw new IllegalArgumentException(
                    "Invalid field type '" + field.getType() + "' on field '" + field.getName()
                            + "'. Valid types: " + TypeMapper.getValidTypes());
        }
    }

    @SuppressWarnings("unchecked")
    private List<IndexDefinition> parseIndexes(List<Map<String, Object>> indexesSection) {
        List<IndexDefinition> result = new ArrayList<>();
        if (indexesSection == null) {
            return result;
        }
        for (Map<String, Object> idxMap : indexesSection) {
            IndexDefinition idx = new IndexDefinition();
            idx.setName((String) idxMap.get("name"));
            idx.setColumns((List<String>) idxMap.get("columns"));
            idx.setUnique(Boolean.TRUE.equals(idxMap.get("unique")));
            result.add(idx);
        }
        return result;
    }

    private void validateEntityEnumRefs(List<EntityDefinition> entities, Map<String, EnumDefinition> enums) {
        for (EntityDefinition entity : entities) {
            for (FieldDefinition field : entity.getFields()) {
                if (field.getEnumRef() != null && !enums.containsKey(field.getEnumRef())) {
                    throw new IllegalArgumentException(
                            "Entity '" + entity.getName() + "' field '" + field.getName()
                                    + "' references undefined enum '" + field.getEnumRef() + "'");
                }
            }
        }
    }
}
