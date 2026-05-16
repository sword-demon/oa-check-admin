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
    private static final Path SEED_DATA = Path.of("src", "main", "resources", "db", "migration", "V3__seed_data.sql");
    private static final Pattern APPROVAL_PERMISSION_PATTERN = Pattern.compile(
            "@SaCheckPermission\\(\"(approval:[^\"]+)\"\\)"
    );
    private static final Pattern SEED_APPROVAL_PERMISSION_PATTERN = Pattern.compile("'(approval:[^']+)'");

    @Test
    void seedDataContainsAllApprovalControllerPermissionsAndGrantsAdminRole() throws IOException {
        Set<String> controllerPermissions = extractPermissions(Files.readString(APPROVAL_CONTROLLER));
        Set<String> seedPermissions = extractSeedPermissions(Files.readString(SEED_DATA));

        assertFalse(controllerPermissions.isEmpty(), "ApprovalController should declare approval permissions");
        assertEquals(controllerPermissions, seedPermissions);

        String seedSql = Files.readString(SEED_DATA);
        int lastApprovalPermission = seedSql.lastIndexOf("'approval:");
        int adminGrant = seedSql.indexOf("SELECT 1, id FROM sys_permission");
        assertTrue(adminGrant > lastApprovalPermission, "admin role grant must run after approval permission inserts");
    }

    private Set<String> extractPermissions(String source) {
        Set<String> permissions = new TreeSet<>();
        Matcher matcher = APPROVAL_PERMISSION_PATTERN.matcher(source);
        while (matcher.find()) {
            permissions.add(matcher.group(1));
        }
        return permissions;
    }

    private Set<String> extractSeedPermissions(String seedSql) {
        Set<String> permissions = new TreeSet<>();
        Matcher matcher = SEED_APPROVAL_PERMISSION_PATTERN.matcher(seedSql);
        while (matcher.find()) {
            permissions.add(matcher.group(1));
        }
        return permissions;
    }
}
