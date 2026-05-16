package com.oa.admin.generator.model;

import com.oa.admin.generator.config.GeneratorConfig;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GenerationContext {
    private GeneratorConfig config;
    private EntityDefinition entity;
    private EnumDefinition enumDef;
    private Map<String, EnumDefinition> enums;
    private List<EntityDefinition> allEntities;
    private String packageName;
    private String flywayVersion;
    private String flywayDescription;
}
