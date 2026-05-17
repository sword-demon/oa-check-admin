package com.oa.admin.approval.controller;

import com.oa.admin.approval.dto.CcVO;
import com.oa.admin.approval.dto.DashboardStatsVO;
import com.oa.admin.approval.dto.InstanceDiagramVO;
import com.oa.admin.approval.dto.TaskVO;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.admin.common.result.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * @author wxvirus
 */

@RestController
@RequestMapping("/api/v1/approval")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalService approvalService;
    private final ApprovalTemplateService templateService;
    private final ApprovalCcService ccService;

    @SaCheckPermission("approval:submit")
    @PostMapping("/submit")
    public R<BizApprovalInstance> submit(@RequestBody Map<String, Object> body) {
        Long templateId = parseLong(body.get("templateId"), "templateId");
        String title = (String) body.get("title");
        String formData = (String) body.get("formData");
        return R.ok(approvalService.submit(templateId, title, formData));
    }

    @SaCheckPermission("approval:approve")
    @PostMapping("/task/{taskId}/approve")
    public R<Void> approve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        int result = parseInt(body.get("result"), "result");
        String comment = (String) body.get("comment");
        approvalService.approve(taskId, result, comment);
        return R.ok();
    }

    @SaCheckPermission("approval:todo")
    @GetMapping("/my-todo")
    public R<List<BizApprovalTask>> myTodo() {
        return R.ok(approvalService.myTodo());
    }

    @SaCheckPermission("approval:done")
    @GetMapping("/my-done")
    public R<List<BizApprovalTask>> myDone() {
        return R.ok(approvalService.myDone());
    }

    @SaCheckPermission("approval:todo")
    @GetMapping("/my-todo/paged")
    public R<PageResult<TaskVO>> myTodoPaged(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(approvalService.myTodoPaged(title, page, pageSize));
    }

    @SaCheckPermission("approval:done")
    @GetMapping("/my-done/paged")
    public R<PageResult<TaskVO>> myDonePaged(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(approvalService.myDonePaged(title, page, pageSize));
    }

    @SaCheckPermission("approval:approve")
    @PostMapping("/task/{taskId}/transfer")
    public R<Void> transfer(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        Long targetUserId = parseLong(body.get("targetUserId"), "targetUserId");
        String reason = (String) body.get("reason");
        approvalService.transfer(taskId, targetUserId, reason);
        return R.ok();
    }

    @SaCheckPermission("approval:withdraw")
    @PostMapping("/{instanceId}/withdraw")
    public R<Void> withdraw(@PathVariable Long instanceId) {
        approvalService.withdraw(instanceId);
        return R.ok();
    }

    @SaCheckPermission("approval:instance:view")
    @GetMapping("/instance/{instanceId}/tasks")
    public R<List<BizApprovalTask>> instanceTasks(@PathVariable Long instanceId) {
        return R.ok(approvalService.instanceTasks(instanceId));
    }

    @SaCheckPermission("approval:instance:view")
    @GetMapping("/my-applications")
    public R<PageResult<BizApprovalInstance>> myApplications(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(approvalService.myApplications(title, status, page, pageSize));
    }

    @SaCheckPermission("approval:instance:view")
    @GetMapping("/instance/{instanceId}")
    public R<BizApprovalInstance> getInstanceDetail(@PathVariable Long instanceId) {
        return R.ok(approvalService.getInstanceDetail(instanceId));
    }

    @SaCheckPermission("approval:instance:view")
    @GetMapping("/instance/{instanceId}/diagram")
    public R<InstanceDiagramVO> getInstanceDiagram(@PathVariable Long instanceId) {
        return R.ok(approvalService.getInstanceDiagram(instanceId));
    }

    @SaCheckPermission("approval:dashboard")
    @GetMapping("/dashboard/stats")
    public R<DashboardStatsVO> dashboardStats() {
        return R.ok(approvalService.dashboardStats());
    }

    // Template CRUD
    @SaCheckPermission("approval:template:list")
    @GetMapping("/template")
    public R<PageResult<BizProcessTemplate>> listTemplates(
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        IPage<BizProcessTemplate> result = templateService.page(templateName, status, page, pageSize);
        return R.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @SaCheckPermission("approval:template:list")
    @GetMapping("/template/{id}")
    public R<BizProcessTemplate> getTemplate(@PathVariable Long id) {
        return R.ok(templateService.getById(id));
    }

    @SaCheckPermission("approval:template:create")
    @PostMapping("/template")
    public R<BizProcessTemplate> createTemplate(@RequestBody BizProcessTemplate template) {
        return R.ok(templateService.saveDraft(template, null));
    }

    @SaCheckPermission("approval:template:edit")
    @PutMapping("/template/{id}")
    public R<BizProcessTemplate> updateTemplate(@PathVariable Long id, @RequestBody BizProcessTemplate template) {
        template.setId(id);
        return R.ok(templateService.saveDraft(template, null));
    }

    @SaCheckPermission("approval:template:delete")
    @DeleteMapping("/template/{id}")
    public R<Void> deleteTemplate(@PathVariable Long id) {
        templateService.removeById(id);
        return R.ok();
    }

    @SaCheckPermission("approval:template:publish")
    @PostMapping("/template/{id}/publish")
    public R<BizProcessTemplate> publishTemplate(@PathVariable Long id) {
        return R.ok(templateService.publish(id));
    }

    @SaCheckPermission("approval:template:edit")
    @GetMapping("/template/{id}/xml")
    public R<String> getTemplateXml(@PathVariable Long id) {
        return R.ok(templateService.getTemplateXml(id));
    }

    @SaCheckPermission("approval:template:edit")
    @PutMapping("/template/{id}/xml")
    public R<Void> saveTemplateXml(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (body.get("bpmnXml") == null || body.get("bpmnXml").isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        templateService.saveTemplateXml(id, body.get("bpmnXml"));
        return R.ok();
    }

    @SaCheckPermission("approval:template:edit")
    @GetMapping("/template/{id}/node-config")
    public R<List<BizProcessNodeConfig>> getNodeConfigs(@PathVariable Long id) {
        return R.ok(templateService.getNodeConfigs(id));
    }

    @SaCheckPermission("approval:template:edit")
    @PutMapping("/template/{id}/node-config")
    public R<Void> saveNodeConfigs(@PathVariable Long id,
                                   @RequestBody List<BizProcessNodeConfig> configs) {
        templateService.saveNodeConfigs(id, configs);
        return R.ok();
    }

    @SaCheckPermission("approval:template:create")
    @PostMapping("/template/{id}/new-version")
    public R<BizProcessTemplate> createNewVersion(@PathVariable Long id) {
        return R.ok(templateService.createNewVersion(id));
    }

    @SaCheckPermission("approval:cc")
    @GetMapping("/cc")
    public R<List<CcVO>> myCc() {
        return R.ok(ccService.myCcWithDetails());
    }

    @SaCheckPermission("approval:cc")
    @PostMapping("/cc/{ccId}/read")
    public R<Void> markCcRead(@PathVariable Long ccId) {
        ccService.markRead(ccId);
        return R.ok();
    }

    private Long parseLong(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private int parseInt(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }
}
