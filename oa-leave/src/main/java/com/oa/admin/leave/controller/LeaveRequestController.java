package com.oa.admin.leave.controller;

import com.oa.admin.leave.dto.LeaveRequestCreateDTO;
import com.oa.admin.leave.dto.LeaveRequestQueryDTO;
import com.oa.admin.leave.dto.LeaveRequestUpdateDTO;
import com.oa.admin.leave.service.LeaveRequestService;
import com.oa.admin.leave.vo.LeaveRequestVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oa.admin.common.result.PageResult;
import com.oa.admin.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 请假申请 Controller
 * @author wxvirus
 */
@RestController
@RequestMapping("/api/v1/leave/leave_request")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    @SaCheckPermission("leave:leave_request:list")
    public R<PageResult<LeaveRequestVO>> list(LeaveRequestQueryDTO query) {
        return R.ok(leaveRequestService.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("leave:leave_request:query")
    public R<LeaveRequestVO> getById(@PathVariable Long id) {
        return R.ok(leaveRequestService.getDetail(id));
    }

    @PostMapping
    @SaCheckPermission("leave:leave_request:add")
    public R<LeaveRequestVO> create(@RequestBody LeaveRequestCreateDTO request) {
        return R.ok(leaveRequestService.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("leave:leave_request:edit")
    public R<LeaveRequestVO> update(@PathVariable Long id, @RequestBody LeaveRequestUpdateDTO request) {
        return R.ok(leaveRequestService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("leave:leave_request:remove")
    public R<Void> delete(@PathVariable Long id) {
        leaveRequestService.delete(id);
        return R.ok();
    }
}
