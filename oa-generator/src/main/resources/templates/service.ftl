package ${ctx.packageName}.service;

import ${ctx.packageName}.dto.${ctx.entity.createDtoName};
import ${ctx.packageName}.dto.${ctx.entity.queryDtoName};
import ${ctx.packageName}.dto.${ctx.entity.updateDtoName};
import ${ctx.packageName}.entity.${ctx.entity.name};
import ${ctx.packageName}.vo.${ctx.entity.voName};
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.admin.common.result.PageResult;

/**
 * ${ctx.entity.comment!''} Service
 * @author ${ctx.config.author!'generator'}
 */
public interface ${ctx.entity.serviceName} extends IService<${ctx.entity.name}> {

    /**
     * 分页查询
     */
    PageResult<${ctx.entity.voName}> page(${ctx.entity.queryDtoName} query);

    /**
     * 详情查询
     */
    ${ctx.entity.voName} getDetail(Long id);

    /**
     * 新增
     */
    ${ctx.entity.voName} create(${ctx.entity.createDtoName} request);

    /**
     * 更新
     */
    ${ctx.entity.voName} update(Long id, ${ctx.entity.updateDtoName} request);

    /**
     * 删除
     */
    void delete(Long id);
}
