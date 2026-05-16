package com.oa.admin.generator.model;

import lombok.Data;

import java.util.List;

@Data
public class IndexDefinition {
    private String name;
    private List<String> columns;
    private boolean unique;
}
