package com.oa.admin.approval.service;

import com.oa.admin.approval.entity.BizProcessTemplate;

public interface ProcessDeployService {

    String deployTemplate(BizProcessTemplate template);

    String getDeployedXml(String deploymentId);
}
