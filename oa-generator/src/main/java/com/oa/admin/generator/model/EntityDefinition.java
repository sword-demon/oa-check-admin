package com.oa.admin.generator.model;

import com.oa.admin.generator.util.NamingUtils;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class EntityDefinition {
    private String name;
    private String tableName;
    private String comment;
    private List<FieldDefinition> fields;
    private List<IndexDefinition> indexes;

    public String getMapperName() {
        return name + "Mapper";
    }

    public String getServiceName() {
        return name + "Service";
    }

    public String getServiceImplName() {
        return name + "ServiceImpl";
    }

    public String getControllerName() {
        return name + "Controller";
    }

    public String getCreateDtoName() {
        return name + "CreateDTO";
    }

    public String getUpdateDtoName() {
        return name + "UpdateDTO";
    }

    public String getQueryDtoName() {
        return name + "QueryDTO";
    }

    public String getVoName() {
        return name + "VO";
    }

    public String getBeanName() {
        return NamingUtils.uncapitalize(name);
    }

    public String getResourcePath() {
        return NamingUtils.camelToSnake(name);
    }

    public List<FieldDefinition> getSearchableFields() {
        return fields.stream()
                .filter(FieldDefinition::isSearchable)
                .collect(Collectors.toList());
    }
}
