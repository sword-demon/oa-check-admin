package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;

import java.util.List;

public interface ApprovalService extends IService<BizApprovalInstance> {

    BizApprovalInstance submit(Long templateId, String title, String formData);

    void approve(Long taskId, int result, String comment);

    List<BizApprovalTask> myTodo();

    List<BizApprovalTask> myDone();

    void withdraw(Long instanceId);

    List<BizApprovalTask> instanceTasks(Long instanceId);
}
