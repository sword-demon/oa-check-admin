package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizProcessTemplateMapper;
import org.springframework.stereotype.Service;

@Service
public class ApprovalTemplateService extends ServiceImpl<BizProcessTemplateMapper, BizProcessTemplate> {

    public IPage<BizProcessTemplate> page(String templateName, Integer status, long page, long pageSize) {
        LambdaQueryWrapper<BizProcessTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(templateName != null && !templateName.isEmpty(), BizProcessTemplate::getTemplateName, templateName)
               .eq(status != null, BizProcessTemplate::getStatus, status)
               .orderByDesc(BizProcessTemplate::getCreatedAt);
        return this.page(new Page<>(page, pageSize), wrapper);
    }

    public BizProcessTemplate publish(Long id) {
        BizProcessTemplate template = this.getById(id);
        if (template != null) {
            template.setStatus(1);
            this.updateById(template);
        }
        return template;
    }

    public BizProcessTemplate unpublish(Long id) {
        BizProcessTemplate template = this.getById(id);
        if (template != null) {
            template.setStatus(0);
            this.updateById(template);
        }
        return template;
    }
}
