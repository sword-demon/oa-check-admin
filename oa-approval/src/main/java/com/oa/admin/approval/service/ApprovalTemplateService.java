package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizProcessNodeConfig;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizProcessNodeConfigMapper;
import com.oa.admin.approval.mapper.BizProcessTemplateMapper;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalTemplateService extends ServiceImpl<BizProcessTemplateMapper, BizProcessTemplate> {

    private final ProcessDeployService deployService;
    private final BizProcessNodeConfigMapper nodeConfigMapper;

    public IPage<BizProcessTemplate> page(String templateName, Integer status, long page, long pageSize) {
        LambdaQueryWrapper<BizProcessTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(templateName != null && !templateName.isEmpty(), BizProcessTemplate::getTemplateName, templateName)
               .eq(status != null, BizProcessTemplate::getStatus, status)
               .orderByDesc(BizProcessTemplate::getCreatedAt);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    @Transactional
    public BizProcessTemplate saveDraft(BizProcessTemplate template, List<BizProcessNodeConfig> nodeConfigs) {
        if (template.getId() != null) {
            BizProcessTemplate existing = this.getById(template.getId());
            if (existing != null && existing.getStatus() == 2) {
                throw new BusinessException(ErrorCode.TEMPLATE_ALREADY_PUBLISHED);
            }
        }
        template.setStatus(1); // draft
        this.saveOrUpdate(template);

        // Save node configs
        saveNodeConfigs(template.getId(), nodeConfigs);

        return template;
    }

    @Transactional
    public BizProcessTemplate publish(Long id) {
        BizProcessTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus() == 2) {
            throw new BusinessException(ErrorCode.TEMPLATE_ALREADY_PUBLISHED);
        }
        if (template.getBpmnXml() == null || template.getBpmnXml().isBlank()) {
            throw new BusinessException(ErrorCode.BPMN_XML_INVALID);
        }

        // Copy draft to published (immutable snapshot)
        template.setPublishedBpmnXml(template.getBpmnXml());

        // Deploy to Flowable
        String processDefinitionId = deployService.deployTemplate(template);
        template.setFlowableProcessDefinitionId(processDefinitionId);
        template.setVersion(template.getVersion() + 1);
        template.setStatus(2); // published
        this.updateById(template);

        return template;
    }

    @Transactional
    public BizProcessTemplate createNewVersion(Long id) {
        BizProcessTemplate published = this.getById(id);
        if (published == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (published.getStatus() != 2) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        BizProcessTemplate newDraft = new BizProcessTemplate();
        newDraft.setTemplateName(published.getTemplateName());
        newDraft.setTemplateKey(published.getTemplateKey());
        newDraft.setFormConfig(published.getFormConfig());
        newDraft.setBpmnXml(published.getPublishedBpmnXml());
        newDraft.setVersion(published.getVersion());
        newDraft.setStatus(1); // draft
        this.save(newDraft);

        return newDraft;
    }

    public BizProcessTemplate unpublish(Long id) {
        BizProcessTemplate template = this.getById(id);
        if (template != null) {
            template.setStatus(1); // back to draft
            this.updateById(template);
        }
        return template;
    }

    public List<BizProcessNodeConfig> getNodeConfigs(Long templateId) {
        return nodeConfigMapper.selectList(
            new LambdaQueryWrapper<BizProcessNodeConfig>()
                .eq(BizProcessNodeConfig::getTemplateId, templateId)
                .orderByAsc(BizProcessNodeConfig::getSortOrder)
        );
    }

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
}
