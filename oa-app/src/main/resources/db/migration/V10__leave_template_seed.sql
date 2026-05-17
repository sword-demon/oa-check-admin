-- V10: Seed leave approval template
INSERT INTO biz_process_template (template_name, template_key, bpmn_xml, published_bpmn_xml, version, status, created_at, updated_at, deleted)
VALUES (
    '请假审批',
    'leave_request',
    '<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://oa.admin/leave">
  <process id="leave_request" name="请假审批" isExecutable="true">
    <startEvent id="start" name="提交申请"/>
    <userTask id="deptLeaderApprove" name="部门负责人审批">
      <extensionElements>
        <flowable:taskListener event="create"
          class="com.oa.admin.approval.listener.AutoAssignDeptLeaderListener"/>
      </extensionElements>
    </userTask>
    <exclusiveGateway id="gateway1" name="审批结果"/>
    <endEvent id="endApproved" name="审批通过"/>
    <endEvent id="endRejected" name="审批驳回"/>
    <sequenceFlow id="flow1" sourceRef="start" targetRef="deptLeaderApprove"/>
    <sequenceFlow id="flow2" sourceRef="deptLeaderApprove" targetRef="gateway1"/>
    <sequenceFlow id="flowApproved" name="通过" sourceRef="gateway1" targetRef="endApproved">
      <conditionExpression xsi:type="tFormalExpression"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <![CDATA[${result == 1}]]>
      </conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="flowRejected" name="驳回" sourceRef="gateway1" targetRef="endRejected">
      <conditionExpression xsi:type="tFormalExpression"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <![CDATA[${result == 2}]]>
      </conditionExpression>
    </sequenceFlow>
  </process>
</definitions>',
    '<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://oa.admin/leave">
  <process id="leave_request" name="请假审批" isExecutable="true">
    <startEvent id="start" name="提交申请"/>
    <userTask id="deptLeaderApprove" name="部门负责人审批">
      <extensionElements>
        <flowable:taskListener event="create"
          class="com.oa.admin.approval.listener.AutoAssignDeptLeaderListener"/>
      </extensionElements>
    </userTask>
    <exclusiveGateway id="gateway1" name="审批结果"/>
    <endEvent id="endApproved" name="审批通过"/>
    <endEvent id="endRejected" name="审批驳回"/>
    <sequenceFlow id="flow1" sourceRef="start" targetRef="deptLeaderApprove"/>
    <sequenceFlow id="flow2" sourceRef="deptLeaderApprove" targetRef="gateway1"/>
    <sequenceFlow id="flowApproved" name="通过" sourceRef="gateway1" targetRef="endApproved">
      <conditionExpression xsi:type="tFormalExpression"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <![CDATA[${result == 1}]]>
      </conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="flowRejected" name="驳回" sourceRef="gateway1" targetRef="endRejected">
      <conditionExpression xsi:type="tFormalExpression"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <![CDATA[${result == 2}]]>
      </conditionExpression>
    </sequenceFlow>
  </process>
</definitions>',
    1, 2, NOW(), NOW(), 0
)
ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    bpmn_xml = VALUES(bpmn_xml),
    published_bpmn_xml = VALUES(published_bpmn_xml),
    version = VALUES(version),
    status = VALUES(status),
    updated_at = NOW();
