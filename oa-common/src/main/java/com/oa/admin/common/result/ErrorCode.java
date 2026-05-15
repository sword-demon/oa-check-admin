package com.oa.admin.common.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // System (10xx)
    SYSTEM_ERROR(1001, "系统异常"),
    PARAM_ERROR(1002, "参数校验失败"),
    NOT_FOUND(1003, "资源不存在"),

    // Auth (20xx)
    UNAUTHORIZED(2001, "未登录或登录已过期"),
    FORBIDDEN(2002, "无权限访问"),
    LOGIN_FAILED(2003, "用户名或密码错误"),
    TOKEN_EXPIRED(2004, "登录已过期, 请重新登录"),

    // Approval (30xx)
    TEMPLATE_NOT_FOUND(3001, "审批模板不存在"),
    INSTANCE_NOT_FOUND(3002, "审批实例不存在"),
    TASK_NOT_FOUND(3003, "审批任务不存在"),
    ALREADY_APPROVED(3004, "该任务已处理"),
    CANNOT_WITHDRAW(3005, "当前状态不允许撤回"),
    BPMN_XML_INVALID(3006, "流程定义XML格式错误"),
    TEMPLATE_ALREADY_PUBLISHED(3007, "模板已发布, 不可修改"),
    PROCESS_DEPLOY_FAILED(3008, "流程部署失败"),
    NODE_CONFIG_MISSING(3009, "流程节点配置缺失"),
    USER_NOT_FOUND_OR_NO_DEPT(3010, "用户不存在或未分配部门"),
    DEPT_NOT_FOUND_OR_NO_LEADER(3011, "部门不存在或未设置负责人"),
    DEPT_TOP_REACHED(3012, "已到达顶级部门, 无法继续向上"),
    TARGET_DEPT_NOT_FOUND_OR_NO_LEADER(3013, "目标部门不存在或未设置负责人"),
    TEMPLATE_NOT_PUBLISHED(3014, "模板未发布, 无法发起审批");

    private final int code;
    private final String msg;
}
