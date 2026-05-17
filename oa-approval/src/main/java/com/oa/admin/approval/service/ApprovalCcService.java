package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.dto.CcVO;
import com.oa.admin.approval.entity.BizApprovalCc;

import java.util.List;
/**
 * @author wxvirus
 */

public interface ApprovalCcService extends IService<BizApprovalCc> {

    List<BizApprovalCc> myCc();

    List<CcVO> myCcWithDetails();

    void markRead(Long ccId);

    void createCc(Long instanceId, List<Long> ccUserIds, String reason);
}
