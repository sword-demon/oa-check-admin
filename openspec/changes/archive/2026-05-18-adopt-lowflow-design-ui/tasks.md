## 1. Component Structure

- [x] 1.1 Create `oa-ui/src/components/approval/lowflow/` component directory with nodes and panels subdirectories.
- [x] 1.2 Add `LowflowApprovalDesigner.vue` container that preserves the current `ApprovalFlowDesigner` props and `update:modelValue` contract.
- [x] 1.3 Replace `ApprovalFlowDesigner.vue` internals with the lowflow container while keeping the public import path unchanged.

## 2. Recursive Canvas

- [x] 2.1 Implement `FlowTreeNode.vue` to recursively render `node` and `node.next`.
- [x] 2.2 Implement `FlowNodeCard.vue` for shared node shell, title editing, delete action, selected state, and warning tooltip.
- [x] 2.3 Implement `FlowAddButton.vue` with approval, cc, and exclusive branch options; hide unsupported lowflow node types.
- [x] 2.4 Implement canvas scrolling and responsive overflow handling for large branch layouts.

## 3. Branch UI

- [x] 3.1 Implement `FlowGatewayNode.vue` with horizontal branch lanes and lowflow-style connector lines.
- [x] 3.2 Implement branch header rendering for branch name, default branch state, and condition summary.
- [x] 3.3 Implement add, delete, and reorder branch interactions while preserving at least two branches and one default branch.
- [x] 3.4 Implement branch child insertion so each branch stores its entry node in `children[0]` and continues through `next`.

## 4. Model Mutations

- [x] 4.1 Centralize chain helpers for inserting a node after a target, deleting a node by bypassing it, and appending branch child nodes.
- [x] 4.2 Ensure all designer mutations operate on `ApprovalFlowNode.next` and call `normalizeApprovalFlowModel()` for legacy `children` inputs.
- [x] 4.3 Preserve end-node protection so the end node cannot be deleted or followed by inserted nodes.
- [x] 4.4 Keep `generateApprovalFlowArtifacts()` output compatible with existing BPMN XML and node config save APIs.

## 5. Property Drawer

- [x] 5.1 Implement `FlowNodeDrawer.vue` to open on selected node or branch and render type-specific panels.
- [x] 5.2 Move existing approval assignee controls into `ApprovalNodePanel.vue` and keep current user/role loading behavior.
- [x] 5.3 Move existing cc recipient controls into `CcNodePanel.vue` or reuse approval assignee controls when behavior is identical.
- [x] 5.4 Move existing branch condition controls into `BranchConditionPanel.vue`, using dynamic form fields and current operators.
- [x] 5.5 Ensure inline node title editing and drawer title editing both update the same flow model.

## 6. Validation Feedback

- [x] 6.1 Add a designer-facing validation helper that maps flow validation errors to node IDs or branch IDs.
- [x] 6.2 Show warning markers on node cards and branch headers when mapped validation errors exist.
- [x] 6.3 Keep the existing aggregate validation message behavior for wizard next-step and publish actions.

## 7. Tests And Verification

- [x] 7.1 Add frontend unit tests for chain insertion, deletion, branch child append, and legacy model normalization.
- [x] 7.2 Add component tests or focused DOM tests for lowflow-style rendering of main chain, branch lanes, add menu, and validation warnings.
- [x] 7.3 Run `npm --prefix "oa-ui" test -- "src/utils/approval-flow.test.ts" "src/bpmn/bpmn-utils.test.ts"` and any new component tests.
- [x] 7.4 Run `npm --prefix "oa-ui" run build`.
- [x] 7.5 Manually verify template flow: create template -> design form -> design lowflow-style flow -> preview -> publish -> reopen template -> view generated diagram.

## 8. Documentation

- [x] 8.1 Update `docs/approval-template-designer.md` with lowflow-style UI structure, supported node types, and unsupported lowflow node types.
- [x] 8.2 Record migration behavior for old `children` drafts and the rollback path to the previous designer shell.
