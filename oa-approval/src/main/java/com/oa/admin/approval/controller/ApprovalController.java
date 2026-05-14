package com.oa.admin.approval.controller;

import com.oa.admin.approval.entity.BizApprovalCc;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.service.ApprovalCcService;
import com.oa.admin.approval.service.ApprovalService;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.common.result.R;
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

    @PostMapping("/submit")
    public R<BizApprovalInstance> submit(@RequestBody Map<String, Object> body) {
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String title = (String) body.get("title");
        String formData = (String) body.get("formData");
        return R.ok(approvalService.submit(templateId, title, formData));
    }

    @PostMapping("/task/{taskId}/approve")
    public R<Void> approve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        int result = Integer.parseInt(body.get("result").toString());
        String comment = (String) body.get("comment");
        approvalService.approve(taskId, result, comment);
        return R.ok();
    }

    @GetMapping("/my-todo")
    public R<List<BizApprovalTask>> myTodo() {
        return R.ok(approvalService.myTodo());
    }

    @GetMapping("/my-done")
    public R<List<BizApprovalTask>> myDone() {
        return R.ok(approvalService.myDone());
    }

    @PostMapping("/{instanceId}/withdraw")
    public R<Void> withdraw(@PathVariable Long instanceId) {
        approvalService.withdraw(instanceId);
        return R.ok();
    }

    // Template CRUD
    @GetMapping("/template")
    public R<List<BizProcessTemplate>> listTemplates() {
        return R.ok(templateService.list());
    }

    @PostMapping("/template")
    public R<BizProcessTemplate> createTemplate(@RequestBody BizProcessTemplate template) {
        templateService.save(template);
        return R.ok(template);
    }

    @GetMapping("/cc")
    public R<List<BizApprovalCc>> myCc() {
        return R.ok(ccService.myCc());
    }

    @PostMapping("/cc/{ccId}/read")
    public R<Void> markCcRead(@PathVariable Long ccId) {
        ccService.markRead(ccId);
        return R.ok();
    }
}
