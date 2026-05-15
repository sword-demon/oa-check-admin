package com.oa.admin.approval.service.impl;

import java.util.List;

import com.oa.admin.approval.constant.FlowableConstants;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.service.ProcessDeployService;
import com.oa.admin.common.exception.BusinessException;
import com.oa.admin.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDeployServiceImpl implements ProcessDeployService {

    private final RepositoryService repositoryService;

    @Override
    @Transactional
    public String deployTemplate(BizProcessTemplate template) {
        String bpmnXml = template.getPublishedBpmnXml();
        if (bpmnXml == null || bpmnXml.isBlank()) {
            bpmnXml = template.getBpmnXml();
        }
        if (bpmnXml == null || bpmnXml.isBlank()) {
            throw new BusinessException(ErrorCode.BPMN_XML_INVALID);
        }

        String resourceName = template.getTemplateKey() + FlowableConstants.BPMN_SUFFIX;
        Deployment deployment = repositoryService.createDeployment()
            .name(template.getTemplateName())
            .addString(resourceName, bpmnXml)
            .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .latestVersion()
            .singleResult();

        if (processDefinition == null) {
            throw new BusinessException(ErrorCode.PROCESS_DEPLOY_FAILED);
        }

        log.info("Deployed template {}: deploymentId={}, processDefinitionId={}",
            template.getTemplateKey(), deployment.getId(), processDefinition.getId());

        return processDefinition.getId();
    }

    @Override
    public String getDeployedXml(String deploymentId) {
        try {
            List<String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
            return resourceNames.stream()
                .filter(name -> name.endsWith(FlowableConstants.BPMN_SUFFIX) || name.endsWith(FlowableConstants.BPMN_ALT_SUFFIX))
                .findFirst()
                .map(name -> {
                    try (var is = repositoryService.getResourceAsStream(deploymentId, name)) {
                        return new String(is.readAllBytes());
                    } catch (Exception e) {
                        log.error("Failed to read BPMN XML from deployment {}", deploymentId, e);
                        return null;
                    }
                })
                .orElse(null);
        } catch (Exception e) {
            log.warn("Deployment {} not found or already cleaned up", deploymentId);
            return null;
        }
    }
}
