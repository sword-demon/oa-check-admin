## Skill routing

When the user's request matches an available skill, invoke it via the Skill tool. When in doubt, invoke the skill.

Key routing rules:
- Product ideas/brainstorming -> invoke /office-hours
- Strategy/scope -> invoke /plan-ceo-review
- Architecture -> invoke /plan-eng-review
- Design system/plan review -> invoke /design-consultation or /plan-design-review
- Full review pipeline -> invoke /autoplan
- Bugs/errors -> invoke /investigate
- QA/testing site behavior -> invoke /qa or /qa-only
- Code review/diff check -> invoke /review
- Visual polish -> invoke /design-review
- Ship/deploy/PR -> invoke /ship or /land-and-deploy
- Save progress -> invoke /context-save
- Resume context -> invoke /context-restore

## Java Service 层规范

- 所有 Service 类必须先定义 interface, 再由对应的 `ServiceImpl` 实现类来实现
- 接口放在 `service/` 包下, 实现类放在 `service/impl/` 包下
- 命名规范: 接口 `XxxService`, 实现类 `XxxServiceImpl`
- Controller 和其他 Service 依赖注入时, 类型声明为接口而非实现类
- 对于继承 MyBatis-Plus `ServiceImpl<Mapper, Entity>` 的类, impl 类继续继承, interface 定义业务方法签名

## 业务字面量规范

- 业务代码中不得直接出现具有业务含义的字符串、数字、状态码、类型码、错误码等字面量
- 所有业务字面量必须通过以下方式统一定义和引用:
  - **常量类**: 用于错误码、魔术数字等固定值 (如 `ApprovalStatus.APPROVED`)
  - **枚举**: 用于状态码、类型码等有限集合 (如 `enum ApprovalStatus { PENDING, APPROVED, REJECTED }`)
  - **配置文件**: 用于可调整的业务参数 (如阈值、超时时间)
  - **数据字典**: 用于动态管理的业务分类 (通过字典表和字典服务)
- 判断条件中禁止硬编码: `if (status == 1)` 应改为 `if (status == ApprovalStatus.APPROVED.getCode())`
- 错误消息禁止内联拼接: `throw new RuntimeException("审批已通过")` 应改为 `throw new BusinessException(ErrorCode.APPROVAL_ALREADY_APPROVED)`
