package com.oa.admin.approval.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.admin.approval.entity.BizProcessTemplate;
import com.oa.admin.approval.mapper.BizProcessTemplateMapper;
import org.springframework.stereotype.Service;

@Service
public class ApprovalTemplateService extends ServiceImpl<BizProcessTemplateMapper, BizProcessTemplate> {
}
