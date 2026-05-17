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
import com.oa.admin.approval.service.ApprovalFormSchemaService;
import com.oa.admin.approval.service.ProcessDeployService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @author wxvirus
 */

@Service
@RequiredArgsConstructor
public class ApprovalTemplateServiceImpl extends ServiceImpl<BizProcessTemplateMapper, BizProcessTemplate> implements ApprovalTemplateService {

    private final ProcessDeployService deployService;
    private final BizProcessNodeConfigMapper nodeConfigMapper;
    private final ApprovalFormSchemaService formSchemaService;

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
        formSchemaService.validateForSave(template.getFormConfig());
        template.setStatus(TemplateStatus.DRAFT.getCode());
        this.saveOrUpdate(template);

        if (nodeConfigs != null) {
            saveNodeConfigs(template.getId(), nodeConfigs);
        }

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
        List<String> publishErrors = new ArrayList<>();
        try {
            formSchemaService.validateForPublish(template.getFormConfig());
        } catch (BusinessException e) {
            publishErrors.add(e.getMessage());
        }
        publishErrors.addAll(validateFlowForPublish(template));
        if (!publishErrors.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), String.join("; ", publishErrors));
        }

        template.setPublishedBpmnXml(template.getBpmnXml());
        if (template.getVersion() == null) {
            template.setVersion(1);
        }

        String processDefinitionId;
        try {
            processDefinitionId = deployService.deployTemplate(template);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                ErrorCode.PROCESS_DEPLOY_FAILED.getCode(),
                ErrorCode.PROCESS_DEPLOY_FAILED.getMsg() + ": " + e.getMessage()
            );
        }
        template.setFlowableProcessDefinitionId(processDefinitionId);
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
        newDraft.setVersion(nextVersion(published.getVersion()));
        newDraft.setStatus(TemplateStatus.DRAFT.getCode());
        this.save(newDraft);
        cloneNodeConfigs(published.getId(), newDraft.getId());

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
        BizProcessTemplate template = this.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus().equals(TemplateStatus.PUBLISHED.getCode())) {
            throw new BusinessException(ErrorCode.TEMPLATE_ALREADY_PUBLISHED);
        }
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

    private void cloneNodeConfigs(Long sourceTemplateId, Long targetTemplateId) {
        if (sourceTemplateId == null || targetTemplateId == null) {
            return;
        }
        List<BizProcessNodeConfig> configs = getNodeConfigs(sourceTemplateId);
        if (configs == null || configs.isEmpty()) {
            return;
        }
        for (BizProcessNodeConfig config : configs) {
            BizProcessNodeConfig cloned = new BizProcessNodeConfig();
            cloned.setTemplateId(targetTemplateId);
            cloned.setNodeId(config.getNodeId());
            cloned.setNodeName(config.getNodeName());
            cloned.setNodeType(config.getNodeType());
            cloned.setAssigneeType(config.getAssigneeType());
            cloned.setAssigneeConfig(config.getAssigneeConfig());
            cloned.setMultiInstanceType(config.getMultiInstanceType());
            cloned.setCompletionRatio(config.getCompletionRatio());
            cloned.setCcConfig(config.getCcConfig());
            cloned.setSortOrder(config.getSortOrder());
            nodeConfigMapper.insert(cloned);
        }
    }

    private int nextVersion(Integer currentVersion) {
        return currentVersion == null ? 1 : currentVersion + 1;
    }

    private List<String> validateFlowForPublish(BizProcessTemplate template) {
        List<String> errors = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(template.getBpmnXml())));

            List<Element> starts = elementsByLocalName(doc, "startEvent");
            List<Element> ends = elementsByLocalName(doc, "endEvent");
            List<Element> userTasks = elementsByLocalName(doc, "userTask");
            List<Element> serviceTasks = elementsByLocalName(doc, "serviceTask");
            List<Element> exclusiveGateways = elementsByLocalName(doc, "exclusiveGateway");
            List<Element> parallelGateways = elementsByLocalName(doc, "parallelGateway");
            List<Element> flows = elementsByLocalName(doc, "sequenceFlow");
            validateDuplicateElementIds(errors, starts, ends, userTasks, serviceTasks, exclusiveGateways, parallelGateways, flows);

            if (starts.isEmpty()) {
                errors.add("流程缺少开始事件");
            }
            if (ends.isEmpty()) {
                errors.add("流程缺少结束事件");
            }

            Map<String, Integer> incomingCounts = new HashMap<>();
            Map<String, Integer> outgoingCounts = new HashMap<>();
            for (Element flow : flows) {
                String sourceRef = flow.getAttribute("sourceRef");
                String targetRef = flow.getAttribute("targetRef");
                if (!sourceRef.isBlank()) {
                    outgoingCounts.merge(sourceRef, 1, Integer::sum);
                }
                if (!targetRef.isBlank()) {
                    incomingCounts.merge(targetRef, 1, Integer::sum);
                }
            }

            for (Element start : starts) {
                String id = start.getAttribute("id");
                if (outgoingCounts.getOrDefault(id, 0) == 0) {
                    errors.add("开始事件没有出口连线: " + nodeName(start, "开始事件"));
                }
            }
            for (Element end : ends) {
                String id = end.getAttribute("id");
                if (incomingCounts.getOrDefault(id, 0) == 0) {
                    errors.add("结束事件没有入口连线: " + nodeName(end, "结束事件"));
                }
            }

            Map<String, BizProcessNodeConfig> configByNodeId = nodeConfigMap(template.getId());
            for (Element userTask : userTasks) {
                String id = userTask.getAttribute("id");
                if (incomingCounts.getOrDefault(id, 0) == 0) {
                    errors.add("用户任务没有入口连线: " + nodeName(userTask, id));
                }
                if (outgoingCounts.getOrDefault(id, 0) == 0) {
                    errors.add("用户任务没有出口连线: " + nodeName(userTask, id));
                }
                if (!hasAssigneeConfiguration(userTask, configByNodeId.get(id))) {
                    errors.add("审批节点未配置审批人: " + nodeName(userTask, id));
                }
            }
            for (Element serviceTask : serviceTasks) {
                String id = serviceTask.getAttribute("id");
                if (!hasServiceTaskImplementation(serviceTask)) {
                    errors.add("服务任务缺少执行实现: " + nodeName(serviceTask, id));
                }
            }

            validateGatewayOutgoing(errors, exclusiveGateways, outgoingCounts, "排他网关");
            validateGatewayOutgoing(errors, parallelGateways, outgoingCounts, "并行网关");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            errors.add("流程定义XML格式错误: " + e.getMessage());
        }
        return errors;
    }

    private Map<String, BizProcessNodeConfig> nodeConfigMap(Long templateId) {
        Map<String, BizProcessNodeConfig> configs = new HashMap<>();
        if (templateId == null) {
            return configs;
        }
        List<BizProcessNodeConfig> nodeConfigs = getNodeConfigs(templateId);
        if (nodeConfigs == null) {
            return configs;
        }
        for (BizProcessNodeConfig config : nodeConfigs) {
            if (config.getNodeId() != null) {
                configs.put(config.getNodeId(), config);
            }
        }
        return configs;
    }

    private void validateGatewayOutgoing(
        List<String> errors,
        List<Element> gateways,
        Map<String, Integer> outgoingCounts,
        String typeName
    ) {
        for (Element gateway : gateways) {
            String id = gateway.getAttribute("id");
            if (outgoingCounts.getOrDefault(id, 0) < 2) {
                errors.add(typeName + "至少需要 2 个出口: " + nodeName(gateway, id));
            }
        }
    }

    @SafeVarargs
    private final void validateDuplicateElementIds(List<String> errors, List<Element>... elementGroups) {
        Map<String, Integer> idCounts = new HashMap<>();
        for (List<Element> group : elementGroups) {
            for (Element element : group) {
                String id = element.getAttribute("id");
                if (id != null && !id.isBlank()) {
                    idCounts.merge(id, 1, Integer::sum);
                }
            }
        }
        idCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .sorted()
            .forEach(id -> errors.add("流程定义存在重复元素ID: " + id));
    }

    private boolean hasAssigneeConfiguration(Element userTask, BizProcessNodeConfig config) {
        if (config != null && config.getAssigneeType() != null && !config.getAssigneeType().isBlank()) {
            return true;
        }
        if (!userTask.getAttribute("flowable:assignee").isBlank()
            || !userTask.getAttribute("assignee").isBlank()) {
            return true;
        }
        return userTask.getElementsByTagNameNS("*", "extensionElements").getLength() > 0
            || userTask.getElementsByTagName("extensionElements").getLength() > 0;
    }

    private boolean hasServiceTaskImplementation(Element serviceTask) {
        return !attribute(serviceTask, "class").isBlank()
            || !attribute(serviceTask, "delegateExpression").isBlank()
            || !attribute(serviceTask, "type").isBlank()
            || !attribute(serviceTask, "operation").isBlank()
            || !attribute(serviceTask, "expression").isBlank();
    }

    private String attribute(Element element, String localName) {
        String value = element.getAttribute(localName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = element.getAttribute("flowable:" + localName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = element.getAttributeNS("http://flowable.org/bpmn", localName);
        return value == null ? "" : value;
    }

    private String nodeName(Element element, String fallback) {
        String name = element.getAttribute("name");
        return name == null || name.isBlank() ? fallback : name;
    }

    private List<Element> elementsByLocalName(Document doc, String localName) {
        Set<Element> seen = new HashSet<>();
        List<Element> elements = new ArrayList<>();
        NodeList namespaced = doc.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < namespaced.getLength(); i++) {
            Element element = (Element) namespaced.item(i);
            if (seen.add(element)) {
                elements.add(element);
            }
        }
        NodeList nonNamespaced = doc.getElementsByTagName(localName);
        for (int i = 0; i < nonNamespaced.getLength(); i++) {
            Element element = (Element) nonNamespaced.item(i);
            if (seen.add(element)) {
                elements.add(element);
            }
        }
        return elements;
    }
}
