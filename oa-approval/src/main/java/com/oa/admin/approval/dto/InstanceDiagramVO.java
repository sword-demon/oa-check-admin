package com.oa.admin.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceDiagramVO {
    private String bpmnXml;
    private List<String> completedNodeIds;
    private List<String> currentNodeIds;
}
