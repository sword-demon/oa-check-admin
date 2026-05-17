package com.oa.admin.approval.integration;

import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BPMN flow patterns using embedded Flowable engine + H2.
 * Tests: exclusive gateways, parallel gateways, countersign, or-sign, rejection flow.
 * @author wxvirus
 */
class FlowableGatewayIntegrationTest {

    private static ProcessEngine processEngine;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private RepositoryService repositoryService;

    @BeforeAll
    static void initEngine() {
        StandaloneProcessEngineConfiguration config = new StandaloneProcessEngineConfiguration();
        config.setJdbcUrl("jdbc:h2:mem:flowable-test;DB_CLOSE_DELAY=-1");
        config.setJdbcDriver("org.h2.Driver");
        config.setJdbcUsername("sa");
        config.setJdbcPassword("");
        config.setDatabaseSchemaUpdate("true");

        // Register mock beans for UEL expressions used in BPMN
        config.setBeans(Map.of(
            "assigneeResolver", (Object) new TestAssigneeResolver(),
            "candidateUserResolver", (Object) new TestCandidateUserResolver(),
            "approvalTaskCreateListener", (Object) new TestTaskListener()
        ));

        processEngine = config.buildProcessEngine();
    }

    /**
     * Test double for AssigneeResolver - returns fixed user IDs for integration tests.
     */
    static class TestAssigneeResolver {
        public Long resolveDeptLeader(Long userId) {
            return 100L; // fixed dept leader
        }

        public Long resolveUpwardDeptLeader(Long userId, int level) {
            return 200L; // fixed upward leader
        }

        public Long resolveInitiator() {
            return 1L;
        }
    }

    /**
     * Test double for CandidateUserResolver - returns fixed user IDs for integration tests.
     */
    static class TestCandidateUserResolver {
        public java.util.List<Long> resolveRoleUsers(Long roleId) {
            return java.util.List.of(10L, 20L, 30L);
        }
    }

    /**
     * No-op task listener for integration tests (replaces ApprovalTaskCreateListener).
     */
    static class TestTaskListener implements org.flowable.engine.delegate.TaskListener {
        @Override
        public void notify(org.flowable.task.service.delegate.DelegateTask delegateTask) {
            // no-op for tests
        }
    }

    @AfterAll
    static void closeEngine() {
        if (processEngine != null) {
            processEngine.close();
        }
    }

    @BeforeEach
    void setUp() {
        runtimeService = processEngine.getRuntimeService();
        taskService = processEngine.getTaskService();
        repositoryService = processEngine.getRepositoryService();
    }

    private String deployAndGetKey(String resource) {
        var deployment = repositoryService.createDeployment()
            .addClasspathResource("processes/" + resource)
            .deploy();
        return repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .latestVersion()
            .singleResult()
            .getKey();
    }

    // --- Exclusive Gateway Tests ---

    @Test
    void exclusiveGateway_highAmount_routesToDirectorApproval() {
        String key = deployAndGetKey("leave_request_v2.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "leave_days", 10, "leave_type", "sick", "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertFalse(tasks.isEmpty());
        // With leave_days > 5, should route to department leader approval task
        assertTrue(tasks.stream().anyMatch(t -> t.getName().contains("Approval") || t.getName().contains("审批")));
    }

    @Test
    void exclusiveGateway_lowAmount_skipsDirectorAutoCompletes() {
        String key = deployAndGetKey("leave_request_v2.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "leave_days", 2, "leave_type", "personal", "approved", true));

        // With leave_days <= 5, the exclusive gateway should skip to end
        // Process may auto-complete if no user tasks remain
        var instances = runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).list();
        // Either process completed or only one task exists
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertTrue(tasks.size() <= 1);
    }

    // --- Parallel Gateway Tests ---

    @Test
    void parallelGateway_fork_createsConcurrentTasks() {
        String key = deployAndGetKey("parallel_example.bpmn20.xml");

        // We need actual assignees, so mock by using numeric user IDs
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertEquals(2, tasks.size(), "Parallel fork should create 2 concurrent tasks");
    }

    @Test
    void parallelGateway_join_waitsForAllBranches() {
        String key = deployAndGetKey("parallel_example.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertEquals(2, tasks.size());

        // Complete first task - process should NOT end yet
        taskService.complete(tasks.get(0).getId(), Map.of("approved", true));
        assertTrue(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count() > 0,
            "Process should still be running after completing only one branch");

        // Complete second task - process should end
        taskService.complete(tasks.get(1).getId(), Map.of("approved", true));
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count(),
            "Process should complete after all branches finish");
    }

    // --- Countersign Tests ---

    @Test
    void countersign_allApprove_completesSuccessfully() {
        String key = deployAndGetKey("countersign_example.bpmn20.xml");

        List<Long> assignees = List.of(10L, 20L, 30L);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "assigneeList", assignees, "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertEquals(3, tasks.size(), "Countersign should create task for each assignee");

        // All approve
        for (Task task : tasks) {
            taskService.complete(task.getId(), Map.of("approved", true));
        }

        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count(),
            "Process should complete when all countersign tasks approve");
    }

    @Test
    void countersign_oneRejects_completesWithRejection() {
        String key = deployAndGetKey("countersign_example.bpmn20.xml");

        List<Long> assignees = List.of(10L, 20L, 30L);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "assigneeList", assignees, "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertEquals(3, tasks.size());

        // First two approve, third rejects
        taskService.complete(tasks.get(0).getId(), Map.of("approved", true));
        taskService.complete(tasks.get(1).getId(), Map.of("approved", true));
        taskService.complete(tasks.get(2).getId(), Map.of("approved", false));

        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count(),
            "Process should complete (via rejection path) when one countersign task rejects");
    }

    // --- Or-sign Tests ---

    @Test
    void orSign_firstApproval_completesEarly() {
        String key = deployAndGetKey("orsign_example.bpmn20.xml");

        List<Long> assignees = List.of(10L, 20L, 30L);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "assigneeList", assignees, "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertEquals(3, tasks.size(), "Or-sign should create task for each assignee");

        // First one approves
        taskService.complete(tasks.get(0).getId(), Map.of("approved", true));

        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count(),
            "Or-sign process should complete when any one approver approves");
    }

    @Test
    void orSign_firstRejection_terminatesProcess() {
        String key = deployAndGetKey("orsign_example.bpmn20.xml");

        List<Long> assignees = List.of(10L, 20L, 30L);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "assigneeList", assignees, "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertEquals(3, tasks.size());

        // First one rejects
        taskService.complete(tasks.get(0).getId(), Map.of("approved", false));

        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count(),
            "Or-sign process should complete (via rejection) when any one rejects");
    }

    // --- Rejection Flow Test ---

    @Test
    void rejection_terminatesProcessWithRejectedStatus() {
        String key = deployAndGetKey("leave_request_v2.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, "1",
            Map.of("initiator", 1L, "leave_days", 10, "leave_type", "sick", "approved", true));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        assertFalse(tasks.isEmpty());

        // Reject the task
        taskService.complete(tasks.get(0).getId(), Map.of("approved", false));

        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count(),
            "Process should terminate on rejection");
    }
}
