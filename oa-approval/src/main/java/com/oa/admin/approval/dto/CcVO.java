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
public class CcVO {
    private Long id;
    private Long approvalInstanceId;
    private String instanceTitle;
    private Integer instanceStatus;
    private Long ccUserId;
    private String ccReason;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
