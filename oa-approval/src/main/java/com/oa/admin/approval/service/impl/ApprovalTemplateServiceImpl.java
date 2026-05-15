package com.oa.admin.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.enums.TemplateStatus;
import com.oa.admin.approval.mapper.BizProcessNodeConfigMapper;
import com.oa.admin.approval.mapper.BizProcessTemplateMapper;
import com.oa.admin.approval.service.ApprovalTemplateService;
import com.oa.admin.approval.service.ProcessDeployService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalTemplateServiceImpl extends ServiceImpl<BizProcessTemplateMapper, BizProcessTemplate> implements ApprovalTemplateService {

    private final ProcessDeployService deployService;
    private final BizProcessNodeConfigMapper nodeConfigMapper;

    @Override
    public IPage<BizProcessTemplate> page(String templateName, Integer status, long page, long pageSize) {
        LambdaQueryWrapper<BizProcessTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(templateName != null && !templateName.isEmpty(), BizProcessTemplate::getTemplateName, templateName)
               .eq(status != null, BizProcessTemplate::getStatus, status)
               .orderByDesc(BizProcessTemplate::getCreatedAt);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    @Transactional
    public BizProcessTemplate saveDraft(BizProcessTemplate template, List<BizProcessNodeConfig> nodeConfigs) {
        if (template.getId() != null) {
            BizProcessTemplate existing = this.getById(template.getId());
            if (existing != null && existing.getStatus().equals(TemplateStatus.PUBLISHED.getCode())) {
                throw new BusinessException(ErrorCode.TEMPLATE_ALREADY_PUBLISHED);
            }
        }
        template.setStatus(TemplateStatus.DRAFT.getCode());
        this.saveOrUpdate(template);

        saveNodeConfigs(template.getId(), nodeConfigs);

        return template;
    }

    @Override
    @Transactional
    public BizProcessTemplate publish(Long id) {
        BizProcessTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus().equals(TemplateStatus.PUBLISHED.getCode())) {
            throw new BusinessException(ErrorCode.TEMPLATE_ALREADY_PUBLISHED);
        }
        if (template.getBpmnXml() == null || template.getBpmnXml().isBlank()) {
            throw new BusinessException(ErrorCode.BPMN_XML_INVALID);
        }

        template.setPublishedBpmnXml(template.getBpmnXml());

        String processDefinitionId = deployService.deployTemplate(template);
        template.setFlowableProcessDefinitionId(processDefinitionId);
        template.setVersion(template.getVersion() + 1);
        template.setStatus(TemplateStatus.PUBLISHED.getCode());
        this.updateById(template);

        return template;
    }

    @Override
    @Transactional
    public BizProcessTemplate createNewVersion(Long id) {
        BizProcessTemplate published = this.getById(id);
        if (published == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (!published.getStatus().equals(TemplateStatus.PUBLISHED.getCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        BizProcessTemplate newDraft = new BizProcessTemplate();
        newDraft.setTemplateName(published.getTemplateName());
        newDraft.setTemplateKey(published.getTemplateKey());
        newDraft.setFormConfig(published.getFormConfig());
        newDraft.setBpmnXml(published.getPublishedBpmnXml());
        newDraft.setVersion(published.getVersion());
        newDraft.setStatus(TemplateStatus.DRAFT.getCode());
        this.save(newDraft);

        return newDraft;
    }

    @Override
    public BizProcessTemplate unpublish(Long id) {
        BizProcessTemplate template = this.getById(id);
        if (template != null) {
            template.setStatus(TemplateStatus.DRAFT.getCode());
            this.updateById(template);
        }
        return template;
    }

    @Override
    public List<BizProcessNodeConfig> getNodeConfigs(Long templateId) {
        return nodeConfigMapper.selectList(
            new LambdaQueryWrapper<BizProcessNodeConfig>()
                .eq(BizProcessNodeConfig::getTemplateId, templateId)
                .orderByAsc(BizProcessNodeConfig::getSortOrder)
        );
    }

    @Override
    @Transactional
    public void saveNodeConfigs(Long templateId, List<BizProcessNodeConfig> configs) {
        nodeConfigMapper.delete(
            new LambdaQueryWrapper<BizProcessNodeConfig>()
                .eq(BizProcessNodeConfig::getTemplateId, templateId)
        );
        if (configs != null) {
            for (int i = 0; i < configs.size(); i++) {
                BizProcessNodeConfig config = configs.get(i);
                config.setId(null);
                config.setTemplateId(templateId);
                config.setSortOrder(i);
                nodeConfigMapper.insert(config);
            }
        }
    }

    @Override
    public String getTemplateXml(Long templateId) {
        BizProcessTemplate template = this.getById(templateId);
        return template != null ? template.getBpmnXml() : null;
    }

    @Override
    @Transactional
    public void saveTemplateXml(Long templateId, String bpmnXml) {
        BizProcessTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus().equals(TemplateStatus.PUBLISHED.getCode())) {
            throw new BusinessException(ErrorCode.TEMPLATE_ALREADY_PUBLISHED);
        }
        template.setBpmnXml(bpmnXml);
        this.updateById(template);
    }
}
