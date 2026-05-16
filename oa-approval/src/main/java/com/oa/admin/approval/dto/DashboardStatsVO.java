package com.oa.admin.approval.dto;

import com.oa.admin.approval.entity.BizApprovalTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsVO {
    private long todoCount;
    private long doneCount;
    private long templateCount;
    private long unreadCcCount;
    private List<BizApprovalTask> recentActivities;
}
