package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.dto.DashboardStatsVO;
import com.oa.admin.approval.dto.InstanceDiagramVO;
import com.oa.admin.approval.dto.TaskVO;
import com.oa.admin.approval.entity.BizApprovalInstance;
import com.oa.admin.approval.entity.BizApprovalTask;
import com.oa.admin.common.result.PageResult;

import java.util.List;

public interface ApprovalService extends IService<BizApprovalInstance> {

    BizApprovalInstance submit(Long templateId, String title, String formData);

    void approve(Long taskId, int result, String comment);

    List<BizApprovalTask> myTodo();

    List<BizApprovalTask> myDone();

    void withdraw(Long instanceId);

    List<BizApprovalTask> instanceTasks(Long instanceId);

    PageResult<BizApprovalInstance> myApplications(String title, Integer status, long page, long pageSize);

    BizApprovalInstance getInstanceDetail(Long instanceId);

    InstanceDiagramVO getInstanceDiagram(Long instanceId);

    DashboardStatsVO dashboardStats();

    PageResult<TaskVO> myTodoPaged(String title, long page, long pageSize);

    PageResult<TaskVO> myDonePaged(String title, long page, long pageSize);

    void transfer(Long taskId, Long targetUserId, String reason);
}
