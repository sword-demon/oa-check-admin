package com.oa.admin.approval.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.approval.dto.AdminMetricsVO;
import com.oa.admin.approval.dto.InstanceVO;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * @author wxvirus
 */

@RestController
@RequestMapping("/api/v1/admin/approval")
@RequiredArgsConstructor
public class AdminApprovalController {
    private final ApprovalService approvalService;

    @SaCheckPermission("admin:approval:list")
    @GetMapping("/instances")
    public R<PageResult<InstanceVO>> adminInstances(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long initiatorUserId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(approvalService.adminInstances(title, status, templateId,
            initiatorUserId, startTime, endTime, page, pageSize));
    }

    @SaCheckPermission("admin:approval:terminate")
    @PostMapping("/instances/{instanceId}/terminate")
    public R<Void> terminateInstance(@PathVariable Long instanceId) {
        approvalService.terminateInstance(instanceId);
        return R.ok();
    }

    @SaCheckPermission("admin:approval:reassign")
    @PostMapping("/tasks/{taskId}/reassign")
    public R<Void> reassignTask(@PathVariable Long taskId, @RequestParam Long targetUserId) {
        approvalService.reassignTask(taskId, targetUserId);
        return R.ok();
    }

    @SaCheckPermission("admin:approval:list")
    @GetMapping("/metrics")
    public R<AdminMetricsVO> metrics() {
        return R.ok(approvalService.metrics());
    }
}
