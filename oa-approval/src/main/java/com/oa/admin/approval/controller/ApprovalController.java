package com.oa.admin.approval.controller;

import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import com.oa.admin.common.result.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // Template CRUD
    @SaCheckPermission("approval:template:list")
    @GetMapping("/template")
    public R<List<BizProcessTemplate>> listTemplates() {
        return R.ok(templateService.list());
    }

    @SaCheckPermission("approval:template:create")
    @PostMapping("/template")
    public R<BizProcessTemplate> createTemplate(@RequestBody BizProcessTemplate template) {
        templateService.save(template);
        return R.ok(template);
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
    public R<List<BizApprovalCc>> myCc() {
        return R.ok(ccService.myCc());
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
