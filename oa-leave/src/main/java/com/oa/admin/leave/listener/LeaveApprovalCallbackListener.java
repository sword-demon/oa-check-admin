package com.oa.admin.leave.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.admin.common.event.ApprovalCompletedEvent;
import com.oa.admin.leave.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
/**
 * @author wxvirus
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveApprovalCallbackListener {

    private final LeaveRequestService leaveRequestService;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onApprovalCompleted(ApprovalCompletedEvent event) {
        Long leaveRequestId = extractLeaveRequestId(event.getFormData());
        if (leaveRequestId == null) {
            return;
        }
        log.info("Approval callback for leave request: leaveRequestId={}, result={}", leaveRequestId, event.getResult());
        leaveRequestService.onApprovalResult(leaveRequestId, event.getResult());
    }

    private Long extractLeaveRequestId(String formData) {
        if (formData == null || formData.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(formData);
            JsonNode idNode = node.get("leaveRequestId");
            return idNode != null ? idNode.asLong() : null;
        } catch (Exception e) {
            log.warn("Failed to parse formData for leaveRequestId: {}", formData, e);
            return null;
        }
    }
}
