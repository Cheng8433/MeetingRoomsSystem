package com.myGroup.controller;

import com.myGroup.dto.ApiResponse;
import com.myGroup.dto.LoginRequest;
import com.myGroup.dto.RegisterSysUserRequest;
import com.myGroup.dto.UpdateSysUserRequest;
import com.myGroup.entity.SysUser;
import com.myGroup.entity.SysUserRole;
import com.myGroup.exception.BusinessException;
import com.myGroup.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class SysUserController {

    @Autowired
    private final SysUserService sysuserService;

    @Autowired
    public SysUserController(SysUserService sysuserService) {
        this.sysuserService = sysuserService;
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{userId}")
    public ApiResponse<SysUser> getSysUserById(@PathVariable("userId") int userId) {
        SysUser user = sysuserService.getSysUserById(userId);
        return ApiResponse.success("查询成功", user);

    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/by-name")
    public ResponseEntity<ApiResponse<SysUser>> getSysUserByName(@RequestParam String name) {
            SysUser user = sysuserService.getSysUserByName(name);
            return ResponseEntity.ok(ApiResponse.success("查询成功", user));
    }

    /**
     * 获取所有用户列表（管理员功能）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SysUser>>> getAllSysUsers() {
        try {
            List<SysUser> users = sysuserService.getAllSysUsers();
            return ResponseEntity.ok(ApiResponse.success("查询成功", users));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SysUser>> registerSysUser(@RequestBody RegisterSysUserRequest registerSysUserRequest) {
        try {
            //创建用户中
            SysUser user = new SysUser();
            user.setSysUsername(registerSysUserRequest.getUserName());
            user.setPassword(registerSysUserRequest.getPassword());
            user.setEmail(registerSysUserRequest.getEmail());
            user.setDepartment_name(registerSysUserRequest.getDepartment_name());
            user.setPhone(registerSysUserRequest.getPhone());
            user.setRealName(registerSysUserRequest.getRealName());
            user.setRole(SysUserRole.valueOf("user"));
            user.setCreateTimeToNow();

            SysUser registeredSysUser = sysuserService.registerSysUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("用户注册成功", registeredSysUser));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

/*** 更新用户信息
     */
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<UpdateSysUserRequest>> updateSysUser(
        @PathVariable String id,
        @RequestBody  UpdateSysUserRequest request) {
        SysUser user = sysuserService.getSysUserById(Integer.parseInt(id));
        boolean success = sysuserService.updateSysUser(user,request);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功",request));
        }
        return null;
}

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSysUser(@PathVariable String id) {
        try {
            boolean success = sysuserService.deleteSysUserById(id);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
            }
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
        return null;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest loginRequest,
                                                                  HttpSession session) { // 注入 HttpSession
        try {
            // ========== 新增：验证码校验 ==========
            String sessionCaptcha = (String) session.getAttribute("captcha");
            // 无论验证成功与否，都立即清除 session 中的验证码（防止重放）
            session.removeAttribute("captcha");

            // 检查验证码是否存在或已过期
            if (sessionCaptcha == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("验证码已失效，请刷新")); // 自定义错误码
            }

            // 忽略大小写比较
            if (!sessionCaptcha.equalsIgnoreCase(loginRequest.getCaptcha())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("验证码错误"));
            }
            // ========== 校验结束 ==========

            // 原有的登录逻辑
            SysUser user = sysuserService.login(loginRequest.getUsername(), loginRequest.getPassword());
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("user", user);
            loginData.put("token", generateToken(user));

            return ResponseEntity.ok(ApiResponse.success("登录成功", loginData));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

    /**
     * 批量删除用户
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteSysUsers(@RequestBody List<String> ids) {
        try {
            int successCount = sysuserService.batchDeleteSysUsers(ids);
            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", successCount);
            result.put("totalCount", ids.size());

            return ResponseEntity.ok(ApiResponse.success("成功删除 " + successCount + " 个用户", result));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchSysUsers(@RequestParam String keyword) {
        try {
            List<SysUser> users = sysuserService.searchSysUsers(keyword);
            Map<String, Object> searchData = new HashMap<>();
            searchData.put("list", users);
            searchData.put("keyword", keyword);
            searchData.put("count", users.size());

            return ResponseEntity.ok(ApiResponse.success("搜索成功", searchData));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSysUsernameExists(@RequestParam String username) {
        try {
            boolean exists = sysuserService.isSysUserNameExists(username);
            Map<String, Object> checkData = new HashMap<>();
            checkData.put("exists", exists);
            checkData.put("username", username);

            String message = exists ? "用户名已存在" : "用户名可用";
            return ResponseEntity.ok(ApiResponse.success(message, checkData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("检查用户名失败", "CHECK_USERNAME_ERROR"));
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable int id,
            @RequestBody ResetPasswordRequest request) {
        try {
            boolean success = sysuserService.resetPassword(id, request.getNewPassword());
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("密码重置失败", "RESET_PASSWORD_FAILED"));
            }
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSysUserStats() {
        try {
            int totalSysUsers = sysuserService.getSysUserCount();
            List<SysUser> allSysUsers = sysuserService.getAllSysUsers();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSysUsers", totalSysUsers);
            // 可以添加更多统计信息

            return ResponseEntity.ok(ApiResponse.success("统计信息获取成功", stats));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), e.getCode()));
        }
    }

    // 模拟token生成
    private String generateToken(SysUser user) {
        return "mock_token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    // 内部类：重置密码请求DTO
    public static class ResetPasswordRequest {
        private String newPassword;

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}