package com.oa.admin.generator.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * @author wxvirus
 */

class CodeGeneratorCrudStackTest {

    @TempDir
    Path tempDir;

    @Test
    void generateCreatesDtoVoBackedCrudStack() throws Exception {
        Path repoRoot = Path.of("").toAbsolutePath().getParent();
        Path projectRoot = tempDir.resolve("project");
        Path migrationDir = projectRoot.resolve("oa-app/src/main/resources/db/migration");
        Files.createDirectories(migrationDir);
        Files.writeString(migrationDir.resolve("V1__init.sql"), "-- init");

        new CodeGenerator().generate(
                repoRoot.resolve("generators/leave-request.yaml"),
                projectRoot,
                "oa-app",
                false,
                List.of(),
                false,
                false
        );

        Path packageRoot = projectRoot.resolve("oa-app/src/main/java/com/oa/admin/leave");
        assertTrue(Files.exists(packageRoot.resolve("dto/LeaveRequestCreateDTO.java")));
        assertTrue(Files.exists(packageRoot.resolve("dto/LeaveRequestUpdateDTO.java")));
        assertTrue(Files.exists(packageRoot.resolve("dto/LeaveRequestQueryDTO.java")));
        assertTrue(Files.exists(packageRoot.resolve("vo/LeaveRequestVO.java")));

        String createDto = Files.readString(packageRoot.resolve("dto/LeaveRequestCreateDTO.java"));
        assertTrue(createDto.contains("import com.oa.admin.leave.enums.LeaveType;"));
        assertTrue(createDto.contains("private LeaveType leaveType;"));

        String queryDto = Files.readString(packageRoot.resolve("dto/LeaveRequestQueryDTO.java"));
        assertTrue(queryDto.contains("import com.oa.admin.leave.enums.LeaveType;"));
        assertTrue(queryDto.contains("import com.oa.admin.leave.enums.LeaveStatus;"));
        assertTrue(queryDto.contains("private LeaveType leaveType;"));
        assertTrue(queryDto.contains("private LeaveStatus status;"));

        String vo = Files.readString(packageRoot.resolve("vo/LeaveRequestVO.java"));
        assertTrue(vo.contains("private LeaveType leaveType;"));
        assertTrue(vo.contains("private LeaveStatus status;"));

        String controller = Files.readString(packageRoot.resolve("controller/LeaveRequestController.java"));
        assertTrue(controller.contains("R<PageResult<LeaveRequestVO>> list(LeaveRequestQueryDTO query)"));
        assertTrue(controller.contains("R<LeaveRequestVO> create(@RequestBody LeaveRequestCreateDTO request)"));
        assertTrue(controller.contains("R<LeaveRequestVO> update(@PathVariable Long id, @RequestBody LeaveRequestUpdateDTO request)"));

        String service = Files.readString(packageRoot.resolve("service/LeaveRequestService.java"));
        assertTrue(service.contains("PageResult<LeaveRequestVO> page(LeaveRequestQueryDTO query);"));
        assertTrue(service.contains("LeaveRequestVO create(LeaveRequestCreateDTO request);"));
        assertTrue(service.contains("LeaveRequestVO update(Long id, LeaveRequestUpdateDTO request);"));

        String serviceImpl = Files.readString(packageRoot.resolve("service/impl/LeaveRequestServiceImpl.java"));
        assertTrue(serviceImpl.contains("query.getLeaveType().getCode()"));
        assertTrue(serviceImpl.contains("query.getStatus().getCode()"));
        assertTrue(serviceImpl.contains("LeaveType.fromCode(entity.getLeaveType())"));
        assertTrue(serviceImpl.contains("LeaveStatus.fromCode(entity.getStatus())"));
    }
}
