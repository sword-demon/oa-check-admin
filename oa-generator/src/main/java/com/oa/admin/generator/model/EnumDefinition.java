package com.oa.admin.generator.model;

import lombok.Data;

import java.util.List;
/**
 * @author wxvirus
 */

@Data
public class EnumDefinition {
    private String name;
    private String type = "int";
    private List<EnumValueDefinition> values;

    public boolean hasLabels() {
        return values != null && values.stream().allMatch(v -> v.getLabel() != null);
    }

    public boolean isIntType() {
        return "int".equals(type);
    }
}
