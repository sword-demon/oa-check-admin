package com.oa.admin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalPermissionSeedDataTest {

    private static final Path APPROVAL_CONTROLLER = Path.of(
            "..",
            "oa-approval",
            "src",
            "main",
            "java",
            "com",
            "oa",
            "admin",
            "approval",
            "controller",
            "ApprovalController.java"
    );
    private static final Path LEAVE_CONTROLLER = Path.of(
            "..",
            "oa-leave",
            "src",
            "main",
            "java",
            "com",
            "oa",
            "admin",
            "leave",
            "controller",
            "LeaveRequestController.java"
    );
    private static final Path SEED_DATA = Path.of("src", "main", "resources", "db", "migration", "V3__seed_data.sql");

    @Test
    void seedDataContainsAllApprovalControllerPermissionsAndGrantsAdminRole() throws IOException {
        assertSeedPermissions(APPROVAL_CONTROLLER, "approval");
    }

    @Test
    void seedDataContainsAllLeaveControllerPermissionsAndGrantsAdminRole() throws IOException {
        assertSeedPermissions(LEAVE_CONTROLLER, "leave");
    }

    private void assertSeedPermissions(Path controllerPath, String permissionPrefix) throws IOException {
        String seedSql = Files.readString(SEED_DATA);
        Set<String> controllerPermissions = extractPermissions(Files.readString(controllerPath), permissionPrefix);
        Set<String> seedPermissions = extractSeedPermissions(seedSql, permissionPrefix);

        assertFalse(controllerPermissions.isEmpty(), controllerPath + " should declare " + permissionPrefix + " permissions");
        assertEquals(controllerPermissions, seedPermissions);

        int lastApprovalPermission = seedSql.lastIndexOf("'" + permissionPrefix + ":");
        int adminGrant = seedSql.indexOf("SELECT 1, id FROM sys_permission");
        assertTrue(adminGrant > lastApprovalPermission, "admin role grant must run after " + permissionPrefix + " permission inserts");
    }

    private Set<String> extractPermissions(String source, String permissionPrefix) {
        Set<String> permissions = new TreeSet<>();
        Pattern pattern = Pattern.compile("@SaCheckPermission\\(\"(" + permissionPrefix + ":[^\"]+)\"\\)");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            permissions.add(matcher.group(1));
        }
        return permissions;
    }

    private Set<String> extractSeedPermissions(String seedSql, String permissionPrefix) {
        Set<String> permissions = new TreeSet<>();
        Pattern pattern = Pattern.compile("'(" + permissionPrefix + ":[^']+)'");
        Matcher matcher = pattern.matcher(seedSql);
        while (matcher.find()) {
            permissions.add(matcher.group(1));
        }
        return permissions;
    }
}
