## 1. Schema And Backend Validation

- [x] 1.1 Define approval form schema DTOs/constants for field types, validation rules, option fields, and schema version.
- [x] 1.2 Implement legacy `form_config` compatibility parser for existing `fields` JSON.
- [x] 1.3 Add backend form schema validation for save and publish paths.
- [x] 1.4 Add backend submitted form data validation against published schema.
- [x] 1.5 Add unit tests for schema parsing, legacy compatibility, required fields, numeric ranges, and option validation.

## 2. Template Version And Publishing

- [x] 2.1 Ensure published template versions keep immutable form schema, BPMN XML, and node configuration snapshots.
- [x] 2.2 Update approval submit flow to start by the template version's Flowable process definition ID.
- [x] 2.3 Add publish validation that aggregates form errors and flow errors with actionable node or field names.
- [x] 2.4 Verify new-version creation preserves original published version and creates editable draft version +1.
- [x] 2.5 Add backend tests for publish, new version, historical instance rendering, and invalid template submission.

## 3. Dynamic Form Builder UI

- [x] 3.1 Create form schema TypeScript types and field type registry in `oa-ui`.
- [x] 3.2 Build dynamic form designer layout with field palette, form canvas, and field property panel.
- [x] 3.3 Implement add, edit, delete, duplicate, and reorder interactions for form fields.
- [x] 3.4 Implement field property editors for labels, placeholders, defaults, required flag, options, and numeric validation.
- [x] 3.5 Build reusable runtime renderer for preview, approval submission, and approval detail display.
- [x] 3.6 Add frontend tests for schema normalization, field validation rules, and runtime rendering.

## 4. Dingtalk-Style Flow Designer UI

- [x] 4.1 Define business flow node model for start, approval, cc, exclusive branch, parallel branch, and end nodes.
- [x] 4.2 Build DingTalk-style flow canvas with node insertion, deletion, naming, and selection.
- [x] 4.3 Implement approval node assignee panel for fixed users, department leader, upward leader, role, initiator, and expression strategies.
- [x] 4.4 Implement condition branch editor based on dynamic form fields with default branch support.
- [x] 4.5 Generate BPMN XML and node configs from the business flow node model.
- [x] 4.6 Add frontend tests for flow validation, BPMN generation, task listener injection, and branch expression generation.

## 5. Template Creation Wizard

- [x] 5.1 Replace or extend the template create entry with a wizard: basic info, form design, flow design, preview, publish.
- [x] 5.2 Persist draft state from each wizard step without losing form schema or flow node model.
- [x] 5.3 Add preview page combining dynamic form preview and generated flow preview.
- [x] 5.4 Keep existing BPMN XML preview/edit affordance available for advanced troubleshooting.
- [x] 5.5 Add route and permission checks consistent with existing approval template permissions.

## 6. Approval Runtime Integration

- [x] 6.1 Update generic approval submission UI to render dynamic form schema for selected template.
- [x] 6.2 Update approval instance detail to render submitted form data using the bound template version schema.
- [x] 6.3 Ensure condition branch variables are populated from submitted form data before Flowable process start.
- [x] 6.4 Add fallback rendering for extra formData keys not present in schema.
- [x] 6.5 Add integration tests for submit dynamic approval, condition routing, and historical detail rendering after new version publish.

## 7. Verification And Documentation

- [x] 7.1 Run backend unit tests for `oa-approval` and affected modules.
- [x] 7.2 Run frontend unit tests and production build for `oa-ui`.
- [x] 7.3 Manually verify create template -> design form -> design flow -> publish -> submit -> approve -> view detail.
- [x] 7.4 Document the form schema structure and supported field/node types in project docs.
- [x] 7.5 Record known limitations for attachment storage, advanced expressions, and complex conditional logic.
