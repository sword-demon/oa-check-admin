package com.oa.admin.generator.model;

import com.oa.admin.generator.util.NamingUtils;
import lombok.Data;
/**
 * @author wxvirus
 */

@Data
public class FieldDefinition {
    private String name;
    private String type;
    private String column;
    private String sqlType;
    private boolean nullable = true;
    private String defaultValue;
    private String comment;
    private boolean searchable;
    private String enumRef;
    private boolean jsonFormat;

    public String getCapitalizedName() {
        return NamingUtils.capitalize(name);
    }

    public boolean isStringLike() {
        return "String".equals(type);
    }
}
