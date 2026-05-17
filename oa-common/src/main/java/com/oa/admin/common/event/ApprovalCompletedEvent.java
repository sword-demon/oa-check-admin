package com.oa.admin.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring event published when an approval process completes.
 * Business modules can listen to this event to sync their status.
 * @author wxvirus
 */
public class ApprovalCompletedEvent extends ApplicationEvent {

    private final Long approvalInstanceId;
    private final String formData;
    private final int result; // 1=approved, 2=rejected

    public ApprovalCompletedEvent(Object source, Long approvalInstanceId, String formData, int result) {
        super(source);
        this.approvalInstanceId = approvalInstanceId;
        this.formData = formData;
        this.result = result;
    }

    public Long getApprovalInstanceId() {
        return approvalInstanceId;
    }

    public String getFormData() {
        return formData;
    }

    public int getResult() {
        return result;
    }
}
