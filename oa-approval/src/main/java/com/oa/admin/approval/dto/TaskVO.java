package com.oa.admin.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * @author wxvirus
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskVO {
    private Long id;
    private Long approvalInstanceId;
    private String flowableTaskId;
    private Long assigneeUserId;
    private String taskName;
    private Integer taskType;
    private Integer taskResult;
    private String taskComment;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    // Instance context (enriched)
    private String instanceTitle;
    private Long initiatorUserId;
    private Integer instanceStatus;
    private String formDataSummary;
}
