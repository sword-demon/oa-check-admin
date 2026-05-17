package com.oa.admin.generator.model;

import lombok.Data;

import java.util.List;
/**
 * @author wxvirus
 */

@Data
public class IndexDefinition {
    private String name;
    private List<String> columns;
    private boolean unique;
}
