-- V12: Add BPMN DI information for seeded leave template so bpmn-js can render it.

UPDATE biz_process_template
SET bpmn_xml = REPLACE(
        REPLACE(
            bpmn_xml,
            '<definitions xmlns=',
            '<definitions xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             xmlns='
        ),
        '</definitions>',
        '
  <bpmndi:BPMNDiagram id="BPMNDiagram_leave_request">
    <bpmndi:BPMNPlane id="BPMNPlane_leave_request" bpmnElement="leave_request">
      <bpmndi:BPMNShape id="BPMNShape_start" bpmnElement="start">
        <omgdc:Bounds x="120" y="160" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_deptLeaderApprove" bpmnElement="deptLeaderApprove">
        <omgdc:Bounds x="240" y="138" width="120" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_gateway1" bpmnElement="gateway1" isMarkerVisible="true">
        <omgdc:Bounds x="430" y="153" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_endApproved" bpmnElement="endApproved">
        <omgdc:Bounds x="560" y="90" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_endRejected" bpmnElement="endRejected">
        <omgdc:Bounds x="560" y="230" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="BPMNEdge_flow1" bpmnElement="flow1">
        <omgdi:waypoint x="156" y="178"/>
        <omgdi:waypoint x="240" y="178"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flow2" bpmnElement="flow2">
        <omgdi:waypoint x="360" y="178"/>
        <omgdi:waypoint x="430" y="178"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flowApproved" bpmnElement="flowApproved">
        <omgdi:waypoint x="455" y="153"/>
        <omgdi:waypoint x="455" y="108"/>
        <omgdi:waypoint x="560" y="108"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flowRejected" bpmnElement="flowRejected">
        <omgdi:waypoint x="455" y="203"/>
        <omgdi:waypoint x="455" y="248"/>
        <omgdi:waypoint x="560" y="248"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>'
    ),
    published_bpmn_xml = REPLACE(
        REPLACE(
            published_bpmn_xml,
            '<definitions xmlns=',
            '<definitions xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             xmlns='
        ),
        '</definitions>',
        '
  <bpmndi:BPMNDiagram id="BPMNDiagram_leave_request">
    <bpmndi:BPMNPlane id="BPMNPlane_leave_request" bpmnElement="leave_request">
      <bpmndi:BPMNShape id="BPMNShape_start" bpmnElement="start">
        <omgdc:Bounds x="120" y="160" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_deptLeaderApprove" bpmnElement="deptLeaderApprove">
        <omgdc:Bounds x="240" y="138" width="120" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_gateway1" bpmnElement="gateway1" isMarkerVisible="true">
        <omgdc:Bounds x="430" y="153" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_endApproved" bpmnElement="endApproved">
        <omgdc:Bounds x="560" y="90" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="BPMNShape_endRejected" bpmnElement="endRejected">
        <omgdc:Bounds x="560" y="230" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="BPMNEdge_flow1" bpmnElement="flow1">
        <omgdi:waypoint x="156" y="178"/>
        <omgdi:waypoint x="240" y="178"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flow2" bpmnElement="flow2">
        <omgdi:waypoint x="360" y="178"/>
        <omgdi:waypoint x="430" y="178"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flowApproved" bpmnElement="flowApproved">
        <omgdi:waypoint x="455" y="153"/>
        <omgdi:waypoint x="455" y="108"/>
        <omgdi:waypoint x="560" y="108"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="BPMNEdge_flowRejected" bpmnElement="flowRejected">
        <omgdi:waypoint x="455" y="203"/>
        <omgdi:waypoint x="455" y="248"/>
        <omgdi:waypoint x="560" y="248"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>'
    ),
    updated_at = NOW()
WHERE template_key = 'leave_request'
  AND deleted = 0
  AND LOCATE('BPMNDiagram', COALESCE(published_bpmn_xml, '')) = 0;
