package com.oa.admin.generator.config;

import lombok.Data;
/**
 * @author wxvirus
 */

@Data
public class GeneratorConfig {
    private String module;
    private String tablePrefix;
    private String author;
    private String basePackage = "com.oa.admin";

    public String getFullPackage() {
        return basePackage + "." + module;
    }
}
