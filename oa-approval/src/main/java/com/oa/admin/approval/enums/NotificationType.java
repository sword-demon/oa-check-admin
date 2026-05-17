package com.oa.admin.approval.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
/**
 * @author wxvirus
 */

@Getter
@AllArgsConstructor
public enum NotificationType {
    PENDING_TASK("pending_task"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CC_RECEIVED("cc_received"),
    TASK_TRANSFERRED("task_transferred"),
    INSTANCE_TERMINATED("instance_terminated");

    private final String code;
}
