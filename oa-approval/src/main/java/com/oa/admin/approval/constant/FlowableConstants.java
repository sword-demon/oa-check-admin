package com.oa.admin.approval.constant;
/**
 * @author wxvirus
 */

public final class FlowableConstants {
    public static final String VAR_INITIATOR = "initiator";
    public static final String VAR_APPROVAL_INSTANCE_ID = "approvalInstanceId";
    public static final String VAR_APPROVED = "approved";
    public static final String BPMN_SUFFIX = ".bpmn20.xml";
    public static final String BPMN_ALT_SUFFIX = ".bpmn";
    public static final String UEL_OR_SIGN_CONDITION = "nrOfCompletedInstances == 1";

    private FlowableConstants() {
    }
}
