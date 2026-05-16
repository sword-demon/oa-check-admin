package ${ctx.packageName}.mapper;

import ${ctx.packageName}.entity.${ctx.entity.name};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ${ctx.entity.comment!''} Mapper
 * @author ${ctx.config.author!'generator'}
 */
@Mapper
public interface ${ctx.entity.mapperName} extends BaseMapper<${ctx.entity.name}> {
}
