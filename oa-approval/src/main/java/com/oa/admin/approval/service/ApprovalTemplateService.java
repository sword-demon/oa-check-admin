package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;

import java.util.List;
/**
 * @author wxvirus
 */

public interface ApprovalTemplateService extends IService<BizProcessTemplate> {

    IPage<BizProcessTemplate> page(String templateName, Integer status, long page, long pageSize);

    BizProcessTemplate saveDraft(BizProcessTemplate template, List<BizProcessNodeConfig> nodeConfigs);

    BizProcessTemplate publish(Long id);

    BizProcessTemplate createNewVersion(Long id);

    BizProcessTemplate unpublish(Long id);

    List<BizProcessNodeConfig> getNodeConfigs(Long templateId);

    void saveNodeConfigs(Long templateId, List<BizProcessNodeConfig> configs);

    String getTemplateXml(Long templateId);

    void saveTemplateXml(Long templateId, String bpmnXml);
}
