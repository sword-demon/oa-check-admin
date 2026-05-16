package com.oa.admin.approval.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminMetricsVO {
    private long totalInstances;
    private long pendingInstances;
    private long approvedInstances;
    private long rejectedInstances;
    private long withdrawnInstances;
    private double avgDurationHours;
    private List<TemplateMetric> templateMetrics;

    @Data
    @Builder
    public static class TemplateMetric {
        private Long templateId;
        private String templateName;
        private long total;
        private long pending;
        private long approved;
        private long rejected;
    }
}
